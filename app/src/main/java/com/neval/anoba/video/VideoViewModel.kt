package com.neval.anoba.video

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neval.anoba.R
import com.neval.anoba.common.repository.PinnedItemRepository
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.AnalyticsHelper
import com.neval.anoba.common.utils.ResourceProvider
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.photo.ShareState
import com.neval.anoba.video.videoemoji.VideoEmojiReactionRepository
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
import java.util.concurrent.ConcurrentHashMap

sealed class VideoUiEvent {
    data class ShowToast(val message: String) : VideoUiEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class VideoViewModel(
    private val videoRepository: VideoRepository,
    private val reactionRepository: VideoEmojiReactionRepository,
    private val authService: AuthServiceInterface,
    private val analyticsHelper: AnalyticsHelper,
    private val resourceProvider: ResourceProvider,
    private val pinnedItemRepository: PinnedItemRepository
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)

    private var videoDetailsJob: Job? = null
    private var uploadJob: Job? = null

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
            pinnedItemRepository.togglePin(uid, contentId, "video")
        }
    }

    // 🚀 ANA VİDEO AKIŞI (SADE)
    val videosState: StateFlow<UiState<List<VideoModel>>> = videoRepository.getVideos()
        .combine(_refreshTrigger) { videos: List<VideoModel>, _: Int ->
            val sortedVideos = if (videos.size > 3) {
                val newest = videos.take(3)
                val remaining = videos.drop(3).shuffled()
                newest + remaining
            } else {
                videos
            }
            UiState.Success(sortedVideos) as UiState<List<VideoModel>>
        }
        .catch { e ->
            emit(UiState.Error(e.localizedMessage ?: resourceProvider.getString(R.string.state_error_loading)) as UiState<List<VideoModel>>)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _videoDetails = MutableStateFlow<UiState<VideoModel>>(UiState.Idle)
    val videoDetails: StateFlow<UiState<VideoModel>> = _videoDetails.asStateFlow()

    val userReactions: StateFlow<Map<String, String>> =
        authService.authStateFlow.flatMapLatest { authState ->
            when (authState) {
                is AuthState.Authenticated -> reactionRepository.getUserVideoReactionsFlow(authState.uid)
                else -> flowOf(emptyMap())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<VideoUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _shareState = MutableStateFlow<ShareState>(ShareState.Idle)
    val shareState: StateFlow<ShareState> = _shareState.asStateFlow()

    private val viewCountTimestamps = ConcurrentHashMap<String, Long>()
    private val lastReactionTimestamps = ConcurrentHashMap<String, Long>()

    fun refresh() {
        _refreshTrigger.value++
    }

    fun loadVideoDetails(videoId: String) {
        videoDetailsJob?.cancel()
        videoDetailsJob = viewModelScope.launch {
            _videoDetails.value = UiState.Loading
            videoRepository.getVideo(videoId)
                .catch {
                    _videoDetails.value = UiState.Error(resourceProvider.getString(R.string.state_error_loading))
                }
                .collect { video ->
                    _videoDetails.value = if (video != null) {
                        UiState.Success(video)
                    } else {
                        UiState.Error(resourceProvider.getString(R.string.state_no_data))
                    }
                }
        }
    }

    fun uploadVideo(context: Context, videoUri: Uri, title: String, duration: Long, onComplete: (Boolean, String?) -> Unit) {
        if (_isUploading.value || uploadJob?.isActive == true) return

        val authState = authService.authStateFlow.value as? AuthState.Authenticated ?: return
        
        uploadJob = viewModelScope.launch {
            _isUploading.value = true
            try {
                val newVideoId = videoRepository.uploadVideo(
                    context = context,
                    userId = authState.uid,
                    username = authState.displayName ?: resourceProvider.getString(R.string.guest),
                    userProfileImageUrl = authState.photoUrl,
                    badgeType = authState.badgeType,
                    videoUri = videoUri,
                    title = title,
                    duration = duration,
                    isPublished = false
                )
                onComplete(newVideoId != null, newVideoId)
                if (newVideoId != null) {
                    analyticsHelper.logEvent("video_uploaded") {
                        putLong("duration", duration)
                    }
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage)
            } finally {
                _isUploading.value = false
                uploadJob = null
            }
        }
    }

    fun toggleReaction(videoId: String, emoji: String) {
        val authState = authService.authStateFlow.value as? AuthState.Authenticated ?: return

        val now = System.currentTimeMillis()
        val lastClick = lastReactionTimestamps[videoId] ?: 0L
        if (now - lastClick < 500) return
        lastReactionTimestamps[videoId] = now

        viewModelScope.launch {
            try {
                reactionRepository.toggleReaction(authState.uid, videoId, emoji)
            } catch (e: Exception) {
                _eventFlow.emit(VideoUiEvent.ShowToast("Hata: ${e.localizedMessage}"))
            }
        }
    }

    fun incrementViewCount(videoId: String) {
        val now = System.currentTimeMillis()
        val lastIncremented = viewCountTimestamps[videoId] ?: 0L
        if (now - lastIncremented > 300_000) {
            viewModelScope.launch {
                videoRepository.incrementViewCount(videoId)
                viewCountTimestamps[videoId] = now
            }
        }
    }

    fun unflagVideo(videoId: String) {
        viewModelScope.launch {
            videoRepository.unflagVideo(videoId)
        }
    }

    fun deleteVideo(videoId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = videoRepository.deleteVideo(videoId)
            onComplete(result)
        }
    }

    fun shareVideo(context: Context, videoUrl: String, videoId: String) {
        viewModelScope.launch {
            _shareState.value = ShareState.Processing
            try {
                val uri = videoRepository.getVideoUriForSharing(context, videoUrl, videoId)
                _shareState.value = ShareState.Success(uri)
            } catch (e: Exception) {
                _shareState.value = ShareState.Error(e.message ?: "Hata")
            }
        }
    }

    fun resetShareState() {
        _shareState.value = ShareState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        videoDetailsJob?.cancel()
        uploadJob?.cancel()
    }
}
