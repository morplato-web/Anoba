package com.neval.anoba.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.res.stringResource
import com.neval.anoba.R
import com.neval.anoba.models.AnobaNotification

@Composable
fun NotificationHubCard(
    notifications: List<AnobaNotification>,
    hasUnread: Boolean,
    onNotificationClick: (AnobaNotification) -> Unit,
    onMarkAllAsReadClick: () -> Unit,
    onDeleteNotificationClick: (String) -> Unit, // 🚀 Tek bildirim silme callback
    onDeleteAllNotificationsClick: () -> Unit, // 🚀 Tümünü silme callback
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // ÜST BAŞLIK VE AKSİYON BUTONLARI (TÜMÜNÜ OKU / TÜMÜNÜ SİL)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.notif_hub_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFF232066)
                    )
                    if (hasUnread) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935))
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasUnread) {
                        TextButton(
                            onClick = onMarkAllAsReadClick,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Tümünü Okundu Say",
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = stringResource(R.string.notif_hub_mark_all_read),
                                fontSize = 11.sp,
                                color = Color(0xFF0288D1),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = onDeleteAllNotificationsClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Tümünü Temizle",
                                tint = Color.Gray.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (notifications.isEmpty()) {
                Text(
                    text = stringResource(R.string.notif_hub_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                notifications.take(5).forEach { notif ->
                    val bgColor = if (!notif.isRead) Color(0xFFFFF9E6) else Color(0xFFF8F9FA)
                    val titleColor = if (!notif.isRead) Color(0xFF232066) else Color.DarkGray

                    // 🚀 DÜZELTME: Veritabanından gelen mesaj boşsa veya teknikse otomatik yerelleştirme yapıyoruz.
                    val displayMessage = if (notif.message.isBlank() || notif.type.startsWith("POPULAR") || notif.type.contains("QUARANTINE")) {
                        getLocalizedMessage(notif.type)
                    } else {
                        notif.message
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onNotificationClick(notif) },
                        color = bgColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NotificationAvatarWithBadge(
                                photoUrl = notif.senderPhotoUrl,
                                senderName = notif.senderName,
                                notifType = notif.type
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title.ifBlank { notif.senderName.ifBlank { "Anoba" } },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = titleColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = displayMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = Color.Gray,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!notif.isRead) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD4AF37))
                                )
                            }

                            // 🚀 TEK BİLDİRİMİ SİLME BUTONU
                            IconButton(
                                onClick = { onDeleteNotificationClick(notif.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Bildirimi Sil",
                                    tint = Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getLocalizedMessage(type: String): String {
    val upper = type.uppercase()
    return when {
        upper == "POPULAR_OWNER_ALERT" -> stringResource(R.string.notif_type_popular_owner)
        upper == "POPULAR_REACTOR_ALERT" -> stringResource(R.string.notif_type_popular_reactor)
        upper == "QUARANTINE_ALERT" -> stringResource(R.string.notif_type_quarantine_owner)
        upper == "REPORTER_QUARANTINE" -> stringResource(R.string.notif_type_reporter_quarantine)
        upper == "REPORTER_UNFLAG" -> stringResource(R.string.notif_type_reporter_unflag)
        upper.contains("LIKE") || upper.contains("REACTION") -> stringResource(R.string.notif_type_reaction)
        upper.contains("COMMENT") -> stringResource(R.string.notif_type_comment)
        else -> stringResource(R.string.notif_type_generic)
    }
}

@Composable
fun NotificationAvatarWithBadge(
    photoUrl: String?,
    senderName: String,
    notifType: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val upper = notifType.uppercase()
    val isLikeNotif = upper.contains("LIKE") || upper.contains("REACTION")

    if (isLikeNotif) {
        // 🚀 Tepki/Beğeni bildirimlerinde profil fotoğrafının köşesindeki kalp rozetini kaldırıyoruz (Sadece temiz profil avatarı)
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = senderName,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(38.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = modifier
                    .size(38.dp)
                    .clip(CircleShape)
            ) {
                DefaultAvatarPlaceholder(senderName)
            }
        }
    } else {
        // Yorum, Sohbet, Karantina vb. diğer bildirim türleri için rozetli gösterim
        val (badgeIcon, badgeBgColor, badgeIconColor) = getBadgeDetails(notifType)

        Box(modifier = modifier.size(38.dp)) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = senderName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopStart)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    DefaultAvatarPlaceholder(senderName)
                }
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(1.dp)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(badgeBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeIconColor,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultAvatarPlaceholder(name: String) {
    val initial = name.trim().take(1).uppercase().ifBlank { "?" }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242),
            fontSize = 14.sp
        )
    }
}

private fun getBadgeDetails(type: String): Triple<ImageVector, Color, Color> {
    val upper = type.uppercase()
    return when {
        upper.startsWith("REPORTER_") || upper == "QUARANTINE_ALERT" -> 
            Triple(Icons.Default.Shield, Color(0xFFE8F5E9), Color(0xFF4CAF50))
        upper.contains("LIKE") -> Triple(Icons.Default.Favorite, Color(0xFFFFEBEE), Color(0xFFE53935))
        upper.contains("COMMENT") -> Triple(Icons.AutoMirrored.Filled.Comment, Color(0xFFE3F2FD), Color(0xFF1976D2))
        upper.startsWith("VIDEO") -> Triple(Icons.Default.Videocam, Color(0xFFF3E5F5), Color(0xFF8E24AA))
        upper.startsWith("PHOTO") -> Triple(Icons.Default.CameraAlt, Color(0xFFFFEBEE), Color(0xFFD32F2F))
        upper.startsWith("LETTER") -> Triple(Icons.Default.Edit, Color(0xFFE1F5FE), Color(0xFF0288D1))
        upper.startsWith("CHAT") -> Triple(Icons.AutoMirrored.Filled.Chat, Color(0xFFE8F5E9), Color(0xFF2E7D32))
        upper.startsWith("SES") || upper.startsWith("AUDIO") -> Triple(Icons.Default.GraphicEq, Color(0xFFFFF3E0), Color(0xFFF57C00))
        else -> Triple(Icons.Default.Notifications, Color(0xFFECEFF1), Color(0xFF455A64))
    }
}