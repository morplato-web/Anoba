package com.neval.anoba.community.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.community.engine.CommunityVoiceEngine
import com.neval.anoba.community.model.VoiceMemberModel
import com.neval.anoba.community.viewmodel.CommunityViewModel
import com.neval.anoba.login.state.AuthState
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHubContainerScreen(
    onNavigateBack: () -> Unit,
    viewModel: CommunityViewModel = viewModel(),
    authService: AuthServiceInterface = koinInject()
) {
    val context = LocalContext.current
    val voiceEngine = remember { CommunityVoiceEngine(context) }

    DisposableEffect(Unit) {
        onDispose {
            voiceEngine.destroy()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val servers by viewModel.servers.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val voiceMembers by viewModel.voiceMembers.collectAsState()
    val authState by authService.authStateFlow.collectAsState()

    val authenticatedUser = authState as? AuthState.Authenticated
    val currentUserId = authenticatedUser?.uid ?: "guest_${System.currentTimeMillis()}"
    val currentUsername = authenticatedUser?.displayName?.ifBlank { "Oyuncu" } ?: "Oyuncu"
    val currentUserAvatar = authenticatedUser?.photoUrl ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Anoba FreeRoom 🎮",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1F22)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF313338))
        ) {
            CommunityHubScreen(
                servers = servers,
                channels = channels,
                voiceMembers = voiceMembers,
                selectedServerId = uiState.selectedServerId,
                selectedChannelId = uiState.selectedChannelId,
                currentUserId = currentUserId,
                currentUsername = currentUsername,
                currentUserAvatar = currentUserAvatar,
                isPushToTalkEnabled = uiState.isPushToTalkEnabled,
                onServerSelected = { serverId ->
                    viewModel.selectServer(serverId)
                },
                onChannelSelected = { channel ->
                    viewModel.selectChannel(channel)
                },
                onToggleMute = {
                    val nextMute = !uiState.isMuted
                    voiceEngine.setMuted(nextMute)
                    viewModel.toggleMute(currentUserId)
                },
                onToggleDeafen = {
                    val nextDeafen = !uiState.isDeafened
                    voiceEngine.setDeafened(nextDeafen)
                    viewModel.toggleDeafen(currentUserId)
                },
                onTogglePushToTalk = {
                    viewModel.togglePushToTalkMode()
                },
                onLeaveVoice = {
                    voiceEngine.leaveVoiceChannel()
                    viewModel.leaveVoiceChannel(currentUserId)
                },
                onJoinVoice = { channel ->
                    val member = VoiceMemberModel(
                        userId = currentUserId,
                        username = currentUsername,
                        userAvatar = currentUserAvatar
                    )
                    voiceEngine.joinVoiceChannel("freeroom_${channel.id}", currentUserId.hashCode())
                    viewModel.joinVoiceChannel(channel, member)
                }
            )
        }
    }
}
