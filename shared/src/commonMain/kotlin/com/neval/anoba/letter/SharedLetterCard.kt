package com.neval.anoba.letter

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.getRelativeTimeString
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy

@Composable
fun SharedLetterCard(
    letter: SharedLetterModel,
    modifier: Modifier = Modifier,
    currentUserId: String = "",
    cardColor: Color = Color(0xFFFFF3E0),
    isPinned: Boolean = false,
    hasNotification: Boolean = false,
    onPinClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onLetterClick: (SharedLetterModel) -> Unit = {},
    onCommentClick: (SharedLetterModel) -> Unit = {},
    onUserClick: ((String) -> Unit)? = null
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Mektubu Sil") },
            text = { Text("Bu mektubu silmek istediğinize emin misiniz?") },
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

    val isAudio = !letter.audioUrl.isNullOrBlank()
    val isSealed = letter.privacy == "SEALED"
    val now = 1710000000000L // current fallback time if needed
    val isTimeCapsule = letter.unlockTimestampMillis != null && letter.unlockTimestampMillis!! > now
    val isLocked = isSealed || isTimeCapsule

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // 1. ÜST BAŞLIK: Profil Resmi + İsim + Pin + Tarih + Bildirim Noktası
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayName = letter.username.ifBlank { "Anonim" }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .then(
                        if (onUserClick != null) Modifier.clickable { onUserClick.invoke(letter.ownerId) } else Modifier
                    )
                    .weight(1f)
            ) {
                UserAvatar(
                    photoUrl = letter.userProfileImageUrl,
                    username = displayName,
                    size = 28.dp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = displayName,
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = if (letter.isOfficial) AnobaGold else AnobaNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    AnobaBadge(
                        badgeType = letter.userBadgeType,
                        isOfficial = letter.isOfficial
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canDelete) {
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(onClick = onPinClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin",
                        tint = if (isPinned) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (hasNotification) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                    )
                }

                val timeStr = getRelativeTimeString(letter.createdAtMillis)
                if (timeStr.isNotBlank()) {
                    Text(
                        text = timeStr,
                        style = TextStyle(fontSize = 9.sp),
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }

        // 2. MEKTUP ZARFI KARTI (160dp Sabit Yükseklik)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable { onLetterClick(letter) },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isTimeCapsule -> Color(0xFFF5F5F5)
                    isSealed -> Color(0xFFFFF9C4)
                    else -> cardColor
                }
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Sağ Üstte Açık Mektup / Ses Rozeti
                Icon(
                    imageVector = if (isAudio) Icons.Default.Mic else Icons.Default.Mail,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 10.dp)
                        .size(16.dp),
                    tint = AnobaNavy.copy(alpha = 0.25f)
                )

                if (isLocked) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isTimeCapsule) {
                                Icon(
                                    imageVector = Icons.Default.WatchLater,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.Gray.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            if (isSealed) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.Gray.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Icon(
                                imageVector = if (isAudio) Icons.Default.Mic else Icons.Default.Mail,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else if (isAudio) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(AnobaGold.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = AnobaGold
                            )
                        }

                        if (letter.audioDuration != null && letter.audioDuration!! > 0L) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "${letter.audioDuration}s",
                                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            )
                        }
                    }
                } else {
                    var isLetterExpanded by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .animateContentSize()
                    ) {
                        val cardTitle = letter.title
                        if (!cardTitle.isNullOrBlank()) {
                            Text(
                                text = cardTitle,
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                color = AnobaNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = letter.content,
                            style = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
                            color = Color.Black.copy(alpha = 0.8f),
                            maxLines = if (isLetterExpanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 3. ALT BİLGİ: Göz, Emoji, Paylaş, Yorum
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (letter.viewCount > 0) {
                    Icon(Icons.Default.RemoveRedEye, null, Modifier.size(14.dp), Color.Gray)
                    Text("${letter.viewCount}", Modifier.padding(start = 2.dp, end = 8.dp), Color.Gray, 10.sp)
                }

                if (letter.reactionCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(letter.topEmoji, fontSize = 14.sp)
                        Text(
                            text = "${letter.reactionCount}",
                            modifier = Modifier.padding(start = 2.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isSealed && !isTimeCapsule) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onShareClick() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(16.dp), AnobaNavy.copy(alpha = 0.5f))
                    }
                    Spacer(Modifier.width(4.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onCommentClick(letter) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Comment, null, Modifier.size(16.dp), AnobaNavy)
                    if (letter.commentsCount > 0) {
                        Text("${letter.commentsCount}", Modifier.padding(start = 2.dp), fontWeight = FontWeight.Bold, color = AnobaNavy, fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
