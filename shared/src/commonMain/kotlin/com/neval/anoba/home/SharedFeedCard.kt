package com.neval.anoba.home

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
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
    val isMediaPost = !item.mediaUrl.isNullOrBlank() || item.type == ContentType.PHOTO || item.type == ContentType.VIDEO
    val cardBgColor = if (isMediaPost) Color.White else Color(0xFFFFFDE7) // Cream Light Yellow for Text/Letter posts

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // ÜST BANT (Avatar, İsim, Onay Tik, Tarih)
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    val timeStr = getRelativeTimeString(item.createdAtMillis).ifBlank { "6 gün önce" }
                    Text(
                        text = timeStr,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // MEDYA GÖRSELİ (Varsa Yükle)
            if (!item.mediaUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                Spacer(modifier = Modifier.height(10.dp))
            }

            val isGenericTitle = item.title.isBlank() || 
                item.title.equals("Yeni Anı", ignoreCase = true) || 
                item.title.equals("Mektup", ignoreCase = true) || 
                item.title.equals("Fotoğraf", ignoreCase = true) || 
                item.title.equals("Video", ignoreCase = true) || 
                item.title.equals("Ses Kaydı", ignoreCase = true) || 
                item.title.trim() == item.description.trim()

            if (!isGenericTitle) {
                Text(
                    text = item.title,
                    color = Color(0xFF232066),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    color = Color(0xFF334155),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ALT SADE İSTATİSTİK VE ETKİLEŞİM ÇUBUĞU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // İzlenme Sayısı (👁️ 27)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = "İzlenme",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${item.viewCount.takeIf { it > 0 } ?: 27}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Lider Emoji + Skor (❤️ 1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = item.topEmoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${item.reactionCount}",
                            color = Color(0xFF232066),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Paylaş",
                            tint = Color(0xFF232066),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onCommentClick(item) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Comment,
                            contentDescription = "Yorum",
                            tint = Color(0xFF232066),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
