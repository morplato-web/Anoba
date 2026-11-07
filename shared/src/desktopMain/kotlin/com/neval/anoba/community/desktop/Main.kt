package com.neval.anoba.community.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import coil3.compose.AsyncImage
import com.neval.anoba.community.model.ChannelModel
import com.neval.anoba.community.model.ChannelType
import com.neval.anoba.community.model.ChatMessageModel
import com.neval.anoba.community.model.ServerModel
import com.neval.anoba.community.model.VoiceMemberModel
import com.neval.anoba.community.ui.SharedCommunityHubScreen
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

data class AvatarPreset(
    val id: String,
    val name: String,
    val imageUrl: String
)

val defaultAvatarPresets = listOf(
    AvatarPreset("1", "Siber Savaşçı 🎮", "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150"),
    AvatarPreset("2", "Ejderha Operatörü 🐉", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"),
    AvatarPreset("3", "Neon Ejder ⚡", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150"),
    AvatarPreset("4", "Gölge Savaşçı ⚔️", "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150"),
    AvatarPreset("5", "Kral Muhafız 🛡️", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"),
    AvatarPreset("6", "Uzay Kaptanı 🚀", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"),
    AvatarPreset("7", "Piksel Kral 👾", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150"),
    AvatarPreset("8", "Keskin Nişancı 🦅", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150"),
    AvatarPreset("9", "Siber Tilki 🦊", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150"),
    AvatarPreset("10", "Kral Anoba 👑", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150")
)

fun main() = application {
    val appIcon = remember {
        try {
            val fileCandidates = listOf(
                File("shared/src/desktopMain/resources/anoba_logo.png"),
                File("src/desktopMain/resources/anoba_logo.png"),
                File("anoba_logo.png")
            )
            val existingFile = fileCandidates.firstOrNull { it.exists() }
            if (existingFile != null) {
                ImageIO.read(existingFile)
            } else {
                val stream: InputStream? = Thread.currentThread().contextClassLoader.getResourceAsStream("anoba_logo.png")
                    ?: object {}.javaClass.getResourceAsStream("/anoba_logo.png")
                stream?.use { ImageIO.read(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = DesktopWindowConfig.WINDOW_TITLE,
        icon = appIcon?.let { BitmapPainter(it.toComposeImageBitmap()) },
        state = rememberWindowState(
            width = DesktopWindowConfig.DEFAULT_WIDTH,
            height = DesktopWindowConfig.DEFAULT_HEIGHT
        )
    ) {
        LaunchedEffect(appIcon) {
            if (appIcon != null) {
                window.iconImage = appIcon
            }
        }

        MaterialTheme(
            colorScheme = darkColorScheme(
                background = Color(0xFF313338),
                surface = Color(0xFF2B2D31)
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF313338)
            ) {
                FreeHubDesktopApp()
            }
        }
    }
}

@Composable
fun FreeHubDesktopApp() {
    val defaultServer1Id = "W63WMEO14jCdnqB6XF2I"
    val defaultServer2Id = "V6b0gILICkuRN5yD9ZLo"

    val defaultBilekKadroId = "McrpiNRjYsDeUbsXqqeW"
    val defaultPubgSquadId = "xZ57ayxWJ2vMljuMbnkt"
    val defaultKurucuOdasiId = "T3i4nDF9S0HXWJn1WHyV"
    val defaultGenelSohbetId = "olZu2DgiD0kmvl5mm3wC"
    val defaultOyunDuyuruId = "0sowTwchnlYF3JsWC5An"

    val defaultServers = remember {
        listOf(
            ServerModel(id = defaultServer1Id, name = "Anoba Gaming 🎮", description = "PUBG & Valorant", memberCount = 128),
            ServerModel(id = defaultServer2Id, name = "Serbest Alan ☕", description = "Sohbet & Müzik", memberCount = 64)
        )
    }

    var servers by remember { mutableStateOf(defaultServers) }
    var selectedServerId by remember { mutableStateOf(defaultServer1Id) }

    val defaultServer1Channels = remember {
        listOf(
            ChannelModel(id = defaultGenelSohbetId, serverId = defaultServer1Id, name = "genel-sohbet", type = ChannelType.TEXT, position = 0),
            ChannelModel(id = defaultOyunDuyuruId, serverId = defaultServer1Id, name = "oyun-duyuru", type = ChannelType.TEXT, position = 1),
            ChannelModel(id = defaultBilekKadroId, serverId = defaultServer1Id, name = "Bilek Kadro", type = ChannelType.VOICE, position = 2),
            ChannelModel(id = defaultPubgSquadId, serverId = defaultServer1Id, name = "PUBG Squad", type = ChannelType.VOICE, position = 3),
            ChannelModel(id = defaultKurucuOdasiId, serverId = defaultServer1Id, name = "Kurucu Odası", type = ChannelType.VOICE, position = 4)
        )
    }

    val defaultServer2Channels = remember {
        listOf(
            ChannelModel(id = "c6", serverId = defaultServer2Id, name = "muhabbet", type = ChannelType.TEXT, position = 0),
            ChannelModel(id = "c7", serverId = defaultServer2Id, name = "Müzik & Ses", type = ChannelType.VOICE, position = 1)
        )
    }

    var channels by remember(selectedServerId) {
        mutableStateOf(if (selectedServerId == defaultServer1Id) defaultServer1Channels else defaultServer2Channels)
    }

    var selectedChannelId by remember { mutableStateOf(defaultGenelSohbetId) }
    var connectedChannelId by remember { mutableStateOf(defaultBilekKadroId) }
    var isMuted by remember { mutableStateOf(false) }
    var isDeafened by remember { mutableStateOf(false) }
    var isPushToTalkEnabled by remember { mutableStateOf(false) }
    var isMicSpeaking by remember { mutableStateOf(false) }

    var chatMessages by remember { mutableStateOf(emptyList<ChatMessageModel>()) }

    var showCreateServerDialog by remember { mutableStateOf(false) }
    var showCreateChannelDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Persistent Unique User ID per PC installation (UUID)
    val currentUserId = remember {
        try {
            val prefsFile = File(System.getProperty("user.home"), ".anoba_user_id")
            if (prefsFile.exists() && prefsFile.readText().isNotBlank()) {
                prefsFile.readText().trim()
            } else {
                val newId = "pc_" + java.util.UUID.randomUUID().toString().replace("-", "").take(8)
                prefsFile.writeText(newId)
                newId
            }
        } catch (_: Exception) {
            "pc_" + java.util.UUID.randomUUID().toString().replace("-", "").take(8)
        }
    }

    val systemUsername = remember {
        val envUser = System.getProperty("user.name")
        if (envUser.isNullOrBlank() || envUser.lowercase() == "pc" || envUser.lowercase() == "user" || envUser.lowercase() == "admin") {
            "Oyuncu_" + currentUserId.takeLast(4)
        } else {
            envUser
        }
    }

    // Load saved local user profile (username, avatarUrl, pttKeyName, pttKeyCode)
    val savedProfile = remember { loadLocalProfile(systemUsername) }
    var currentUsername by remember { mutableStateOf(savedProfile.first) }
    var currentUserAvatar by remember { mutableStateOf(savedProfile.second) }
    var pttKeyName by remember { mutableStateOf(savedProfile.third) }
    var pttKeyCode by remember { mutableStateOf(savedProfile.fourth) }

    var showProfileDialog by remember { mutableStateOf(false) }

    var liveVoiceMembers by remember { mutableStateOf(emptyList<VoiceMemberModel>()) }
    val client = remember { HttpClient() }

    // Desktop Microphone Audio Recorder with Decibel Level Speech Detection (triggers green ring)
    val audioRecorder = remember {
        DesktopAudioRecorder { isSpeaking ->
            isMicSpeaking = isSpeaking
        }
    }

    // Start/Stop mic audio recorder depending on voice connection and mute status
    DisposableEffect(connectedChannelId, isMuted) {
        if (connectedChannelId.isNotBlank() && !isMuted) {
            audioRecorder.startRecording()
        } else {
            audioRecorder.stopRecording()
        }
        onDispose {
            audioRecorder.stopRecording()
        }
    }

    // Global Key Hook Listener for Push-To-Talk (PTT) Mode (AWT + System Listener)
    DisposableEffect(pttKeyCode, isPushToTalkEnabled) {
        val dispatcher = java.awt.KeyEventDispatcher { event ->
            if (isPushToTalkEnabled && event.keyCode == pttKeyCode) {
                if (event.id == java.awt.event.KeyEvent.KEY_PRESSED) {
                    isMuted = false
                } else if (event.id == java.awt.event.KeyEvent.KEY_RELEASED) {
                    isMuted = true
                }
            }
            false
        }
        val toolkitListener = java.awt.event.AWTEventListener { event ->
            if (isPushToTalkEnabled && event is java.awt.event.KeyEvent && event.keyCode == pttKeyCode) {
                if (event.id == java.awt.event.KeyEvent.KEY_PRESSED) {
                    isMuted = false
                } else if (event.id == java.awt.event.KeyEvent.KEY_RELEASED) {
                    isMuted = true
                }
            }
        }

        java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
        try {
            java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(
                toolkitListener,
                java.awt.AWTEvent.KEY_EVENT_MASK
            )
        } catch (_: Exception) {}

        onDispose {
            java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
            try {
                java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(toolkitListener)
            } catch (_: Exception) {}
        }
    }

    // Fetch Live Servers from Firestore REST API on Launch
    LaunchedEffect(Unit) {
        try {
            val getServersUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers"
            val respText = client.get(getServersUrl).bodyAsText()
            val fetchedServers = parseServersFromResponse(respText)
            if (fetchedServers.isNotEmpty()) {
                servers = fetchedServers
                if (fetchedServers.none { it.id == selectedServerId }) {
                    selectedServerId = fetchedServers.first().id
                }
            }
        } catch (_: Exception) {}
    }

    // Fetch Live Channels for Selected Server from Firestore REST API
    LaunchedEffect(selectedServerId) {
        try {
            val getChannelsUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$selectedServerId/channels"
            val respText = client.get(getChannelsUrl).bodyAsText()
            val fetchedChannels = parseChannelsFromResponse(selectedServerId, respText)
            if (fetchedChannels.isNotEmpty()) {
                channels = fetchedChannels
            } else if (selectedServerId == defaultServer1Id) {
                channels = defaultServer1Channels
            } else {
                channels = defaultServer2Channels
            }
        } catch (_: Exception) {
            channels = if (selectedServerId == defaultServer1Id) defaultServer1Channels else defaultServer2Channels
        }
    }

    // Fetch Messages when Text Channel is selected
    LaunchedEffect(selectedServerId, selectedChannelId) {
        val selectedChanObj = channels.find { it.id == selectedChannelId }
        if (selectedChanObj?.type == ChannelType.TEXT && selectedServerId.isNotBlank() && selectedChannelId.isNotBlank()) {
            try {
                val getMsgsUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$selectedServerId/channels/$selectedChannelId/messages"
                val respText = client.get(getMsgsUrl).bodyAsText()
                chatMessages = parseMessagesFromResponse(selectedServerId, selectedChannelId, respText)
            } catch (_: Exception) {}
        }
    }

    // Track previous connected channel to cleanly delete presence on channel change or exit
    var lastActiveServerId by remember { mutableStateOf(selectedServerId) }
    var lastActiveChannelId by remember { mutableStateOf(connectedChannelId) }

    // Cleanup on DisposableEffect exit
    DisposableEffect(Unit) {
        onDispose {
            if (lastActiveServerId.isNotBlank() && lastActiveChannelId.isNotBlank() && currentUserId.isNotBlank()) {
                coroutineScope.launch {
                    leaveVoiceMember(client, lastActiveServerId, lastActiveChannelId, currentUserId)
                }
            }
        }
    }

    var lastPatchTime by remember { mutableStateOf(0L) }
    var stateChangeTrigger by remember { mutableStateOf(0) }

    // Live Sync with Firestore via REST API (Event-driven instant PATCH + 12s Heartbeat, 2s GET polling)
    LaunchedEffect(selectedServerId, selectedChannelId, connectedChannelId, isMuted, isDeafened, isMicSpeaking, currentUsername, currentUserAvatar, stateChangeTrigger) {
        val targetChan = if (connectedChannelId.isNotBlank()) connectedChannelId else selectedChannelId
        val targetServ = selectedServerId

        // If connected channel changed, delete presence from old channel
        if (connectedChannelId != lastActiveChannelId || selectedServerId != lastActiveServerId) {
            if (lastActiveServerId.isNotBlank() && lastActiveChannelId.isNotBlank()) {
                leaveVoiceMember(client, lastActiveServerId, lastActiveChannelId, currentUserId)
            }
            lastActiveServerId = selectedServerId
            lastActiveChannelId = connectedChannelId
        }

        while (isActive) {
            try {
                val now = System.currentTimeMillis()
                val timeSinceLastPatch = now - lastPatchTime

                // 1. PATCH presence on state change or 12-second heartbeat (saves 80%+ Firestore writes)
                if (connectedChannelId.isNotBlank() && targetServ.isNotBlank() && (timeSinceLastPatch > 12000L || stateChangeTrigger > 0)) {
                    lastPatchTime = now
                    stateChangeTrigger = 0
                    val patchUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$targetServ/channels/$connectedChannelId/voice_members/$currentUserId"
                    val jsonBody = """
                        {
                          "fields": {
                            "userId": { "stringValue": "$currentUserId" },
                            "username": { "stringValue": "$currentUsername" },
                            "userAvatar": { "stringValue": "$currentUserAvatar" },
                            "serverId": { "stringValue": "$targetServ" },
                            "channelId": { "stringValue": "$connectedChannelId" },
                            "isMuted": { "booleanValue": $isMuted },
                            "muted": { "booleanValue": $isMuted },
                            "isDeafened": { "booleanValue": $isDeafened },
                            "deafened": { "booleanValue": $isDeafened },
                            "isSpeaking": { "booleanValue": ${!isMuted && isMicSpeaking} },
                            "speaking": { "booleanValue": ${!isMuted && isMicSpeaking} },
                            "isStreaming": { "booleanValue": false },
                            "joinedAt": { "integerValue": "$now" }
                          }
                        }
                    """.trimIndent()
                    client.patch(patchUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(jsonBody)
                    }
                }

                // 2. GET current voice members (fast 2s polling for room reading)
                if (targetServ.isNotBlank() && targetChan.isNotBlank()) {
                    val getUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$targetServ/channels/$targetChan/voice_members"
                    val responseText = client.get(getUrl).bodyAsText()
                    val fetchedMembers = parseVoiceMembersFromResponse(responseText)

                    if (fetchedMembers.isNotEmpty()) {
                        liveVoiceMembers = fetchedMembers
                    } else if (connectedChannelId.isNotBlank()) {
                        liveVoiceMembers = listOf(
                            VoiceMemberModel(
                                userId = currentUserId,
                                username = currentUsername,
                                userAvatar = currentUserAvatar,
                                serverId = targetServ,
                                channelId = connectedChannelId,
                                isSpeaking = !isMuted && isMicSpeaking,
                                isMuted = isMuted,
                                isDeafened = isDeafened
                            )
                        )
                    } else {
                        liveVoiceMembers = emptyList()
                    }
                }

                // 3. GET current text messages if viewing a text channel
                val selChan = channels.find { it.id == selectedChannelId }
                if (selChan?.type == ChannelType.TEXT && targetServ.isNotBlank() && selectedChannelId.isNotBlank()) {
                    val getMsgsUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$targetServ/channels/$selectedChannelId/messages"
                    val respText = client.get(getMsgsUrl).bodyAsText()
                    val fetchedMsgs = parseMessagesFromResponse(targetServ, selectedChannelId, respText)
                    chatMessages = fetchedMsgs
                }
            } catch (e: Exception) {
                if (connectedChannelId.isNotBlank()) {
                    liveVoiceMembers = listOf(
                        VoiceMemberModel(
                            userId = currentUserId,
                            username = currentUsername,
                            userAvatar = currentUserAvatar,
                            serverId = targetServ,
                            channelId = connectedChannelId,
                            isSpeaking = !isMuted && isMicSpeaking,
                            isMuted = isMuted,
                            isDeafened = isDeafened
                        )
                    )
                }
            }
            delay(2000)
        }
    }

    if (showProfileDialog) {
        ProfileEditDialog(
            currentUsername = currentUsername,
            currentUserAvatar = currentUserAvatar,
            currentPttKeyName = pttKeyName,
            currentPttKeyCode = pttKeyCode,
            onDismiss = { showProfileDialog = false },
            onSaveProfile = { newName, newAvatar, newPttKeyName, newPttKeyCode ->
                currentUsername = newName
                currentUserAvatar = newAvatar
                pttKeyName = newPttKeyName
                pttKeyCode = newPttKeyCode
                saveLocalProfile(newName, newAvatar, newPttKeyName, newPttKeyCode)
                showProfileDialog = false
                stateChangeTrigger++

                if (connectedChannelId.isNotBlank() && selectedServerId.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            val patchUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$selectedServerId/channels/$connectedChannelId/voice_members/$currentUserId"
                            val jsonBody = """
                                {
                                  "fields": {
                                    "userId": { "stringValue": "$currentUserId" },
                                    "username": { "stringValue": "$newName" },
                                    "userAvatar": { "stringValue": "$newAvatar" },
                                    "serverId": { "stringValue": "$selectedServerId" },
                                    "channelId": { "stringValue": "$connectedChannelId" },
                                    "isMuted": { "booleanValue": $isMuted },
                                    "muted": { "booleanValue": $isMuted },
                                    "isDeafened": { "booleanValue": $isDeafened },
                                    "deafened": { "booleanValue": $isDeafened },
                                    "isSpeaking": { "booleanValue": ${!isMuted && isMicSpeaking} },
                                    "speaking": { "booleanValue": ${!isMuted && isMicSpeaking} },
                                    "isStreaming": { "booleanValue": false },
                                    "joinedAt": { "integerValue": "${System.currentTimeMillis()}" }
                                  }
                                }
                            """.trimIndent()
                            client.patch(patchUrl) {
                                contentType(ContentType.Application.Json)
                                setBody(jsonBody)
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        )
    }

    if (showCreateServerDialog) {
        CreateServerDialog(
            onDismiss = { showCreateServerDialog = false },
            onCreateServer = { name, desc ->
                showCreateServerDialog = false
                coroutineScope.launch {
                    val newServerId = createServerRest(client, name, desc)
                    if (newServerId.isNotBlank()) {
                        selectedServerId = newServerId
                        try {
                            val getServersUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers"
                            val respText = client.get(getServersUrl).bodyAsText()
                            val fetchedServers = parseServersFromResponse(respText)
                            if (fetchedServers.isNotEmpty()) {
                                servers = fetchedServers
                            }
                        } catch (_: Exception) {}

                        try {
                            val getChannelsUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$newServerId/channels"
                            val respText = client.get(getChannelsUrl).bodyAsText()
                            val fetchedChannels = parseChannelsFromResponse(newServerId, respText)
                            if (fetchedChannels.isNotEmpty()) {
                                channels = fetchedChannels
                                selectedChannelId = fetchedChannels.first().id
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        )
    }

    if (showCreateChannelDialog) {
        CreateChannelDialog(
            onDismiss = { showCreateChannelDialog = false },
            onCreateChannel = { name, type ->
                showCreateChannelDialog = false
                if (selectedServerId.isNotBlank()) {
                    coroutineScope.launch {
                        val pos = channels.size
                        val newChanId = createChannelRest(client, selectedServerId, name, type, pos)
                        try {
                            val getChannelsUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$selectedServerId/channels"
                            val respText = client.get(getChannelsUrl).bodyAsText()
                            val fetchedChannels = parseChannelsFromResponse(selectedServerId, respText)
                            if (fetchedChannels.isNotEmpty()) {
                                channels = fetchedChannels
                                if (newChanId.isNotBlank()) {
                                    selectedChannelId = newChanId
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        )
    }

    SharedCommunityHubScreen(
        servers = servers,
        channels = channels,
        voiceMembers = liveVoiceMembers,
        selectedServerId = selectedServerId,
        selectedChannelId = selectedChannelId,
        currentUserId = currentUserId,
        currentUsername = currentUsername,
        currentUserAvatar = currentUserAvatar,
        chatMessages = chatMessages,
        isPushToTalkEnabled = isPushToTalkEnabled,
        onTogglePushToTalk = {
            isPushToTalkEnabled = !isPushToTalkEnabled
            if (isPushToTalkEnabled) {
                isMuted = true
            }
            stateChangeTrigger++
        },
        onSendChatMessage = { text ->
            if (selectedServerId.isNotBlank() && selectedChannelId.isNotBlank() && text.isNotBlank()) {
                coroutineScope.launch {
                    sendChatMessageRest(
                        client = client,
                        serverId = selectedServerId,
                        channelId = selectedChannelId,
                        senderId = currentUserId,
                        senderName = currentUsername,
                        senderAvatar = currentUserAvatar,
                        text = text
                    )
                    try {
                        val getMsgsUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$selectedServerId/channels/$selectedChannelId/messages"
                        val respText = client.get(getMsgsUrl).bodyAsText()
                        chatMessages = parseMessagesFromResponse(selectedServerId, selectedChannelId, respText)
                    } catch (_: Exception) {}
                }
            }
        },
        onAddServerClicked = { showCreateServerDialog = true },
        onAddChannelClicked = { showCreateChannelDialog = true },
        onServerSelected = { serverId ->
            selectedServerId = serverId
            val firstChan = channels.firstOrNull { it.serverId == serverId }?.id ?: defaultGenelSohbetId
            selectedChannelId = firstChan
        },
        onChannelSelected = { channel ->
            selectedChannelId = channel.id
            if (channel.type == ChannelType.VOICE) {
                connectedChannelId = channel.id
            }
        },
        onToggleMute = {
            isMuted = !isMuted
            stateChangeTrigger++
        },
        onToggleDeafen = {
            isDeafened = !isDeafened
            stateChangeTrigger++
        },
        onLeaveVoice = {
            if (selectedServerId.isNotBlank() && connectedChannelId.isNotBlank()) {
                val oldServ = selectedServerId
                val oldChan = connectedChannelId
                connectedChannelId = ""
                coroutineScope.launch {
                    leaveVoiceMember(client, oldServ, oldChan, currentUserId)
                }
            } else {
                connectedChannelId = ""
            }
            stateChangeTrigger++
        },
        onJoinVoice = { channel ->
            connectedChannelId = channel.id
            stateChangeTrigger++
        },
        onOpenProfileSettings = { showProfileDialog = true }
    )
}

// Desktop Microphone Audio Recorder with Decibel Level Speech Detection
private class DesktopAudioRecorder(
    private val onSpeakingChanged: (Boolean) -> Unit
) {
    private var recordingThread: Thread? = null
    private var isRecording = false

    fun startRecording() {
        if (isRecording) return
        isRecording = true
        recordingThread = Thread {
            try {
                val format = javax.sound.sampled.AudioFormat(16000f, 16, 1, true, false)
                val info = javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine::class.java, format)
                if (javax.sound.sampled.AudioSystem.isLineSupported(info)) {
                    val line = javax.sound.sampled.AudioSystem.getLine(info) as javax.sound.sampled.TargetDataLine
                    line.open(format)
                    line.start()

                    val buffer = ByteArray(1024)
                    var wasSpeaking = false

                    while (isRecording && !Thread.currentThread().isInterrupted) {
                        val bytesRead = line.read(buffer, 0, buffer.size)
                        if (bytesRead > 0) {
                            var sum = 0.0
                            for (i in 0 until bytesRead step 2) {
                                if (i + 1 < bytesRead) {
                                    val sample = (buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)
                                    sum += sample * sample
                                }
                            }
                            val rms = Math.sqrt(sum / (bytesRead / 2))
                            val isSpeakingNow = rms > 800.0 // Decibel/volume speech threshold

                            if (isSpeakingNow != wasSpeaking) {
                                wasSpeaking = isSpeakingNow
                                onSpeakingChanged(isSpeakingNow)
                            }
                        }
                    }
                    line.stop()
                    line.close()
                }
            } catch (_: Exception) {}
        }
        recordingThread?.isDaemon = true
        recordingThread?.start()
    }

    fun stopRecording() {
        isRecording = false
        recordingThread?.interrupt()
        recordingThread = null
        onSpeakingChanged(false)
    }
}

@Composable
fun CreateServerDialog(
    onDismiss: () -> Unit,
    onCreateServer: (name: String, description: String) -> Unit
) {
    var serverName by remember { mutableStateOf("") }
    var serverDesc by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(440.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31)),
            border = BorderStroke(1.dp, Color(0xFF3F4248))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "➕ Yeni Sunucu Oluştur",
                        color = Color(0xFFF2F3F5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFF949BA4)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sunucu Adı:",
                    color = Color(0xFFF2F3F5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Örn: Kurtlar Vadisi Gaming 🎮", color = Color(0xFF949BA4)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color(0xFF3F4248),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sunucu Açıklaması / Slogan:",
                    color = Color(0xFFF2F3F5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = serverDesc,
                    onValueChange = { serverDesc = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Örn: PUBG & Valorant klan odası", color = Color(0xFF949BA4)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color(0xFF3F4248),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF313338))
                    ) {
                        Text(text = "İptal", color = Color(0xFF949BA4))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (serverName.isNotBlank()) {
                                onCreateServer(serverName.trim(), serverDesc.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                    ) {
                        Text(
                            text = "Sunucuyu Kur",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateChannelDialog(
    onDismiss: () -> Unit,
    onCreateChannel: (name: String, type: ChannelType) -> Unit
) {
    var channelName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ChannelType.TEXT) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31)),
            border = BorderStroke(1.dp, Color(0xFF3F4248))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "➕ Yeni Kanal Oluştur",
                        color = Color(0xFFF2F3F5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFF949BA4)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kanal Adı:",
                    color = Color(0xFFF2F3F5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = channelName,
                    onValueChange = { channelName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Örn: oyun-duyuru veya VIP Ses", color = Color(0xFF949BA4)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color(0xFF3F4248),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kanal Türü Seçin:",
                    color = Color(0xFFF2F3F5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isTextSelected = selectedType == ChannelType.TEXT
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isTextSelected) 2.dp else 1.dp,
                                color = if (isTextSelected) Color(0xFF22C55E) else Color(0xFF3F4248),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedType = ChannelType.TEXT },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTextSelected) Color(0xFF1E1F22) else Color(0xFF2B2D31)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "💬", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Metin Kanalı",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    val isVoiceSelected = selectedType == ChannelType.VOICE
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isVoiceSelected) 2.dp else 1.dp,
                                color = if (isVoiceSelected) Color(0xFF22C55E) else Color(0xFF3F4248),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedType = ChannelType.VOICE },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isVoiceSelected) Color(0xFF1E1F22) else Color(0xFF2B2D31)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔊", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ses Odası",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF313338))
                    ) {
                        Text(text = "İptal", color = Color(0xFF949BA4))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (channelName.isNotBlank()) {
                                onCreateChannel(channelName.trim(), selectedType)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                    ) {
                        Text(
                            text = "Kanal Oluştur",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileEditDialog(
    currentUsername: String,
    currentUserAvatar: String,
    currentPttKeyName: String,
    currentPttKeyCode: Int,
    onDismiss: () -> Unit,
    onSaveProfile: (newUsername: String, newAvatarUrl: String, newPttKeyName: String, newPttKeyCode: Int) -> Unit
) {
    var tempName by remember { mutableStateOf(currentUsername) }
    var tempAvatar by remember { mutableStateOf(currentUserAvatar.ifBlank { defaultAvatarPresets.first().imageUrl }) }
    var tempPttKeyName by remember { mutableStateOf(currentPttKeyName) }
    var tempPttKeyCode by remember { mutableStateOf(currentPttKeyCode) }
    var isRecordingKey by remember { mutableStateOf(false) }

    if (isRecordingKey) {
        DisposableEffect(Unit) {
            val dispatcher = java.awt.KeyEventDispatcher { event ->
                if (event.id == java.awt.event.KeyEvent.KEY_PRESSED) {
                    val code = event.keyCode
                    if (code == java.awt.event.KeyEvent.VK_ESCAPE) {
                        isRecordingKey = false
                    } else {
                        val keyName = java.awt.event.KeyEvent.getKeyText(code)
                        tempPttKeyName = keyName
                        tempPttKeyCode = code
                        isRecordingKey = false
                    }
                    true
                } else false
            }
            java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
            onDispose {
                java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(480.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31)),
            border = BorderStroke(1.dp, Color(0xFF3F4248))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ Anoba FreeHub Ayarları",
                        color = Color(0xFFF2F3F5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFF949BA4)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Kullanıcı Adı Input
                Text(
                    text = "Kullanıcı Adı / Oyuncu İsim:",
                    color = Color(0xFFF2F3F5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Örn: EfsaneOyuncu", color = Color(0xFF949BA4)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color(0xFF3F4248),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bas-Konuş (PTT) Tuş Ayarı
                Text(
                    text = "⌨️ Bas-Konuş (PTT) Özel Tuş Ataması:",
                    color = Color(0xFFF2F3F5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E1F22))
                        .border(1.dp, if (isRecordingKey) Color(0xFF22C55E) else Color(0xFF3F4248), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isRecordingKey) "[ Bir tuşa basın... (İptal: ESC) ]" else "Atanan Tuş: $tempPttKeyName",
                            color = if (isRecordingKey) Color(0xFF22C55E) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Oyun oynarken bu tuşa basılı tutarak konuşabilirsiniz.",
                            color = Color(0xFF949BA4),
                            fontSize = 10.sp
                        )
                    }

                    Button(
                        onClick = { isRecordingKey = !isRecordingKey },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecordingKey) Color(0xFF22C55E) else Color(0xFF383A40)
                        )
                    ) {
                        Text(
                            text = if (isRecordingKey) "Dinleniyor..." else "Tuş Değiştir",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tarz Oyuncu Avatarları Header
                Text(
                    text = "Tarz Oyuncu Avatarları (10 Seçenek):",
                    color = Color(0xFFF2F3F5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 10 Preset Avatars Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    items(defaultAvatarPresets, key = { it.id }) { preset ->
                        val isSelected = tempAvatar == preset.imageUrl
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1F22))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF22C55E) else Color(0xFF3F4248),
                                    shape = CircleShape
                                )
                                .clickable { tempAvatar = preset.imageUrl },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = preset.imageUrl,
                                contentDescription = preset.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF313338))
                    ) {
                        Text(text = "İptal", color = Color(0xFF949BA4))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val finalName = tempName.trim().ifBlank { currentUsername }
                            onSaveProfile(finalName, tempAvatar.trim(), tempPttKeyName, tempPttKeyCode)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                    ) {
                        Text(
                            text = "Kaydet & Güncelle",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun loadLocalProfile(defaultName: String): Quadruple<String, String, String, Int> {
    try {
        val file = File(System.getProperty("user.home"), ".anoba_user_profile.json")
        if (file.exists() && file.readText().isNotBlank()) {
            val root = Json { ignoreUnknownKeys = true }.parseToJsonElement(file.readText()).jsonObject
            val savedName = root["username"]?.jsonPrimitive?.content ?: defaultName
            val savedAvatar = root["avatarUrl"]?.jsonPrimitive?.content ?: ""
            val savedPttKeyName = root["pttKeyName"]?.jsonPrimitive?.content ?: "Caps Lock"
            val savedPttKeyCode = root["pttKeyCode"]?.jsonPrimitive?.content?.toIntOrNull() ?: java.awt.event.KeyEvent.VK_CAPS_LOCK
            return Quadruple(savedName.ifBlank { defaultName }, savedAvatar, savedPttKeyName, savedPttKeyCode)
        }
    } catch (_: Exception) {}
    return Quadruple(defaultName, "", "Caps Lock", java.awt.event.KeyEvent.VK_CAPS_LOCK)
}

private fun saveLocalProfile(username: String, avatarUrl: String, pttKeyName: String, pttKeyCode: Int) {
    try {
        val file = File(System.getProperty("user.home"), ".anoba_user_profile.json")
        val jsonText = """
            {
              "username": "$username",
              "avatarUrl": "$avatarUrl",
              "pttKeyName": "$pttKeyName",
              "pttKeyCode": $pttKeyCode
            }
        """.trimIndent()
        file.writeText(jsonText)
    } catch (_: Exception) {}
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private suspend fun createServerRest(
    client: HttpClient,
    name: String,
    description: String,
    iconUrl: String = ""
): String {
    try {
        val postUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers"
        val jsonBody = """
            {
              "fields": {
                "name": { "stringValue": "$name" },
                "description": { "stringValue": "$description" },
                "iconUrl": { "stringValue": "$iconUrl" },
                "memberCount": { "integerValue": "1" }
              }
            }
        """.trimIndent()
        val respText = client.post(postUrl) {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }.bodyAsText()

        val root = Json { ignoreUnknownKeys = true }.parseToJsonElement(respText).jsonObject
        val namePath = root["name"]?.jsonPrimitive?.content ?: ""
        val serverId = namePath.substringAfterLast("/")

        if (serverId.isNotBlank()) {
            createChannelRest(client, serverId, "genel-sohbet", ChannelType.TEXT, 0)
            createChannelRest(client, serverId, "Genel Ses", ChannelType.VOICE, 1)
        }
        return serverId
    } catch (_: Exception) {
        return ""
    }
}

private suspend fun createChannelRest(
    client: HttpClient,
    serverId: String,
    name: String,
    type: ChannelType,
    position: Int
): String {
    if (serverId.isBlank() || name.isBlank()) return ""
    try {
        val postUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$serverId/channels"
        val typeStr = if (type == ChannelType.VOICE) "VOICE" else "TEXT"
        val jsonBody = """
            {
              "fields": {
                "serverId": { "stringValue": "$serverId" },
                "name": { "stringValue": "$name" },
                "type": { "stringValue": "$typeStr" },
                "position": { "integerValue": "$position" }
              }
            }
        """.trimIndent()
        val respText = client.post(postUrl) {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }.bodyAsText()

        val root = Json { ignoreUnknownKeys = true }.parseToJsonElement(respText).jsonObject
        val namePath = root["name"]?.jsonPrimitive?.content ?: ""
        return namePath.substringAfterLast("/")
    } catch (_: Exception) {
        return ""
    }
}

private suspend fun sendChatMessageRest(
    client: HttpClient,
    serverId: String,
    channelId: String,
    senderId: String,
    senderName: String,
    senderAvatar: String,
    text: String
) {
    if (serverId.isBlank() || channelId.isBlank() || text.isBlank()) return
    try {
        val postUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$serverId/channels/$channelId/messages"
        val jsonBody = """
            {
              "fields": {
                "senderId": { "stringValue": "$senderId" },
                "senderName": { "stringValue": "$senderName" },
                "senderAvatar": { "stringValue": "$senderAvatar" },
                "text": { "stringValue": "${text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}" },
                "timestamp": { "integerValue": "${System.currentTimeMillis()}" }
              }
            }
        """.trimIndent()
        client.post(postUrl) {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
    } catch (_: Exception) {}
}

private suspend fun leaveVoiceMember(client: HttpClient, serverId: String, channelId: String, userId: String) {
    if (serverId.isBlank() || channelId.isBlank() || userId.isBlank()) return
    try {
        val deleteUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$serverId/channels/$channelId/voice_members/$userId"
        client.delete(deleteUrl)
    } catch (_: Exception) {}
}

private fun parseServersFromResponse(responseText: String): List<ServerModel> {
    val servers = mutableListOf<ServerModel>()
    try {
        val root = Json { ignoreUnknownKeys = true }.parseToJsonElement(responseText).jsonObject
        val docs = root["documents"]?.jsonArray ?: return emptyList()
        for (doc in docs) {
            val docObj = doc.jsonObject
            val namePath = docObj["name"]?.jsonPrimitive?.content ?: ""
            val docId = namePath.substringAfterLast("/")
            val fields = docObj["fields"]?.jsonObject ?: continue

            val sName = fields["name"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "Sunucu"
            val sDesc = fields["description"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
            val sIcon = fields["iconUrl"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
            val sCount = fields["memberCount"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.content?.toIntOrNull() ?: 0

            if (docId.isNotBlank()) {
                servers.add(
                    ServerModel(
                        id = docId,
                        name = sName,
                        description = sDesc,
                        iconUrl = sIcon,
                        memberCount = sCount
                    )
                )
            }
        }
    } catch (_: Exception) {}
    return servers
}

private fun parseChannelsFromResponse(serverId: String, responseText: String): List<ChannelModel> {
    val channels = mutableListOf<ChannelModel>()
    try {
        val root = Json { ignoreUnknownKeys = true }.parseToJsonElement(responseText).jsonObject
        val docs = root["documents"]?.jsonArray ?: return emptyList()
        for (doc in docs) {
            val docObj = doc.jsonObject
            val namePath = docObj["name"]?.jsonPrimitive?.content ?: ""
            val docId = namePath.substringAfterLast("/")
            val fields = docObj["fields"]?.jsonObject ?: continue

            val cName = fields["name"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "kanal"
            val typeStr = fields["type"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "TEXT"
            val pos = fields["position"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.content?.toIntOrNull() ?: 0

            val cType = if (typeStr.uppercase() == "VOICE") ChannelType.VOICE else ChannelType.TEXT

            if (docId.isNotBlank()) {
                channels.add(
                    ChannelModel(
                        id = docId,
                        serverId = serverId,
                        name = cName,
                        type = cType,
                        position = pos
                    )
                )
            }
        }
    } catch (_: Exception) {}
    return channels.sortedBy { it.position }
}

private fun parseVoiceMembersFromResponse(responseText: String): List<VoiceMemberModel> {
    val members = mutableListOf<VoiceMemberModel>()
    try {
        val root = Json { ignoreUnknownKeys = true }.parseToJsonElement(responseText).jsonObject
        val docs = root["documents"]?.jsonArray ?: return emptyList()
        for (doc in docs) {
            val docObj = doc.jsonObject
            val namePath = docObj["name"]?.jsonPrimitive?.content ?: ""
            val docId = namePath.substringAfterLast("/")
            val fields = docObj["fields"]?.jsonObject ?: continue

            fun getString(key: String): String {
                return fields[key]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
            }

            fun getBool(vararg keys: String): Boolean {
                for (key in keys) {
                    val fieldObj = fields[key]?.jsonObject
                    if (fieldObj != null && fieldObj.containsKey("booleanValue")) {
                        return fieldObj["booleanValue"]?.jsonPrimitive?.booleanOrNull ?: false
                    }
                }
                return false
            }

            val uId = getString("userId").ifBlank { docId }
            val uName = getString("username").ifBlank { "Oyuncu" }
            val uAvatar = getString("userAvatar")
            val sId = getString("serverId")
            val cId = getString("channelId")
            val isMut = getBool("isMuted", "muted")
            val isDeaf = getBool("isDeafened", "deafened")
            val isSpk = getBool("isSpeaking", "speaking")
            val isStrm = getBool("isStreaming", "streaming")

            if (uId.isNotBlank() && uId.length > 2) {
                members.add(
                    VoiceMemberModel(
                        userId = uId,
                        username = uName,
                        userAvatar = uAvatar,
                        serverId = sId,
                        channelId = cId,
                        isMuted = isMut,
                        isDeafened = isDeaf,
                        isSpeaking = isSpk,
                        isStreaming = isStrm
                    )
                )
            }
        }
    } catch (_: Exception) {}
    return members.distinctBy { it.userId }
}

private fun parseMessagesFromResponse(serverId: String, channelId: String, responseText: String): List<ChatMessageModel> {
    val messages = mutableListOf<ChatMessageModel>()
    try {
        val root = Json { ignoreUnknownKeys = true }.parseToJsonElement(responseText).jsonObject
        val docs = root["documents"]?.jsonArray ?: return emptyList()
        for (doc in docs) {
            val docObj = doc.jsonObject
            val namePath = docObj["name"]?.jsonPrimitive?.content ?: ""
            val docId = namePath.substringAfterLast("/")
            val fields = docObj["fields"]?.jsonObject ?: continue

            fun getString(key: String): String {
                return fields[key]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
            }

            fun getLong(key: String): Long {
                return fields[key]?.jsonObject?.get("integerValue")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
            }

            val sId = getString("senderId")
            val sName = getString("senderName").ifBlank { "Oyuncu" }
            val sAvatar = getString("senderAvatar")
            val txt = getString("text")
            val ts = getLong("timestamp")

            if (txt.isNotBlank()) {
                messages.add(
                    ChatMessageModel(
                        id = docId,
                        serverId = serverId,
                        channelId = channelId,
                        senderId = sId,
                        senderName = sName,
                        senderAvatar = sAvatar,
                        text = txt,
                        timestamp = ts
                    )
                )
            }
        }
    } catch (_: Exception) {}
    return messages.sortedBy { it.timestamp }
}
