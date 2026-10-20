package com.neval.anoba.photo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neval.anoba.R
import com.neval.anoba.common.repository.PinnedItemRepository
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.AnalyticsHelper
import com.neval.anoba.common.utils.ResourceProvider
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.photo.photocomment.PhotoComment
import com.neval.anoba.photo.photocomment.PhotoCommentRepository
import com.neval.anoba.photo.photoemoji.PhotoEmojiReactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap

sealed class ShareState {
    data object Idle : ShareState()
    data object Processing : ShareState()
    data class Success(val uri: Uri) : ShareState()
    data class Error(val message: String) : ShareState()
}

sealed class PhotoUiEvent {
    data class ShowToast(val message: String) : PhotoUiEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoViewModel(
    private val authService: AuthServiceInterface,
    private val photoRepository: PhotoRepository,
    private val reactionRepository: PhotoEmojiReactionRepository,
    private val commentRepository: PhotoCommentRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val resourceProvider: ResourceProvider,
    private val pinnedItemRepository: PinnedItemRepository
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val pinnedIds: StateFlow<Set<String>> = authService.authStateFlow.flatMapLatest { state ->
        if (state is AuthState.Authenticated) {
            pinnedItemRepository.getAllPinnedItems(state.uid).map { list -> list.map { it.contentId }.toSet() }
        } else {
            flowOf(emptySet())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun togglePin(contentId: String) {
        val uid = authService.getCurrentUserId()
        if (uid.isBlank()) return
        viewModelScope.launch {
            pinnedItemRepository.togglePin(uid, contentId, "photo")
        }
    }

    val photosState: StateFlow<UiState<List<PhotoModel>>> = combine(photoRepository.getPhotos(), _refreshTrigger) { photos, _ ->
        val state: UiState<List<PhotoModel>> = UiState.Success(photos.shuffled())
        state
    }.catch { emit(UiState.Error(resourceProvider.getString(R.string.state_error_loading))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val photos: StateFlow<List<PhotoModel>> = photosState
        .map { state -> if (state is UiState.Success<List<PhotoModel>>) state.data else emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _photoState = MutableStateFlow<UiState<PhotoModel>>(UiState.Idle)
    val photoState: StateFlow<UiState<PhotoModel>> = _photoState.asStateFlow()

    private val _comments = MutableStateFlow<List<PhotoComment>>(emptyList())
    val comments: StateFlow<List<PhotoComment>> = _comments.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _shareState = MutableStateFlow<ShareState>(ShareState.Idle)
    val shareState: StateFlow<ShareState> = _shareState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PhotoUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val lastReactionTimestamps = ConcurrentHashMap<String, Long>()

    private var photoDetailsJob: Job? = null
    private var commentsJob: Job? = null
    private var uploadJob: Job? = null

    val userReactions: StateFlow<Map<String, String>> =
        authService.authStateFlow.flatMapLatest { authState ->
            when (authState) {
                is AuthState.Authenticated -> reactionRepository.getUserPhotoReactionsFlow(authState.uid)
                else -> flowOf(emptyMap())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshTrigger.value++
            kotlinx.coroutines.delay(500)
            _isRefreshing.value = false
        }
    }

    fun loadPhotoDetails(photoId: String) {
        photoDetailsJob?.cancel()
        photoDetailsJob = viewModelScope.launch {
            _photoState.value = UiState.Loading
            photoRepository.getPhoto(photoId)
                .catch {
                    _photoState.value = UiState.Error(resourceProvider.getString(R.string.state_error_loading))
                }
                .collect { photo ->
                    _photoState.value = if (photo != null) {
                        UiState.Success(photo)
                    } else {
                        UiState.Error(resourceProvider.getString(R.string.error_user_not_found))
                    }
                }
        }
    }

    fun incrementViewCount(photoId: String) {
        viewModelScope.launch {
            photoRepository.incrementViewCount(photoId)
        }
    }

    fun unflagPhoto(photoId: String) {
        viewModelScope.launch {
            photoRepository.unflagPhoto(photoId)
        }
    }

    fun deletePhoto(photoId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = photoRepository.deletePhoto(photoId)
            if (result) {
                analyticsHelper.logEvent("photo_deleted") {
                    putString("photo_id", photoId)
                }
            }
            onComplete(result)
        }
    }

    fun uploadPhotoAndCreateRecord(
        context: Context,
        photoUri: Uri,
        title: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (_isUploading.value) return

        val authState = authService.authStateFlow.value as? AuthState.Authenticated
        if (authState == null) {
            onComplete(false, null)
            return
        }

        uploadJob = viewModelScope.launch {
            _isUploading.value = true
            try {
                val userRole = authService.getUserRole()
                val isGuide = userRole == "ADMIN" || userRole == "GUIDE"

                val username = authState.displayName ?: if (authState.isAnonymous) resourceProvider.getString(R.string.guest) else resourceProvider.getString(R.string.state_loading)
                val userProfileImageUrl = authState.photoUrl
                val badgeType = authState.badgeType

                val newPhotoId = photoRepository.uploadPhoto(
                    userId = authState.uid,
                    username = username,
                    userProfileImageUrl = userProfileImageUrl,
                    badgeType = badgeType,
                    photoUri = photoUri,
                    title = title,
                    isPublished = false,
                    isGuideContent = isGuide,
                    context = context
                )
                if (newPhotoId != null) {
                    analyticsHelper.logEvent("photo_uploaded") {
                        putBoolean("is_guide", isGuide)
                    }
                }
                onComplete(newPhotoId != null, newPhotoId)
            } catch (ex: Exception) {
                Log.e("PhotoViewModel", "Upload error", ex)
                onComplete(false, null)
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun toggleReaction(photoId: String, emoji: String) {
        val authState = authService.authStateFlow.value
        val authenticatedUser = authState as? AuthState.Authenticated ?: return

        val now = System.currentTimeMillis()
        val lastClick = lastReactionTimestamps[photoId] ?: 0L
        if (now - lastClick < 500) return
        lastReactionTimestamps[photoId] = now

        viewModelScope.launch {
            try {
                reactionRepository.toggleReaction(authenticatedUser.uid, photoId, emoji)
                analyticsHelper.logEvent("photo_reaction_toggle") {
                    putString("emoji", emoji)
                    putString("photo_id", photoId)
                }
            } catch (_: Exception) {
                _eventFlow.emit(PhotoUiEvent.ShowToast("İşlem başarısız oldu."))
            }
        }
    }

    fun sharePhoto(context: Context, photoUrl: String, photoId: String) {
        analyticsHelper.logButtonClick("Share Photo")
        viewModelScope.launch {
            _shareState.value = ShareState.Processing
            try {
                withTimeout(30000) {
                    val uri = photoRepository.getPhotoUriForSharing(context, photoUrl, photoId)
                    _shareState.value = ShareState.Success(uri)
                    analyticsHelper.logEvent("photo_shared") {
                        putString("photo_id", photoId)
                    }
                }
            } catch (e: Exception) {
                _shareState.value =
                    ShareState.Error(e.message ?: "Fotoğraf paylaşılırken bir hata oluştu.")
            }
        }
    }

    fun resetShareState() {
        _shareState.value = ShareState.Idle
    }

    fun loadComments(photoId: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            commentRepository.getCommentsStream(photoId).collect {
                _comments.value = it
            }
        }
    }

    fun addComment(photoId: String, text: String) {
        val user = authService.getCurrentFirebaseUser() ?: return

        viewModelScope.launch {
            val comment = PhotoComment(
                photoId = photoId,
                userId = user.uid,
                username = user.displayName ?: if (user.isAnonymous) resourceProvider.getString(R.string.guest) else resourceProvider.getString(R.string.state_loading),
                content = text,
                userProfileUrl = user.photoUrl?.toString()
            )
            commentRepository.addComment(photoId, comment)
            analyticsHelper.logEvent("photo_comment_added") {
                putString("photo_id", photoId)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        photoDetailsJob?.cancel()
        commentsJob?.cancel()
        uploadJob?.cancel()
    }
}