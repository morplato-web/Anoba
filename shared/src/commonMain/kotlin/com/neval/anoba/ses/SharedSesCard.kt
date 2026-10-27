package com.neval.anoba.ses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.getRelativeTimeString
import com.neval.anoba.ui.theme.AnobaGold

private val CardBackgroundPink = Color(0xFFFFE4E6)
private val DarkText = Color(0xFF1E293B)

@Composable
fun SharedSesCard(
    ses: SharedSesModel,
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
    canDelete: Boolean = false,
    onPinClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onSesClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onLikeClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Ses Kaydını Sil") },
            text = { Text("Bu ses kaydını silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick()
                }) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundPink),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Başlık & Profil Barı
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    UserAvatar(
                        photoUrl = ses.userProfileImageUrl,
                        username = ses.username,
                        size = 36.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ses.username,
                                color = DarkText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            AnobaBadge(
                                badgeType = ses.userBadgeType,
                                isOfficial = ses.isOfficial
                            )
                        }
                        if (ses.title.isNotBlank()) {
                            Text(
                                text = ses.title,
                                color = DarkText.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPinClick) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Sabitle",
                            tint = if (isPinned) Color(0xFF2196F3) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (canDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ses Oynatıcı Kartı
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSesClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE11D48).copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Ses Kaydı",
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (ses.title.isNotBlank()) ses.title else "Sesli Not",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DarkText
                        )
                        Text(
                            text = if (ses.durationText.isNotBlank()) ses.durationText else "Sesli Not Dinle",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = onSesClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE11D48))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Oynat",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            if (ses.personalNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ses.personalNote,
                    color = DarkText.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alt Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onLikeClick() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (ses.isLikedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Beğen",
                            tint = if (ses.isLikedByMe) Color(0xFFE11D48) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${ses.reactionCount}",
                            color = DarkText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCommentClick() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Comment,
                            contentDescription = "Yorumlar",
                            tint = DarkText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${ses.commentsCount}",
                            color = DarkText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "Görüntülenme",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${ses.viewCount}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
