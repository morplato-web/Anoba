package com.neval.anoba.community.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.community.model.ChannelModel
import com.neval.anoba.community.model.ChannelType
import com.neval.anoba.community.model.ServerModel
import com.neval.anoba.community.model.VoiceMemberModel

// Discord / Gaming Dark Theme Renk Paleti (Koyu Lacivert / Kömür Grisi)
private val ServerBarBackground = Color(0xFF1E1F22)
private val ChannelBarBackground = Color(0xFF2B2D31)
private val StageBackground = Color(0xFF313338)
private val SpeakingGreen = Color(0xFF22C55E)
private val AnobaNavyAccent = Color(0xFF1A237E)
private val TextWhite = Color(0xFFF2F3F5)
private val TextMuted = Color(0xFF949BA4)

@Suppress("UNUSED_PARAMETER", "UnusedBoxWithConstraintsScope")
@Composable
fun CommunityHubScreen(
    servers: List<ServerModel>,
    channels: List<ChannelModel>,
    voiceMembers: List<VoiceMemberModel>,
    selectedServerId: String,
    selectedChannelId: String,
    currentUserId: String,
    currentUsername: String,
    currentUserAvatar: String,
    onServerSelected: (String) -> Unit,
    onChannelSelected: (ChannelModel) -> Unit,
    onToggleMute: () -> Unit,
    onToggleDeafen: () -> Unit,
    onLeaveVoice: () -> Unit,
    onJoinVoice: (ChannelModel) -> Unit,
    modifier: Modifier = Modifier,
    isPushToTalkEnabled: Boolean = false,
    isScreenSharing: Boolean = false,
    activeSpeakingUserIds: Set<Int> = emptySet(),
    onTogglePushToTalk: () -> Unit = {},
    onToggleScreenShare: () -> Unit = {},
    onPushToTalkKeyPressed: () -> Unit = {},
    onPushToTalkKeyReleased: () -> Unit = {}
) {
    var isMuted by remember { mutableStateOf(false) }
    var isDeafened by remember { mutableStateOf(false) }
    var showStageOnMobile by remember { mutableStateOf(false) }

    val activeVoiceChannel = channels.find { it.id == selectedChannelId && it.type == ChannelType.VOICE }
    val selectedServer = servers.find { it.id == selectedServerId }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val isMobile = maxWidth < 600.dp

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = StageBackground
        ) {
            if (isMobile) {
                // 📱 MOBİL YERLEŞİM (maxWidth < 600.dp) - Tek Ekran Esnek Görünüm
                if (showStageOnMobile && activeVoiceChannel != null) {
                    // Mobil Tam Ekran Sahne / Grid Kartlar
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Üst Bar: Kanallara Geri Dön Butonu
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ChannelBarBackground)
                                    .clickable { showStageOnMobile = false }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "◀ ${stringResource(R.string.freehub_channels)}",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Text(
                                text = "🔊 ${activeVoiceChannel.name}",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Grid Kartlar
                        if (voiceMembers.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.freehub_empty_room),
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 140.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) {
                                items(voiceMembers, key = { it.userId }) { member ->
                                    SpeakerGridCard(
                                        member = member,
                                        activeSpeakingUserIds = activeSpeakingUserIds
                                    )
                                }
                            }
                        }

                        // Mobil Alt Ses Kontrol Barı
                        VoiceControlBottomPanel(
                            username = currentUsername,
                            avatarUrl = currentUserAvatar,
                            connectedChannelName = activeVoiceChannel.name,
                            isMuted = isMuted,
                            isDeafened = isDeafened,
                            isPushToTalkEnabled = isPushToTalkEnabled,
                            isScreenSharing = isScreenSharing,
                            onToggleMute = {
                                isMuted = !isMuted
                                onToggleMute()
                            },
                            onToggleDeafen = {
                                isDeafened = !isDeafened
                                onToggleDeafen()
                            },
                            onTogglePushToTalk = onTogglePushToTalk,
                            onToggleScreenShare = onToggleScreenShare,
                            onLeaveVoice = {
                                onLeaveVoice()
                                showStageOnMobile = false
                            }
                        )
                    }
                } else {
                    // Mobil Sunucu Barı (64dp) + Kanal Listesi (Kalan Alan)
                    Row(modifier = Modifier.fillMaxSize()) {
                        ServerListColumn(
                            servers = servers,
                            selectedServerId = selectedServerId,
                            onServerSelected = onServerSelected,
                            modifier = Modifier
                                .width(64.dp)
                                .fillMaxHeight()
                                .background(ServerBarBackground)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(ChannelBarBackground)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .background(ServerBarBackground.copy(alpha = 0.5f))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = selectedServer?.name ?: "FreeHub",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(channels, key = { it.id }) { channel ->
                                    ChannelItemRow(
                                        channel = channel,
                                        isSelected = channel.id == selectedChannelId,
                                        voiceMembers = if (channel.type == ChannelType.VOICE) voiceMembers.filter { it.channelId == channel.id } else emptyList(),
                                        activeSpeakingUserIds = activeSpeakingUserIds,
                                        onClick = {
                                            onChannelSelected(channel)
                                            if (channel.type == ChannelType.VOICE) {
                                                onJoinVoice(channel)
                                                showStageOnMobile = true
                                            }
                                        }
                                    )
                                }
                            }

                            VoiceControlBottomPanel(
                                username = currentUsername,
                                avatarUrl = currentUserAvatar,
                                connectedChannelName = activeVoiceChannel?.name,
                                isMuted = isMuted,
                                isDeafened = isDeafened,
                                isPushToTalkEnabled = isPushToTalkEnabled,
                                isScreenSharing = isScreenSharing,
                                onToggleMute = {
                                    isMuted = !isMuted
                                    onToggleMute()
                                },
                                onToggleDeafen = {
                                    isDeafened = !isDeafened
                                    onToggleDeafen()
                                },
                                onTogglePushToTalk = onTogglePushToTalk,
                                onToggleScreenShare = onToggleScreenShare,
                                onLeaveVoice = onLeaveVoice
                            )
                        }
                    }
                }
            } else {
                // 💻 MASAÜSTÜ / TABLET DÜZENİ (maxWidth >= 600.dp) - 3 SÜTUN YAN YANA
                Row(modifier = Modifier.fillMaxSize()) {

                    // 🔴 1. SÜTUN: Sol Sunucu Barı (72.dp Sabit)
                    ServerListColumn(
                        servers = servers,
                        selectedServerId = selectedServerId,
                        onServerSelected = onServerSelected,
                        modifier = Modifier
                            .width(72.dp)
                            .fillMaxHeight()
                            .background(ServerBarBackground)
                    )

                    // 🟠 2. SÜTUN: Orta Kanal Listesi + Ses Kontrol Barı (240.dp Sabit)
                    Column(
                        modifier = Modifier
                            .width(240.dp)
                            .fillMaxHeight()
                            .background(ChannelBarBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(ServerBarBackground.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = selectedServer?.name ?: "FreeHub",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(channels, key = { it.id }) { channel ->
                                ChannelItemRow(
                                    channel = channel,
                                    isSelected = channel.id == selectedChannelId,
                                    voiceMembers = if (channel.type == ChannelType.VOICE) voiceMembers.filter { it.channelId == channel.id } else emptyList(),
                                    activeSpeakingUserIds = activeSpeakingUserIds,
                                    onClick = {
                                        onChannelSelected(channel)
                                        if (channel.type == ChannelType.VOICE) {
                                            onJoinVoice(channel)
                                        }
                                    }
                                )
                            }
                        }

                        VoiceControlBottomPanel(
                            username = currentUsername,
                            avatarUrl = currentUserAvatar,
                            connectedChannelName = activeVoiceChannel?.name,
                            isMuted = isMuted,
                            isDeafened = isDeafened,
                            isPushToTalkEnabled = isPushToTalkEnabled,
                            isScreenSharing = isScreenSharing,
                            onToggleMute = {
                                isMuted = !isMuted
                                onToggleMute()
                            },
                            onToggleDeafen = {
                                isDeafened = !isDeafened
                                onToggleDeafen()
                            },
                            onTogglePushToTalk = onTogglePushToTalk,
                            onToggleScreenShare = onToggleScreenShare,
                            onLeaveVoice = onLeaveVoice
                        )
                    }

                    // 🟡 3. SÜTUN: Sağ Sahne (Grid Kartlar - weight(1f) Dinamik Esnek)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(StageBackground)
                            .padding(16.dp)
                    ) {
                        if (activeVoiceChannel != null) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🔊 ${activeVoiceChannel.name} (${voiceMembers.size} Oyuncu)",
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                if (voiceMembers.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Odada henüz kimse yok. Ses kanalına katıldınız!",
                                            color = TextMuted,
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 220.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(voiceMembers, key = { it.userId }) { member ->
                                            SpeakerGridCard(
                                                member = member,
                                                activeSpeakingUserIds = activeSpeakingUserIds
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = stringResource(R.string.freehub_select_channel_prompt),
                                        color = TextWhite,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = stringResource(R.string.freehub_select_channel_subprompt),
                                        color = TextMuted,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerListColumn(
    servers: List<ServerModel>,
    selectedServerId: String,
    onServerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Anoba Ana Hub İkonu
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AnobaNavyAccent)
                .clickable { if (servers.isNotEmpty()) onServerSelected(servers.first().id) },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🎮", fontSize = 22.sp)
        }

        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(32.dp)
                .height(2.dp)
                .background(TextMuted.copy(alpha = 0.3f))
        )

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(servers, key = { it.id }) { server ->
                val isSelected = server.id == selectedServerId
                val cornerRadius by animateDpAsState(if (isSelected) 16.dp else 24.dp, label = "shape")

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Seçili sunucu sol beyaz çizgi göstergesi
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(if (isSelected) 36.dp else 0.dp)
                            .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                            .background(TextWhite)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(if (isSelected) AnobaNavyAccent else ChannelBarBackground)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) TextWhite else Color.Transparent,
                                shape = RoundedCornerShape(cornerRadius)
                            )
                            .clickable { onServerSelected(server.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (server.iconUrl.isNotBlank()) {
                            AsyncImage(
                                model = server.iconUrl,
                                contentDescription = server.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = server.name.take(2).uppercase(),
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelItemRow(
    channel: ChannelModel,
    isSelected: Boolean,
    voiceMembers: List<VoiceMemberModel>,
    activeSpeakingUserIds: Set<Int> = emptySet(),
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) StageBackground else Color.Transparent)
                .clickable { onClick() }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (channel.type == ChannelType.VOICE) "🔊" else "#",
                color = if (isSelected) TextWhite else TextMuted,
                fontSize = 15.sp,
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = channel.name,
                color = if (isSelected) TextWhite else TextMuted,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Ses kanalının altındaki oyuncu listesi
        if (channel.type == ChannelType.VOICE && voiceMembers.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 32.dp, top = 2.dp, bottom = 4.dp)) {
                voiceMembers.forEach { member ->
                    val isSpeaking = member.isSpeaking || (member.userId.hashCode() in activeSpeakingUserIds)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(AnobaNavyAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (member.userAvatar.isNotBlank()) {
                                AsyncImage(
                                    model = member.userAvatar,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = member.username.take(1).uppercase(),
                                    color = TextWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = member.username,
                            color = if (isSpeaking) SpeakingGreen else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSpeaking) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (member.isStreaming) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.Red)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.freehub_live_badge),
                                    color = TextWhite,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (member.isMuted) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.MicOff,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeakerGridCard(
    member: VoiceMemberModel,
    activeSpeakingUserIds: Set<Int> = emptySet()
) {
    val isSpeaking = member.isSpeaking || (member.userId.hashCode() in activeSpeakingUserIds)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ChannelBarBackground),
        border = if (isSpeaking) BorderStroke(2.dp, SpeakingGreen) else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AnobaNavyAccent)
                        .border(
                            width = if (isSpeaking) 3.dp else 0.dp,
                            color = if (isSpeaking) SpeakingGreen else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (member.userAvatar.isNotBlank()) {
                        AsyncImage(
                            model = member.userAvatar,
                            contentDescription = member.username,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = member.username.take(2).uppercase(),
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = member.username,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            // Yayın Yapıyorsa "Yayını İzle" Kutusu
            if (member.isStreaming) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(1.dp, SpeakingGreen, RoundedCornerShape(6.dp))
                        .clickable { }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ScreenShare,
                            contentDescription = null,
                            tint = SpeakingGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.freehub_watch_stream),
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sağ üst mute ikonu
            if (member.isMuted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceControlBottomPanel(
    username: String,
    avatarUrl: String,
    connectedChannelName: String?,
    isMuted: Boolean,
    isDeafened: Boolean,
    isPushToTalkEnabled: Boolean = false,
    isScreenSharing: Boolean = false,
    onToggleMute: () -> Unit,
    onToggleDeafen: () -> Unit,
    onTogglePushToTalk: () -> Unit = {},
    onToggleScreenShare: () -> Unit = {},
    onLeaveVoice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ServerBarBackground)
            .padding(8.dp)
    ) {
        // Ses Bağlantısı Durumu
        if (connectedChannelName != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.freehub_voice_connected),
                        color = SpeakingGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = connectedChannelName,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onLeaveVoice,
                    modifier = Modifier.size(28.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Red)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Ayrıl",
                        tint = TextWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Kullanıcı Profil & Butonlar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AnobaNavyAccent),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = username,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = username.take(1).uppercase(),
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = username,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Mikrofon & Kulaklık & Bas-Konuş Butonları
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Bas-Konuş Mode Toggle Button
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPushToTalkEnabled) SpeakingGreen.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isPushToTalkEnabled) SpeakingGreen else TextMuted.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onTogglePushToTalk() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPushToTalkEnabled) stringResource(R.string.freehub_mode_ptt) else stringResource(R.string.freehub_mode_free),
                        color = if (isPushToTalkEnabled) SpeakingGreen else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mikrofon",
                        tint = if (isMuted) Color.Red else TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleDeafen,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Headphones,
                        contentDescription = "Kulaklık",
                        tint = if (isDeafened) Color.Red else TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleScreenShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ScreenShare,
                        contentDescription = "Ekran Paylaşımı",
                        tint = if (isScreenSharing) SpeakingGreen else TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
