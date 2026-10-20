package com.neval.anoba.letter

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
import com.neval.anoba.letter.letteremoji.LetterEmojiReactionRepository
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.models.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

sealed class LetterUiEvent {
    data class ShowToast(val message: String) : LetterUiEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class LetterViewModel(
    private val authService: AuthServiceInterface,
    private val letterRepository: LetterRepository,
    private val reactionRepository: LetterEmojiReactionRepository,
    userRepository: IUserRepository,
    private val resourceProvider: ResourceProvider,
    private val analyticsHelper: AnalyticsHelper,
    private val pinnedItemRepository: PinnedItemRepository
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedLetter = MutableStateFlow<UiState<LetterModel>>(UiState.Idle)
    val selectedLetter: StateFlow<UiState<LetterModel>> = _selectedLetter.asStateFlow()

    private val viewCountTimestamps = ConcurrentHashMap<String, Long>()
    private val lastReactionTimestamps = ConcurrentHashMap<String, Long>()

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
            pinnedItemRepository.togglePin(uid, contentId, "letter")
        }
    }

    val lettersState: StateFlow<UiState<List<LetterModel>>> = combine(letterRepository.getLetters(), _refreshTrigger) { letters, _ ->
        val publicLetters = letters.filter { it.privacy == "PUBLIC" }
        val state: UiState<List<LetterModel>> = UiState.Success(publicLetters.shuffled())
        state
    }.catch { e ->
        Log.e("LetterViewModel", "Error loading letters: ${e.localizedMessage}")
        emit(UiState.Error(resourceProvider.getString(R.string.state_error_loading)))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    val letters: StateFlow<List<LetterModel>> = lettersState
        .map { state -> if (state is UiState.Success<List<LetterModel>>) state.data else emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LetterUiEvent>()

    // --- SES KAYIT ALANLARI ---
    private var mediaRecorder: android.media.MediaRecorder? = null
    var audioFile: java.io.File? = null
    // -------------------------

    @get:Suppress("unused")
    val eventFlow: SharedFlow<LetterUiEvent> = _eventFlow.asSharedFlow()

    val allUsers: StateFlow<List<User>> = userRepository.getAllUsersStream()
        .catch { e ->
            Log.e("LetterViewModel", "Error loading users: ${e.localizedMessage}")
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userReactions: StateFlow<Map<String, String>> = authService.authStateFlow.flatMapLatest { authState ->
        if (authState is AuthState.Authenticated) {
            reactionRepository.getUserLetterReactions(authState.uid)
        } else {
            emptyFlow()
        }
    }.catch { e ->
        Log.e("LetterViewModel", "Error reactions: ${e.localizedMessage}")
        emit(emptyMap())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshTrigger.value++
            delay(500)
            _isRefreshing.value = false
        }
    }

    fun getLetterDetails(letterId: String) {
        viewModelScope.launch {
            _selectedLetter.value = UiState.Loading
            letterRepository.getLetterById(letterId)
                .catch {
                    _selectedLetter.value = UiState.Error(resourceProvider.getString(R.string.state_error_loading))
                }
                .collect { letter ->
                    _selectedLetter.value = if (letter != null) {
                        UiState.Success(letter)
                    } else {
                        UiState.Error(resourceProvider.getString(R.string.state_no_data))
                    }
                }
        }
    }

    fun saveLetter(
        content: String,
        privacy: LetterPrivacy,
        recipientId: String? = null,
        recipientUsername: String? = null,
        unlockTimestamp: Long? = null,
        pseudonym: String? = null,
        isAskAnoba: Boolean = false,
        audioFile: java.io.File? = null,
        audioDuration: Long? = null,
        onResult: (String?) -> Unit
    ) {
        val authState = authService.authStateFlow.value as? AuthState.Authenticated
        if (authState == null) {
            onResult(null)
            return
        }

        viewModelScope.launch {
            _isSending.value = true
            val newLetterId = letterRepository.saveLetter(
                ownerId = authState.uid,
                username = authState.displayName ?: resourceProvider.getString(R.string.anonymous),
                userProfileImageUrl = authState.photoUrl,
                badgeType = authState.badgeType,
                content = content,
                privacy = privacy.name,
                recipientId = recipientId,
                recipientUsername = recipientUsername,
                unlockTimestamp = unlockTimestamp,
                isPublished = false,
                pseudonym = pseudonym,
                isAskAnoba = isAskAnoba,
                audioFile = audioFile,
                audioDuration = audioDuration
            )
            if (newLetterId != null) {
                analyticsHelper.logEvent("letter_saved") {
                    putString("privacy", privacy.name)
                    putBoolean("has_recipient", recipientId != null)
                    putBoolean("is_time_capsule", unlockTimestamp != null)
                }
            }
            _isSending.value = false
            onResult(newLetterId)
        }
    }

    fun deleteLetter(letterId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = letterRepository.deleteLetter(letterId)
            if (result) {
                analyticsHelper.logEvent("letter_deleted") {
                    putString("letter_id", letterId)
                }
            }
            onComplete(result)
        }
    }

    fun incrementLetterViewCount(letterId: String) {
        val now = System.currentTimeMillis()
        val lastIncremented = viewCountTimestamps[letterId] ?: 0L
        if (now - lastIncremented > 300_000) { // 5 dakikalık debounce
            viewModelScope.launch {
                letterRepository.incrementLetterViewCount(letterId)
                viewCountTimestamps[letterId] = now
            }
        }
    }

    fun incrementLetterShareCount(letterId: String) {
        viewModelScope.launch {
            letterRepository.incrementShareCount(letterId)
        }
    }

    fun unflagLetter(letterId: String) {
        viewModelScope.launch {
            letterRepository.unflagLetter(letterId)
        }
    }

    fun toggleReaction(letterId: String, emoji: String) {
        val authState = authService.authStateFlow.value
        val authenticatedUser = authState as? AuthState.Authenticated ?: return

        val now = System.currentTimeMillis()
        val lastClick = lastReactionTimestamps[letterId] ?: 0L
        if (now - lastClick < 500) return
        lastReactionTimestamps[letterId] = now

        viewModelScope.launch {
            try {
                reactionRepository.toggleReaction(authenticatedUser.uid, letterId, emoji)
                analyticsHelper.logEvent("letter_reaction_toggle") {
                    putString("emoji", emoji)
                    putString("letter_id", letterId)
                }
            } catch (e: Exception) {
                _eventFlow.emit(LetterUiEvent.ShowToast("${resourceProvider.getString(R.string.error_action_failed)}: ${e.localizedMessage}"))
            }
        }
    }

    // 🚀 ADMİN: Bekleyen dertler
    private val _pendingAnobaLetters = MutableStateFlow<UiState<List<LetterModel>>>(UiState.Idle)
    val pendingAnobaLetters: StateFlow<UiState<List<LetterModel>>> = _pendingAnobaLetters.asStateFlow()

    // 🚀 ADMİN: Cevaplanan dertler
    private val _answeredAnobaLetters = MutableStateFlow<UiState<List<LetterModel>>>(UiState.Idle)
    val answeredAnobaLetters: StateFlow<UiState<List<LetterModel>>> = _answeredAnobaLetters.asStateFlow()

    fun loadPendingAnobaLetters() {
        viewModelScope.launch {
            _pendingAnobaLetters.value = UiState.Loading
            letterRepository.getPendingAnobaRequests()
                .catch { e ->
                    Log.e("LetterViewModel", "Error loading pending letters: ${e.localizedMessage}")
                    _pendingAnobaLetters.value = UiState.Error(e.localizedMessage ?: "Hata")
                }
                .collect { list ->
                    _pendingAnobaLetters.value = UiState.Success(list)
                }
        }
    }

    fun loadAnsweredAnobaLetters() {
        viewModelScope.launch {
            _answeredAnobaLetters.value = UiState.Loading
            letterRepository.getAnsweredAnobaRequests()
                .catch { e ->
                    Log.e("LetterViewModel", "Error loading answered letters: ${e.localizedMessage}")
                    _answeredAnobaLetters.value = UiState.Error(e.localizedMessage ?: "Hata")
                }
                .collect { list ->
                    _answeredAnobaLetters.value = UiState.Success(list)
                }
        }
    }

    fun answerLetter(letterId: String, responseText: String, isContentHidden: Boolean, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = letterRepository.sendAnobaResponse(letterId, responseText, isContentHidden)
            if (success) {
                analyticsHelper.logEvent("letter_answered_by_anoba") {
                    putString("letter_id", letterId)
                    putBoolean("is_content_hidden", isContentHidden)
                }
            }
            onResult(success)
        }
    }

    // --- SES KAYIT FONKSİYONLARI ---
    fun startRecording(context: android.content.Context) {
        analyticsHelper.logButtonClick("Start Letter Voice Recording")
        val fileName = "${context.cacheDir.absolutePath}/letter_audio_${System.currentTimeMillis()}.m4a"
        audioFile = java.io.File(fileName)

        try {
            mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                android.media.MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                android.media.MediaRecorder()
            }.apply {
                setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                setOutputFile(fileName)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("LetterViewModel", "Kayıt başlatılamadı: ${e.message}")
            viewModelScope.launch {
                _eventFlow.emit(LetterUiEvent.ShowToast("Mikrofon şu an kullanılamıyor. Lütfen izinleri kontrol edin."))
            }
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    fun stopRecording() {
        analyticsHelper.logButtonClick("Stop Letter Voice Recording")
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e("LetterViewModel", "Kayıt durdurulamadı: ${e.message}")
                release()
            }
        }
        mediaRecorder = null
    }

    fun cancelRecording() {
        analyticsHelper.logButtonClick("Cancel Letter Voice Recording")
        stopRecording()
        audioFile?.delete()
        audioFile = null
    }

    fun getAudioDuration(filePath: String): Long {
        val player = android.media.MediaPlayer()
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
}