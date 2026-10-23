package com.neval.anoba.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ÜST BANT (Kullanıcı Avatarı, İsmi, Yetki Rozeti & İçerik Tipi Rozeti)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                color = Color.White,
                                fontSize = 14.sp,
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
                        val timeStr = getRelativeTimeString(item.createdAtMillis)
                        if (timeStr.isNotBlank()) {
                            Text(
                                text = timeStr,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // İçerik Tipi Rozeti
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (typeIcon, typeLabel, typeColor) = when (item.type) {
                            ContentType.PHOTO -> Triple(Icons.Default.CameraAlt, "Fotoğraf", Color(0xFFE53935))
                            ContentType.VIDEO -> Triple(Icons.Default.Videocam, "Video", Color(0xFF8E24AA))
                            ContentType.LETTER -> Triple(Icons.Default.Edit, "Mektup", Color(0xFF0288D1))
                            ContentType.AUDIO -> Triple(Icons.Default.GraphicEq, "Ses", Color(0xFFF57C00))
                            ContentType.MARKET -> Triple(Icons.Default.ShoppingBag, "Pazar Yeri", Color(0xFFFFD700))
                        }
                        Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(typeLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // MEDYA GÖRSEL ALANI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.mediaUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.mediaUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val fallbackIcon = when (item.type) {
                        ContentType.PHOTO -> Icons.Default.CameraAlt
                        ContentType.VIDEO -> Icons.Default.Videocam
                        ContentType.LETTER -> Icons.Default.Edit
                        ContentType.AUDIO -> Icons.Default.GraphicEq
                        ContentType.MARKET -> Icons.Default.ShoppingBag
                    }
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = null,
                        tint = Color(0xFFFFD700).copy(alpha = 0.7f),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // ALT AÇIKLAMA VE SADE İSTATİSTİK BANT (Kural: Sadece Lider Emoji + Skor)
            Column(modifier = Modifier.padding(12.dp)) {
                if (item.title.isNotBlank()) {
                    Text(
                        text = item.title,
                        color = Color.White,
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
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sadece Lider Emoji + Skor (PROJECT_GUIDE Kuralı)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = item.topEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${item.reactionCount}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onCommentClick(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Comment,
                                contentDescription = "Yorum Yap",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Paylaş",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
