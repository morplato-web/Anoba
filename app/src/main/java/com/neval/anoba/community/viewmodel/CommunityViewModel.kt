package com.neval.anoba.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neval.anoba.community.model.ChannelModel
import com.neval.anoba.community.model.ChannelType
import com.neval.anoba.community.model.ServerModel
import com.neval.anoba.community.model.VoiceMemberModel
import com.neval.anoba.community.repository.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CommunityUiState(
    val selectedServerId: String = "",
    val selectedChannelId: String = "",
    val connectedVoiceChannelId: String = "",
    val isMuted: Boolean = false,
    val isDeafened: Boolean = false,
    val isSpeaking: Boolean = false,
    val isPushToTalkEnabled: Boolean = false,
    val pushToTalkKeyName: String = "Caps Lock"
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CommunityViewModel(
    private val repository: CommunityRepository = CommunityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    // Canlı Sunucu Listesi
    val servers: StateFlow<List<ServerModel>> = repository.getServersStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Seçili Sunucunun Kanalları
    val channels: StateFlow<List<ChannelModel>> = _uiState
        .flatMapLatest { state ->
            if (state.selectedServerId.isNotBlank()) {
                repository.getChannelsStream(state.selectedServerId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Seçili Ses Kanalının Üyeleri
    val voiceMembers: StateFlow<List<VoiceMemberModel>> = _uiState
        .flatMapLatest { state ->
            val voiceChanId = if (state.connectedVoiceChannelId.isNotBlank()) {
                state.connectedVoiceChannelId
            } else {
                state.selectedChannelId
            }
            if (state.selectedServerId.isNotBlank() && voiceChanId.isNotBlank()) {
                repository.getVoiceMembersStream(state.selectedServerId, voiceChanId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // İlk açılışta örnek varsayılan sunucu ve kanalları Firestore'a ekle (eğer yoksa)
        viewModelScope.launch {
            repository.createDefaultSampleServersIfEmpty()
        }

        // Sunucular yüklendiğinde otomatik ilk sunucuyu ve ilk kanalı seç
        viewModelScope.launch {
            servers.collect { serverList ->
                if (serverList.isNotEmpty() && _uiState.value.selectedServerId.isBlank()) {
                    selectServer(serverList.first().id)
                }
            }
        }
    }

    fun selectServer(serverId: String) {
        _uiState.value = _uiState.value.copy(
            selectedServerId = serverId,
            selectedChannelId = ""
        )
        // Kanal listesinden ilk kanalı otomatik seç
        viewModelScope.launch {
            channels.collect { channelList ->
                if (channelList.isNotEmpty() && _uiState.value.selectedChannelId.isBlank()) {
                    _uiState.value = _uiState.value.copy(selectedChannelId = channelList.first().id)
                }
            }
        }
    }

    fun selectChannel(channel: ChannelModel) {
        _uiState.value = _uiState.value.copy(selectedChannelId = channel.id)
    }

    fun joinVoiceChannel(channel: ChannelModel, member: VoiceMemberModel) {
        if (channel.type != ChannelType.VOICE) return
        viewModelScope.launch {
            // Eğer önceden bağlı olunan başka kanal varsa önce oradan ayrıl
            val currentServerId = _uiState.value.selectedServerId
            val currentConnChan = _uiState.value.connectedVoiceChannelId
            if (currentConnChan.isNotBlank() && currentConnChan != channel.id) {
                repository.leaveVoiceChannel(currentServerId, currentConnChan, member.userId)
            }

            _uiState.value = _uiState.value.copy(
                connectedVoiceChannelId = channel.id,
                selectedChannelId = channel.id
            )

            val voiceMember = member.copy(
                serverId = currentServerId,
                channelId = channel.id,
                isMuted = _uiState.value.isMuted,
                isDeafened = _uiState.value.isDeafened
            )
            repository.joinVoiceChannel(currentServerId, channel.id, voiceMember)
        }
    }

    fun leaveVoiceChannel(userId: String) {
        val currentServerId = _uiState.value.selectedServerId
        val currentConnChan = _uiState.value.connectedVoiceChannelId
        if (currentServerId.isBlank() || currentConnChan.isBlank() || userId.isBlank()) return

        viewModelScope.launch {
            repository.leaveVoiceChannel(currentServerId, currentConnChan, userId)
            _uiState.value = _uiState.value.copy(connectedVoiceChannelId = "")
        }
    }

    fun toggleMute(userId: String) {
        val newMute = !_uiState.value.isMuted
        _uiState.value = _uiState.value.copy(isMuted = newMute)

        val currentServerId = _uiState.value.selectedServerId
        val currentConnChan = _uiState.value.connectedVoiceChannelId
        if (currentConnChan.isNotBlank()) {
            viewModelScope.launch {
                repository.updateVoiceState(
                    serverId = currentServerId,
                    channelId = currentConnChan,
                    userId = userId,
                    isMuted = newMute,
                    isDeafened = _uiState.value.isDeafened,
                    isSpeaking = _uiState.value.isSpeaking
                )
            }
        }
    }

    fun toggleDeafen(userId: String) {
        val newDeaf = !_uiState.value.isDeafened
        _uiState.value = _uiState.value.copy(isDeafened = newDeaf)

        val currentServerId = _uiState.value.selectedServerId
        val currentConnChan = _uiState.value.connectedVoiceChannelId
        if (currentConnChan.isNotBlank()) {
            viewModelScope.launch {
                repository.updateVoiceState(
                    serverId = currentServerId,
                    channelId = currentConnChan,
                    userId = userId,
                    isMuted = _uiState.value.isMuted,
                    isDeafened = newDeaf,
                    isSpeaking = _uiState.value.isSpeaking
                )
            }
        }
    }

    fun togglePushToTalkMode() {
        val nextState = !_uiState.value.isPushToTalkEnabled
        _uiState.value = _uiState.value.copy(
            isPushToTalkEnabled = nextState,
            isMuted = nextState // Push-To-Talk açıldığında varsayılan olarak basana kadar mikrofon kapalıdır
        )
    }

    fun onPushToTalkKeyPressed(userId: String) {
        if (!_uiState.value.isPushToTalkEnabled) return
        _uiState.value = _uiState.value.copy(isMuted = false, isSpeaking = true)
        val currentServerId = _uiState.value.selectedServerId
        val currentConnChan = _uiState.value.connectedVoiceChannelId
        if (currentConnChan.isNotBlank()) {
            viewModelScope.launch {
                repository.updateVoiceState(currentServerId, currentConnChan, userId, isMuted = false, isDeafened = _uiState.value.isDeafened, isSpeaking = true)
            }
        }
    }

    fun onPushToTalkKeyReleased(userId: String) {
        if (!_uiState.value.isPushToTalkEnabled) return
        _uiState.value = _uiState.value.copy(isMuted = true, isSpeaking = false)
        val currentServerId = _uiState.value.selectedServerId
        val currentConnChan = _uiState.value.connectedVoiceChannelId
        if (currentConnChan.isNotBlank()) {
            viewModelScope.launch {
                repository.updateVoiceState(currentServerId, currentConnChan, userId, isMuted = true, isDeafened = _uiState.value.isDeafened, isSpeaking = false)
            }
        }
    }
}
