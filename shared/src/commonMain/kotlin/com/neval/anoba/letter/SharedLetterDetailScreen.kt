package com.neval.anoba.letter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.getRelativeTimeString
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedLetterDetailScreen(
    letter: SharedLetterModel? = null,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onOpenComments: () -> Unit = {},
    onShareClick: (SharedLetterModel) -> Unit = {},
    onEmojiReaction: (emoji: String) -> Unit = {},
    onOpenEmojiUsersList: () -> Unit = {},
) {
    val currentLetter = remember(letter) {
        letter ?: SharedLetterModel(
            id = "let_demo",
            ownerId = "u_demo",
            username = "Ahmet Y.",
            title = "2026 Yılına Mektup ✉️",
            content = "Sevgili geleceğim, bu bu mektup Anoba Zaman Kapsülü ile yazıldı...",
            privacy = "PUBLIC",
            createdAtMillis = 1710000000000L,
            isPublished = true,
            userBadgeType = "PIONEER",
            reactionCount = 128,
            commentsCount = 14,
            viewCount = 340,
        )
    }

    val isAudio = !currentLetter.audioUrl.isNullOrBlank()
    var isPlaying by remember { mutableStateOf(value = false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✉️ Mektup Detayı", fontWeight = FontWeight.Bold, color = AnobaNavy) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AnobaNavy)
                    }
                },
                actions = {
                    IconButton(onClick = { onShareClick(currentLetter) }) {
                        Icon(Icons.Default.Share, contentDescription = "Paylaş", tint = AnobaNavy)
                    }
                    IconButton(onClick = onHomeClick) {
                        Icon(Icons.Default.Home, contentDescription = "Ana Sayfa", tint = AnobaNavy)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFCFB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(
                                photoUrl = currentLetter.userProfileImageUrl,
                                username = currentLetter.username,
                                size = 32.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentLetter.username,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (currentLetter.isOfficial) AnobaGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            AnobaBadge(
                                badgeType = currentLetter.userBadgeType,
                                isOfficial = currentLetter.isOfficial,
                            )
                        }

                        Icon(
                            imageVector = if (isAudio) Icons.Default.Mic else Icons.Default.Mail,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ses Oynatıcı Paneli (Varsa)
                    if (isAudio) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                IconButton(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier.size(52.dp).background(Color.White, CircleShape),
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = AnobaGold,
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sesli Mektup Dinle", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }

                    val titleText = currentLetter.title
                    if (!titleText.isNullOrBlank()) {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AnobaNavy,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = currentLetter.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Serif,
                            lineHeight = 24.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val timeStr = getRelativeTimeString(currentLetter.createdAtMillis)
                    if (timeStr.isNotBlank()) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🚀 EMOJİ SEÇİM VE BİLGİ BARI
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Üst Satır: SOLDAYAN YANA -> Göz İkonu + Görüntülenme Sayısı  |  Lider Emoji + Skor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (currentLetter.viewCount > 0) {
                            Icon(
                                imageVector = Icons.Default.RemoveRedEye,
                                contentDescription = "Görüntülenme",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentLetter.viewCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                            )
                        }

                        if (currentLetter.viewCount > 0 && currentLetter.reactionCount > 0) {
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        if (currentLetter.reactionCount > 0) {
                            Surface(
                                modifier = Modifier.clickable { onOpenEmojiUsersList() },
                                shape = RoundedCornerShape(12.dp),
                                color = AnobaNavy.copy(alpha = 0.08f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = currentLetter.topEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentLetter.reactionCount.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AnobaNavy,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🚀 KAYAN EMOJİ SATIRI
                    val emojiList = listOf("❤️", "🔥", "👏", "👍", "😂", "😍", "😮", "😢", "🥰")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        emojiList.forEach { emoji ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onEmojiReaction(emoji) },
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = emoji, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Yorumlar Butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenComments() }
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "Yorumlar",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Yorumlar",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Text(
                    text = "${currentLetter.commentsCount} yorum",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
                Text("Kapat")
            }

            Spacer(modifier = Modifier.height(34.dp))
        }
    }
}
