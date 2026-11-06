package com.neval.anoba.community.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.neval.anoba.community.model.ChannelModel
import com.neval.anoba.community.model.ChannelType
import com.neval.anoba.community.model.ServerModel
import com.neval.anoba.community.model.VoiceMemberModel
import com.neval.anoba.community.ui.SharedCommunityHubScreen
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

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
    val server1Id = "W63WMEO14jCdnqB6XF2I"
    val server2Id = "V6b0gILICkuRN5yD9ZLo"

    val bilekKadroId = "McrpiNRjYsDeUbsXqqeW"
    val pubgSquadId = "xZ57ayxWJ2vMljuMbnkt"
    val kurucuOdasiId = "T3i4nDF9S0HXWJn1WHyV"
    val genelSohbetId = "olZu2DgiD0kmvl5mm3wC"
    val oyunDuyuruId = "0sowTwchnlYF3JsWC5An"

    var selectedServerId by remember { mutableStateOf(server1Id) }
    var selectedChannelId by remember { mutableStateOf(bilekKadroId) }
    var connectedChannelId by remember { mutableStateOf(bilekKadroId) }
    var isMuted by remember { mutableStateOf(false) }
    var isDeafened by remember { mutableStateOf(false) }

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
    var currentUsername by remember { mutableStateOf(systemUsername) }

    val sampleServers = remember {
        listOf(
            ServerModel(id = server1Id, name = "Anoba Gaming 🎮", description = "PUBG & Valorant", memberCount = 128),
            ServerModel(id = server2Id, name = "Serbest Alan ☕", description = "Sohbet & Müzik", memberCount = 64)
        )
    }

    val sampleChannels = remember(selectedServerId) {
        if (selectedServerId == server1Id) {
            listOf(
                ChannelModel(id = genelSohbetId, serverId = server1Id, name = "genel-sohbet", type = ChannelType.TEXT, position = 0),
                ChannelModel(id = oyunDuyuruId, serverId = server1Id, name = "oyun-duyuru", type = ChannelType.TEXT, position = 1),
                ChannelModel(id = bilekKadroId, serverId = server1Id, name = "Bilek Kadro", type = ChannelType.VOICE, position = 2),
                ChannelModel(id = pubgSquadId, serverId = server1Id, name = "PUBG Squad", type = ChannelType.VOICE, position = 3),
                ChannelModel(id = kurucuOdasiId, serverId = server1Id, name = "Kurucu Odası", type = ChannelType.VOICE, position = 4)
            )
        } else {
            listOf(
                ChannelModel(id = "c6", serverId = server2Id, name = "muhabbet", type = ChannelType.TEXT, position = 0),
                ChannelModel(id = "c7", serverId = server2Id, name = "Müzik & Ses", type = ChannelType.VOICE, position = 1)
            )
        }
    }

    var liveVoiceMembers by remember { mutableStateOf(emptyList<VoiceMemberModel>()) }
    val client = remember { HttpClient() }

    // Live Sync with Firestore via REST API with 2-second Polling
    LaunchedEffect(selectedServerId, selectedChannelId, connectedChannelId, isMuted, isDeafened, currentUsername) {
        val targetChan = if (connectedChannelId.isNotBlank()) connectedChannelId else selectedChannelId
        val targetServ = selectedServerId

        while (isActive) {
            try {
                if (connectedChannelId.isNotBlank() && targetServ.isNotBlank()) {
                    val postUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$targetServ/channels/$connectedChannelId/voice_members/$currentUserId"
                    val jsonBody = """
                        {
                          "fields": {
                            "userId": { "stringValue": "$currentUserId" },
                            "username": { "stringValue": "$currentUsername" },
                            "serverId": { "stringValue": "$targetServ" },
                            "channelId": { "stringValue": "$connectedChannelId" },
                            "muted": { "booleanValue": $isMuted },
                            "deafened": { "booleanValue": $isDeafened },
                            "speaking": { "booleanValue": ${!isMuted} },
                            "joinedAt": { "integerValue": "${System.currentTimeMillis()}" }
                          }
                        }
                    """.trimIndent()
                    client.post(postUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(jsonBody)
                    }
                }

                if (targetServ.isNotBlank() && targetChan.isNotBlank()) {
                    val getUrl = "https://firestore.googleapis.com/v1/projects/anoba-cd9de/databases/(default)/documents/community_servers/$targetServ/channels/$targetChan/voice_members"
                    val responseText = client.get(getUrl).bodyAsText()
                    if (responseText.contains("documents")) {
                        val fetchedMembers = mutableListOf<VoiceMemberModel>()
                        val docs = responseText.split("\"name\":")
                        for (doc in docs) {
                            if (doc.contains("userId") && doc.contains("stringValue")) {
                                try {
                                    val uId = doc.substringAfter("\"userId\"").substringAfter("\"stringValue\":").substringAfter("\"").substringBefore("\"")
                                    val uName = if (doc.contains("\"username\"")) doc.substringAfter("\"username\"").substringAfter("\"stringValue\":").substringAfter("\"").substringBefore("\"") else "Oyuncu"
                                    val isSpk = doc.contains("\"speaking\"") && doc.substringAfter("\"speaking\"").contains("true")
                                    val isMut = doc.contains("\"muted\"") && doc.substringAfter("\"muted\"").contains("true")
                                    val isDeaf = doc.contains("\"deafened\"") && doc.substringAfter("\"deafened\"").contains("true")
                                    if (uId.isNotBlank() && uId.length > 2) {
                                        fetchedMembers.add(
                                            VoiceMemberModel(
                                                userId = uId,
                                                username = uName,
                                                isSpeaking = isSpk,
                                                isMuted = isMut,
                                                isDeafened = isDeaf
                                            )
                                        )
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                        if (fetchedMembers.isNotEmpty()) {
                            liveVoiceMembers = fetchedMembers.distinctBy { it.userId }
                        } else if (connectedChannelId.isNotBlank()) {
                            liveVoiceMembers = listOf(
                                VoiceMemberModel(userId = currentUserId, username = currentUsername, isSpeaking = !isMuted, isMuted = isMuted)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                if (liveVoiceMembers.isEmpty() && connectedChannelId.isNotBlank()) {
                    liveVoiceMembers = listOf(
                        VoiceMemberModel(userId = currentUserId, username = currentUsername, isSpeaking = !isMuted, isMuted = isMuted)
                    )
                }
            }
            delay(2000)
        }
    }

    SharedCommunityHubScreen(
        servers = sampleServers,
        channels = sampleChannels,
        voiceMembers = liveVoiceMembers,
        selectedServerId = selectedServerId,
        selectedChannelId = selectedChannelId,
        currentUserId = currentUserId,
        currentUsername = currentUsername,
        currentUserAvatar = "",
        onServerSelected = { serverId ->
            selectedServerId = serverId
            val firstChan = if (serverId == server1Id) genelSohbetId else "c6"
            selectedChannelId = firstChan
        },
        onChannelSelected = { channel ->
            selectedChannelId = channel.id
            if (channel.type == ChannelType.VOICE) {
                connectedChannelId = channel.id
            }
        },
        onToggleMute = { isMuted = !isMuted },
        onToggleDeafen = { isDeafened = !isDeafened },
        onLeaveVoice = { connectedChannelId = "" },
        onJoinVoice = { channel -> connectedChannelId = channel.id }
    )
}
