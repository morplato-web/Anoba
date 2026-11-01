package com.neval.anoba.letter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
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

// 🎯 ANOBA'NIN ORİJİNAL STANDART EMOJİ LİSTESİ
private val ANOBA_STANDARD_EMOJIS = listOf(
    "❤️", // Sevgi / Çok Beğenme
    "😂", // Kahkaha / Çok Komik
    "👍", // Onay / Katılıyorum
    "🔥", // Mükemmel / Efsane
    "🥰", // Şefkat / Destek
    "😢", // Üzüntü / Duygusal
    "😲", // Vay Canına! / Şaşkınlık
    "🤔", // Düşündürücü
    "🫤", // Nötr / Vasat / Katılmıyorum
    "😠", // Kızgınlık / Sinirli
    "🚫", // Sakıncalı İçerik
    "❌"  // Silinsin Talebi
)

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
            content = "Sevgili geleceğim, bu mektup Anoba Zaman Kapsülü ile yazıldı...",
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
    var isPlaying by remember { mutableStateOf(false) }

    val topEmoji = currentLetter.topEmoji.ifBlank { "❤️" }
    val reactionTotal = currentLetter.reactionCount

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
        bottomBar = {
            // 🚀 TELEFONUN SİSTEM NAVİGASYON ÇUBUĞUNUN ÜSTÜNDE GÜVENLİ ALANDA DURAN BUTON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // Sistem barının altında kalmasını kesin olarak önler
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AnobaGold),
                ) {
                    Text(
                        text = "Kapat",
                        fontWeight = FontWeight.Bold,
                        color = AnobaNavy,
                        fontSize = 15.sp,
                    )
                }
            }
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

            // 1. MEKTUP METNİ KARTI
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
                }
            }

            // 🎯 MEKTUP KARTI İLE İSTATİSTİK SATIRI ARASINDAKİ 20.dp BOŞLUK
            Spacer(modifier = Modifier.height(20.dp))

            // 🚀 2. KUTUSUZ İSTATİSTİK SATIRI
            // SOL: [Göz + Skor]  [En Çok Tıklanan Emoji + Skor]  ------ SAĞ: [Zaman Damgası]  [Yorum + Skor]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Sol Grup: Göz İkonu ve En Çok Tıklanan Emoji Rozeti
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Göz İkonu + Skor
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveRedEye,
                            contentDescription = "Görüntülenme",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = currentLetter.viewCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }

                    // En Çok Tıklanan Emoji + Skor Rozeti
                    if (reactionTotal > 0) {
                        Surface(
                            modifier = Modifier.clickable { onOpenEmojiUsersList() },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFEBEE),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(text = topEmoji, fontSize = 13.sp)
                                Text(
                                    text = reactionTotal.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F),
                                )
                            }
                        }
                    }
                }

                // Sağ Grup: Zaman Damgası ve Yorum İkonu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Zaman Damgası (Yorum ikonundan hemen önce)
                    val timeStr = getRelativeTimeString(currentLetter.createdAtMillis)
                    if (timeStr.isNotBlank()) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }

                    // Yorum İkonu + Skor
                    Row(
                        modifier = Modifier
                            .clickable { onOpenComments() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Comment,
                            contentDescription = "Yorumlar",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Gray,
                        )
                        Text(
                            text = currentLetter.commentsCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🚀 3. KUTUSUZ SERBEST KAYAN ANOBA ORİJİNAL EMOJİ SATIRI
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(
                    items = ANOBA_STANDARD_EMOJIS,
                    key = { it },
                ) { emoji: String ->
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier.width(44.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape,
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = ripple(bounded = true, radius = 20.dp),
                                    role = Role.Button,
                                    onClick = { onEmojiReaction(emoji) },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "·",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray,
                        )
                    }
                }
            }

            // Sayfa içeriği için alt boşluk
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}