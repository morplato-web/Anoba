package com.neval.anoba.ses

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neval.anoba.R
import com.neval.anoba.common.repository.IUserRepository
import com.neval.anoba.common.repository.PinnedItemRepository
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.AnalyticsHelper
import com.neval.anoba.common.utils.ResourceProvider
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.models.User
import com.neval.anoba.photo.ShareState
import com.neval.anoba.ses.sesemoji.SesEmojiReactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

sealed class SesUiEvent {
    data class ShowToast(val message: String) : SesUiEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class SesViewModel(
    val sesRepository: SesRepository,
    val authService: AuthServiceInterface,
    private val reactionRepository: SesEmojiReactionRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val resourceProvider: ResourceProvider,
    private val pinnedItemRepository: PinnedItemRepository,
    userRepository: IUserRepository
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)

    // Kullanıcı listesi (Mühürlü ses için)
    val allUsers: StateFlow<List<User>> = userRepository.getAllUsersStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seslerState: StateFlow<UiState<List<SesModel>>> = combine(sesRepository.getSesler(), _refreshTrigger) { sesList, _ ->
        val state: UiState<List<SesModel>> = UiState.Success(sesList.shuffled())
        state
    }.catch { e ->
        Log.e("SesViewModel", "Error loading all ses", e)
        emit(UiState.Error(resourceProvider.getString(R.string.state_error_loading)))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

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
            pinnedItemRepository.togglePin(uid, contentId, "audio")
        }
    }

    private val _sesDetails = MutableStateFlow<UiState<SesModel>>(UiState.Idle)
    val sesDetails: StateFlow<UiState<SesModel>> = _sesDetails.asStateFlow()

    private val _shareState = MutableStateFlow<ShareState>(ShareState.Idle)
    val shareState: StateFlow<ShareState> = _shareState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SesUiEvent>()
    @Suppress("unused")
    val eventFlow = _eventFlow.asSharedFlow()

    private val lastReactionTimestamps = ConcurrentHashMap<String, Long>()

    private var mediaRecorder: MediaRecorder? = null
    var audioFile: File? = null

    val userReactions: StateFlow<Map<String, String>> = authService.authStateFlow
        .flatMapLatest { authState ->
            if (authState is AuthState.Authenticated) {
                reactionRepository.getUserSesReactionsFlow(authState.uid)
            } else {
                emptyFlow()
            }
        }
        .catch { e ->
            Log.e("SesViewModel", "Error observing user reactions", e)
            emit(emptyMap())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun refresh() {
        _refreshTrigger.value++
    }

    fun loadSesDetails(sesId: String) {
        viewModelScope.launch {
            _sesDetails.value = UiState.Loading
            sesRepository.getSesFlow(sesId)
                .catch { e ->
                    Log.e("SesViewModel", "Error getting ses details", e)
                    _sesDetails.value = UiState.Error(resourceProvider.getString(R.string.state_error_loading))
                }
                .collect { ses ->
                    _sesDetails.value = if (ses != null) {
                        UiState.Success(ses)
                    } else {
                        UiState.Error(resourceProvider.getString(R.string.state_no_data))
                    }
                }
        }
    }

    fun startRecording(context: Context) {
        analyticsHelper.logButtonClick("Start Voice Recording")
        val fileName = "${context.cacheDir.absolutePath}/audiorecord.m4a"
        audioFile = File(fileName)

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(fileName)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("SesViewModel", "Kayıt başlatılamadı: ${e.message}")
            viewModelScope.launch {
                _eventFlow.emit(SesUiEvent.ShowToast("Mikrofon şu an kullanılamıyor. Lütfen izinleri kontrol edin."))
            }
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    fun stopRecording() {
        analyticsHelper.logButtonClick("Stop Voice Recording")
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e("SesViewModel", "Kayıt durdurulamadı: ${e.message}")
                release()
            }
        }
        mediaRecorder = null
    }

    fun cancelRecording() {
        analyticsHelper.logButtonClick("Cancel Voice Recording")
        stopRecording()
        audioFile?.delete()
        audioFile = null
    }

    fun uploadAudioToFirebase(file: File, privacy: String = "PUBLIC", unlockTimestamp: Long? = null, onComplete: (String?) -> Unit) {
        val authState = authService.authStateFlow.value as? AuthState.Authenticated
        if (authState == null) {
            onComplete(null)
            return
        }
        val username = authState.displayName ?: if (authState.isAnonymous) resourceProvider.getString(R.string.guest) else resourceProvider.getString(R.string.state_loading)
        val userProfileImageUrl = authState.photoUrl
        val badgeType = authState.badgeType

        viewModelScope.launch {
            try {
                val duration = getAudioDuration(file.absolutePath)
                val newAudioId = sesRepository.uploadAudio(
                    userId = authState.uid,
                    username = username,
                    userProfileImageUrl = userProfileImageUrl,
                    badgeType = badgeType,
                    file = file,
                    duration = duration,
                    isPublished = false
                )
                if (newAudioId != null) {
                    // Gizlilik ve Zaman Kapsülü tercihlerini güncelle
                    val updates = mutableMapOf<String, Any?>("privacy" to privacy)
                    if (unlockTimestamp != null) updates["unlockTimestamp"] = unlockTimestamp

                    sesRepository.updateSes(newAudioId, updates)

                    analyticsHelper.logEvent("audio_uploaded") {
                        putLong("duration", duration)
                        putString("privacy", privacy)
                        if (unlockTimestamp != null) putLong("unlock_ts", unlockTimestamp)
                    }
                }
                onComplete(newAudioId)
            } catch (e: Exception) {
                Log.e("SesViewModel", "Yükleme hatası", e)
                onComplete(null)
            }
        }
    }

    fun incrementViewCount(sesId: String) {
        viewModelScope.launch {
            sesRepository.incrementViewCount(sesId)
        }
    }

    fun unflagSes(sesId: String) {
        viewModelScope.launch {
            sesRepository.unflagSes(sesId)
        }
    }

    fun deleteAudio(sesId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = sesRepository.deleteSes(sesId)
            if (result) {
                analyticsHelper.logEvent("audio_deleted") {
                    putString("audio_id", sesId)
                }
            }
            onComplete(result)
        }
    }

    fun toggleReaction(sesId: String, emoji: String) {
        val authState = authService.authStateFlow.value
        val authenticatedUser = authState as? AuthState.Authenticated ?: return

        val now = System.currentTimeMillis()
        val lastClick = lastReactionTimestamps[sesId] ?: 0L
        if (now - lastClick < 500) return
        lastReactionTimestamps[sesId] = now

        viewModelScope.launch {
            try {
                reactionRepository.toggleReaction(
                    userId = authenticatedUser.uid,
                    sesId = sesId,
                    emoji = emoji
                )
                analyticsHelper.logEvent("audio_reaction_toggle") {
                    putString("emoji", emoji)
                    putString("audio_id", sesId)
                }
            } catch (_: Exception) {
                _eventFlow.emit(SesUiEvent.ShowToast("İşlem başarısız oldu."))
            }
        }
    }

    private fun getAudioDuration(filePath: String): Long {
        val player = MediaPlayer()
        return try {
            player.setDataSource(filePath)
            player.prepare()
            val duration = player.duration.toLong()
            player.release()
            duration
        } catch (_: Exception) {
            player.release()
            0L
        }
    }

    fun shareSes(context: Context, audioUrl: String, sesId: String) {
        analyticsHelper.logButtonClick("Share Voice")
        viewModelScope.launch {
            _shareState.value = ShareState.Processing
            try {
                val uri = sesRepository.getAudioUriForSharing(context, audioUrl, sesId)
                _shareState.value = ShareState.Success(uri)
                analyticsHelper.logEvent("audio_shared") {
                    putString("audio_id", sesId)
                }
            } catch (e: Exception) {
                _shareState.value = ShareState.Error(e.message ?: "Ses paylaşılırken bir hata oluştu.")
            }
        }
    }

    fun resetShareState() {
        _shareState.value = ShareState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}