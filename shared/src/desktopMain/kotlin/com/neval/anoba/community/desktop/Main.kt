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
    var selectedServerId by remember { mutableStateOf("s1") }
    var selectedChannelId by remember { mutableStateOf("c3") }
    var isMuted by remember { mutableStateOf(false) }
    var isDeafened by remember { mutableStateOf(false) }

    val sampleServers = remember {
        listOf(
            ServerModel(id = "s1", name = "Anoba Gaming 🎮", description = "PUBG & Valorant", memberCount = 128),
            ServerModel(id = "s2", name = "Serbest Alan ☕", description = "Sohbet & Müzik", memberCount = 64)
        )
    }

    val sampleChannels = remember(selectedServerId) {
        if (selectedServerId == "s1") {
            listOf(
                ChannelModel(id = "c1", serverId = "s1", name = "genel-sohbet", type = ChannelType.TEXT, position = 0),
                ChannelModel(id = "c2", serverId = "s1", name = "oyun-duyuru", type = ChannelType.TEXT, position = 1),
                ChannelModel(id = "c3", serverId = "s1", name = "Bilek Kadro", type = ChannelType.VOICE, position = 2),
                ChannelModel(id = "c4", serverId = "s1", name = "PUBG Squad", type = ChannelType.VOICE, position = 3),
                ChannelModel(id = "c5", serverId = "s1", name = "Kurucu Odası", type = ChannelType.VOICE, position = 4)
            )
        } else {
            listOf(
                ChannelModel(id = "c6", serverId = "s2", name = "muhabbet", type = ChannelType.TEXT, position = 0),
                ChannelModel(id = "c7", serverId = "s2", name = "Müzik & Ses", type = ChannelType.VOICE, position = 1)
            )
        }
    }

    val sampleVoiceMembers = remember {
        listOf(
            VoiceMemberModel(userId = "u1", username = "Anoba", isSpeaking = true)
        )
    }

    SharedCommunityHubScreen(
        servers = sampleServers,
        channels = sampleChannels,
        voiceMembers = sampleVoiceMembers,
        selectedServerId = selectedServerId,
        selectedChannelId = selectedChannelId,
        currentUserId = "u1",
        currentUsername = "Anoba",
        currentUserAvatar = "",
        onServerSelected = { serverId -> selectedServerId = serverId },
        onChannelSelected = { channel -> selectedChannelId = channel.id },
        onToggleMute = { isMuted = !isMuted },
        onToggleDeafen = { isDeafened = !isDeafened },
        onLeaveVoice = { },
        onJoinVoice = { channel -> selectedChannelId = channel.id }
    )
}
