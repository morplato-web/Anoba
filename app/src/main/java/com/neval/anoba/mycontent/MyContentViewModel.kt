package com.neval.anoba.mycontent

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.neval.anoba.R
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.letter.LetterRepository
import com.neval.anoba.market.MarketRepository
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.photo.PhotoRepository
import com.neval.anoba.ses.SesRepository
import com.neval.anoba.video.VideoRepository
import com.neval.anoba.video.getVideoDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import androidx.lifecycle.SavedStateHandle

@OptIn(ExperimentalCoroutinesApi::class)
class MyContentViewModel(
    private val letterRepository: LetterRepository,
    private val photoRepository: PhotoRepository,
    private val videoRepository: VideoRepository,
    private val sesRepository: SesRepository,
    private val marketRepository: MarketRepository,
    private val authService: AuthServiceInterface,
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uploadProgress = MutableStateFlow<String?>(null)
    val uploadProgress: StateFlow<String?> = _uploadProgress.asStateFlow()

    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage.asStateFlow()

    private val refreshTrigger = MutableStateFlow(0)
    private var uploadJob: Job? = null
    private var downloadJob: Job? = null

    val targetUserId: String? = savedStateHandle.get<String>("userId")
    private val _targetUsername = MutableStateFlow<String?>(null)
    val targetUsername: StateFlow<String?> = _targetUsername.asStateFlow()

    val isOwner: StateFlow<Boolean> = authService.authStateFlow
        .map { state ->
            val currentUid = (state as? AuthState.Authenticated)?.uid
            targetUserId.isNullOrBlank() || targetUserId == currentUid
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        if (!targetUserId.isNullOrBlank()) {
            viewModelScope.launch {
                try {
                    val userDoc = firestore.collection("users").document(targetUserId).get().await()
                    val uname = userDoc.getString("username")?.ifBlank {
                        userDoc.getString("displayName")
                    } ?: userDoc.getString("displayName")
                    _targetUsername.value = uname
                } catch (e: Exception) {
                    Log.w("MyContentViewModel", "Target username error: ${e.message}")
                }
            }
        }
    }

    val isAnobaUser: StateFlow<Boolean> = authService.authStateFlow
        .map { state ->
            val displayName = (state as? AuthState.Authenticated)?.displayName
            displayName?.equals("anoba", ignoreCase = true) == true
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val allUserContent: StateFlow<List<MyContentItem>> = combine(authService.authStateFlow, refreshTrigger) { authState, _ ->
        authState
    }.flatMapLatest { authState ->
        val currentUid = (authState as? AuthState.Authenticated)?.uid ?: ""
        val effectiveUid = if (!targetUserId.isNullOrBlank()) targetUserId else currentUid

        if (effectiveUid.isNotBlank()) {
            val lettersFlow = letterRepository.getUserLetters(effectiveUid).catch { emit(emptyList()) }
            val photosFlow = photoRepository.getUserPhotos(effectiveUid).catch { emit(emptyList()) }
            val videosFlow = videoRepository.getUserVideos(effectiveUid).catch { emit(emptyList()) }
            val audiosFlow = sesRepository.getUserAudios(effectiveUid).catch { emit(emptyList()) }
            val marketsFlow = marketRepository.getMarketItemsStream().map { items -> items.filter { it.ownerId == effectiveUid } }.catch { emit(emptyList()) }

            combine(lettersFlow, photosFlow, videosFlow, audiosFlow, marketsFlow) { letters, photos, videos, audios, markets ->
                (letters.map { MyContentItem.Letter(it) } +
                        photos.map { MyContentItem.Photo(it) } +
                        videos.map { MyContentItem.Video(it) } +
                        audios.map { MyContentItem.Audio(it) } +
                        markets.map { MyContentItem.Market(it) })
                    .filter { it.id.isNotBlank() }
            }.onStart {
                _isLoading.value = true
                _errorMessage.value = null
            }
                .catch { error ->
                    _isLoading.value = false
                    _errorMessage.value = R.string.mycontent_error_loading
                    Log.e("MyContentViewModel", "Content load error", error)
                    emit(emptyList())
                }
        } else {
            flowOf(emptyList())
        }
    }
        .map {
            _isLoading.value = false
            it
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val publishedContent: StateFlow<List<MyContentItem>> = combine(allUserContent, isOwner) { content, owner ->
        if (owner) {
            content.filter { it.isPublished && it !is MyContentItem.Market }
                .sortedByDescending { it.sortDate }
        } else {
            content.filter { it.isPublished && !it.isFlagged && it !is MyContentItem.Market }
                .sortedByDescending { it.sortDate }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val draftContent: StateFlow<List<MyContentItem>> = combine(allUserContent, isOwner) { content, owner ->
        if (owner) {
            content.filter { !it.isPublished && it !is MyContentItem.Market }
                .sortedByDescending { it.sortDate }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketContent: StateFlow<List<MyContentItem.Market>> = combine(allUserContent, isOwner) { content, owner ->
        if (owner) {
            content.filterIsInstance<MyContentItem.Market>()
                .sortedByDescending { it.sortDate }
        } else {
            content.filterIsInstance<MyContentItem.Market>()
                .filter { it.isPublished && !it.isFlagged }
                .sortedByDescending { it.sortDate }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _videoDownloadState = MutableStateFlow<VideoDownloadState>(VideoDownloadState.Idle)
    val videoDownloadState: StateFlow<VideoDownloadState> = _videoDownloadState.asStateFlow()

    private val _launchGallery = MutableStateFlow(false)
    val launchGallery: StateFlow<Boolean> = _launchGallery.asStateFlow()

    fun publishContent(id: String, type: MyContentType) {
        viewModelScope.launch {
            try {
                when (type) {
                    MyContentType.LETTER -> letterRepository.togglePublishState(id)
                    MyContentType.PHOTO -> photoRepository.togglePublishState(id)
                    MyContentType.VIDEO -> videoRepository.togglePublishState(id)
                    MyContentType.AUDIO -> sesRepository.togglePublishState(id)
                    MyContentType.MARKET -> marketRepository.markAsSold(id)
                }
                refreshTrigger.value++
            } catch (_: Exception) {
                _errorMessage.value = R.string.error_action_failed
            }
        }
    }

    fun toggleAnobaGuideState(id: String, type: MyContentType) {
        val currentDisplayName = (authService.authStateFlow.value as? AuthState.Authenticated)?.displayName
        if (currentDisplayName?.equals("anoba", ignoreCase = true) == false) return

        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val collectionName = when (type) {
                    MyContentType.LETTER -> "letters"
                    MyContentType.PHOTO -> "photos"
                    MyContentType.VIDEO -> "videos"
                    MyContentType.AUDIO -> "sesler"
                    MyContentType.MARKET -> "market_items"
                }
                val docRef = db.collection(collectionName).document(id)
                val snapshot = docRef.get().await()
                val currentGuideState = snapshot.getBoolean("is_guide_content") ?: false
                docRef.update("is_guide_content", !currentGuideState).await()
                refreshTrigger.value++
            } catch (_: Exception) { }
        }
    }

    fun deleteContent(id: String, type: MyContentType) {
        if (id.isBlank()) return
        viewModelScope.launch {
            try {
                when (type) {
                    MyContentType.LETTER -> letterRepository.deleteLetter(id)
                    MyContentType.PHOTO -> photoRepository.deletePhoto(id)
                    MyContentType.VIDEO -> videoRepository.deleteVideo(id)
                    MyContentType.AUDIO -> sesRepository.deleteSes(id)
                    MyContentType.MARKET -> marketRepository.deleteItem(id)
                }
                refreshTrigger.value++
            } catch (_: Exception) {
                _errorMessage.value = R.string.mycontent_error_delete
            }
        }
    }

    fun onEditClicked(item: MyContentItem, context: Context, navController: NavController) {
        if (item.id.isBlank()) return
        when (item) {
            is MyContentItem.Photo -> navController.navigate(Constants.PHOTO_EDIT_SCREEN.replace("{photoId}", item.photo.id))
            is MyContentItem.Video -> {
                downloadJob?.cancel()
                downloadJob = viewModelScope.launch {
                    _videoDownloadState.value = VideoDownloadState.InProgress
                    try {
                        withTimeout(60000) {
                            val result = videoRepository.downloadVideoToCache(context, item.video.id, item.video.videoUrl)
                            _videoDownloadState.value = result
                        }
                    } catch (_: Exception) {
                        _videoDownloadState.value = VideoDownloadState.Error(messageResId = R.string.mycontent_error_timeout)
                    }
                }
            }
            is MyContentItem.Audio -> navController.navigate(Constants.SES_EDIT_SCREEN.replace("{sesId}", item.audio.id))
            is MyContentItem.Letter -> navController.navigate(Constants.LETTER_EDIT_SCREEN.replace("{letterId}", item.letter.id))
            is MyContentItem.Market -> navController.navigate("market_edit_screen/${item.marketItem.id}")
        }
    }

    fun resetVideoDownloadState() { _videoDownloadState.value = VideoDownloadState.Idle }
    fun onGalleryIconClicked() { _launchGallery.value = true }
    fun onGalleryLaunched() { _launchGallery.value = false }

    fun cancelUpload() {
        uploadJob?.cancel()
        _isLoading.value = false
        _uploadProgress.value = null
    }

    fun onMediaSelected(uris: List<Uri>, context: Context) {
        if (uris.isEmpty()) return
        uploadMultipleMediaInternal(uris, context)
    }

    private fun uploadMultipleMediaInternal(uris: List<Uri>, context: Context) {
        if (_isLoading.value) return

        val authState = authService.authStateFlow.value as? AuthState.Authenticated
        val userId = authState?.uid ?: ""
        val username = authState?.displayName ?: "Bilinmiyor"
        val userProfileUrl = authState?.photoUrl
        val badgeType = authState?.badgeType
        val title = "Yeni Anı"

        if (userId.isBlank()) return

        uploadJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                uris.forEachIndexed { index, uri ->
                    _uploadProgress.value = "${index + 1} / ${uris.size}"
                    
                    val mimeType = withContext(Dispatchers.IO) {
                        context.contentResolver.getType(uri)
                    }

                    if (mimeType?.startsWith("video/") == true) {
                        val duration = withContext(Dispatchers.IO) {
                            getVideoDuration(context, uri)
                        }
                        videoRepository.uploadVideo(
                            context = context,
                            videoUri = uri, title = title, duration = duration,
                            userId = userId, username = username, userProfileImageUrl = userProfileUrl,
                            badgeType = badgeType, isPublished = false
                        )
                    } else if (mimeType?.startsWith("image/") == true) {
                        photoRepository.uploadPhoto(
                            photoUri = uri, title = title,
                            userId = userId, username = username, userProfileImageUrl = userProfileUrl,
                            badgeType = badgeType, isPublished = false,
                            context = context
                        )
                    }
                }
                refreshTrigger.value++
            } catch (e: Exception) {
                Log.e("MyContentViewModel", "Upload error", e)
                _errorMessage.value = R.string.auth_generic_fail
            } finally {
                _isLoading.value = false
                _uploadProgress.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        uploadJob?.cancel()
        downloadJob?.cancel()
    }
}