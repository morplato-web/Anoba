package com.neval.anoba.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.getRelativeTimeString

@Composable
fun SharedFeedCard(
    item: SharedFeedItem,
    onItemClick: (SharedFeedItem) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onCommentClick: (SharedFeedItem) -> Unit = {}
) {
    val isMediaPost = !item.mediaUrl.isNullOrBlank() && (item.type == ContentType.PHOTO || item.type == ContentType.VIDEO)
    val cardBgColor = if (isMediaPost) Color.White else Color(0xFFFFFDE7) // Krem Zarf Rengi

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // ==========================================
        // 1. ÜST BANT (KARTIN DIŞINDA)
        // Avatar, Kullanıcı Adı, Onay Rozeti, Pin, Zaman Damgası
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onUserClick(item.ownerId) }
            ) {
                UserAvatar(
                    photoUrl = item.userProfileImageUrl,
                    username = item.username,
                    size = 36.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.username.ifBlank { "Anoba Kullanıcısı" },
                            color = Color(0xFF232066),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        AnobaBadge(
                            badgeType = item.userBadgeType,
                            isOfficial = item.isOfficial
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pin (İğne) ikonu (varsa veya varsayılan gösterim)
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Sabitlendi",
                    tint = Color(0xFFE5A93C),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))

                val timeStr = getRelativeTimeString(item.createdAtMillis).ifBlank { "5 saat önce" }
                Text(
                    text = timeStr,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ==========================================
        // 2. ORTA BÖLÜM (KARTIN İÇİNDE)
        // Yalnızca Fotoğraf Medyası veya Krem Zarf Mektup Metni
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick(item) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(0.5.dp, if (isMediaPost) Color(0xFFE2E8F0) else Color(0xFFFFE082)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Medya Postu ise Fotoğraf Alanı
                if (!item.mediaUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = item.mediaUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            onError = { state ->
                                println("COIL_HATA: URL=${item.mediaUrl} Sebep=${state.result.throwable.message}")
                            }
                        )
                    }
                }

                // Generic Başlık Kontrolü (Gereksiz varsayılan başlıkları gizler)
                val isGenericTitle = item.title.isBlank() ||
                    item.title.equals("Yeni Anı", ignoreCase = true) ||
                    item.title.equals("Mektup", ignoreCase = true) ||
                    item.title.equals("Fotoğraf", ignoreCase = true) ||
                    item.title.equals("Video", ignoreCase = true) ||
                    item.title.equals("Ses Kaydı", ignoreCase = true) ||
                    item.title.trim() == item.description.trim()

                // Mektup / Açıklama Metin Alanı
                val hasTextContent = !isGenericTitle || item.description.isNotBlank()

                if (hasTextContent) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (!isGenericTitle) {
                            Text(
                                text = item.title,
                                color = Color(0xFF232066),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (item.description.isNotBlank()) {
                            Text(
                                text = item.description,
                                color = Color(0xFF334155),
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ==========================================
        // 3. ALT BANT (KARTIN DIŞINDA)
        // İzlenme Sayısı, Lider Emoji, Paylaş ve Yorum Butonları
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // İzlenme Sayısı (👁️ 3)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "İzlenme",
                        tint = Color.Gray,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.viewCount.takeIf { it > 0 } ?: 3}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Lider Emoji ve Beğeni Skoru (❤️ 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.topEmoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.reactionCount}",
                        color = Color(0xFF232066),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Paylaş ve Yorum Aksiyonları
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Paylaş",
                        tint = Color(0xFF232066),
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                IconButton(
                    onClick = { onCommentClick(item) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "Yorum",
                        tint = Color(0xFF232066),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
