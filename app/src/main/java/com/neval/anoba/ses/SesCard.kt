package com.neval.anoba.ses

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.ImageLoader
import com.neval.anoba.R
import com.neval.anoba.common.ui.AnobaBadge
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.common.utils.formatDuration
import com.neval.anoba.ses.sesemoji.SesReactionUsersBottomSheet
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.ui.theme.AnobaNavy
import com.neval.anoba.video.videoemoji.VideoEmojis
import org.koin.compose.koinInject
import kotlin.random.Random

@Composable
fun SesCard(
    ses: SesModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    isCommentSectionClickable: Boolean = false,
    showPlayIcon: Boolean = true,
    cardColor: Color = Color(0xFFFDFCFB),
    isPinned: Boolean = false,
    onPinClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onUserClick: ((String) -> Unit)? = null,
    imageLoader: ImageLoader = koinInject()
) {
    val topReaction = ses.reactions.maxByOrNull { it.value }
    val isSealed = ses.privacy == "SEALED"
    val isTimeCapsule = ses.unlockTimestamp != null && ses.unlockTimestamp!! > System.currentTimeMillis()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReactionUsersSheet by remember { mutableStateOf(false) }

    if (showReactionUsersSheet) {
        SesReactionUsersBottomSheet(
            sesId = ses.id,
            reactions = ses.reactions,
            onDismiss = { showReactionUsersSheet = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.mycontent_delete_dialog_title)) },
            text = { Text(stringResource(R.string.mycontent_delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick()
                }) {
                    Text(stringResource(R.string.mycontent_delete_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.mycontent_delete_dialog_cancel))
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.then(
                    if (onUserClick != null) Modifier.clickable { onUserClick.invoke(ses.ownerId) } else Modifier
                )
            ) {
                UserAvatar(
                    photoUrl = ses.userProfileImageUrl,
                    username = ses.username,
                    size = 32.dp,
                    imageLoader = imageLoader
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = ses.username.ifBlank { stringResource(id = R.string.sescard_anonymous) },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (ses.isOfficial) AnobaGold else MaterialTheme.colorScheme.onSurface
                    )
                    AnobaBadge(
                        badgeType = ses.userBadgeType,
                        isOfficial = ses.isOfficial
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (canDelete) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(onClick = onPinClick) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = "Pin",
                    tint = if (isPinned) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            val context = LocalContext.current
            val displayDate = ses.publishTimestamp ?: ses.createdAt
            Text(
                text = displayDate?.let { DateTimeUtils.formatRelativeTime(context, it.time) } ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate(Constants.SES_DETAIL_SCREEN.replace("{sesId}", ses.id)) },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isTimeCapsule) {
                            Icon(
                                imageVector = Icons.Default.WatchLater,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            if (isSealed) Spacer(Modifier.width(4.dp))
                        }

                        if (isSealed) {
                            Surface(
                                color = AnobaGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(10.dp),
                                        tint = Color(0xFF7B5E00)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(id = R.string.lettercard_recipient, ses.recipientUsername ?: ".."),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF7B5E00)
                                    )
                                }
                            }
                        }
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                    val random = Random(ses.id.hashCode())
                    val barCount = 45
                    val barWidth = 3.dp.toPx()
                    val barSpacing = 4.dp.toPx()
                    val cornerRadius = CornerRadius(4.dp.toPx())

                    for (i in 0 until barCount) {
                        val distanceFromCenter = kotlin.math.abs(i - barCount / 2f)
                        val multiplier = 1f - (distanceFromCenter / (barCount / 2f))
                        val barHeight = (random.nextInt(15, 60).dp.toPx() * multiplier) + 5.dp.toPx()

                        drawRoundRect(
                            color = Color(0xFF546E7A).copy(alpha = 0.7f),
                            topLeft = Offset(
                                x = i * (barWidth + barSpacing),
                                y = (size.height - barHeight) / 2
                            ),
                            size = Size(barWidth, barHeight),
                            cornerRadius = cornerRadius
                        )
                    }
                }

                if (showPlayIcon) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.8f),
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.PlayCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = AnobaGold
                            )
                        }
                    }
                }

                Text(
                    text = formatDuration(ses.duration, leadingZero = false),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }

        if (!ses.anobaResponse.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, AnobaGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Anoba Yanıtı",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = AnobaGold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = ses.anobaResponse!!,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = AnobaNavy.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ses.viewCount > 0) {
                    Icon(Icons.Default.RemoveRedEye, null, Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(ses.viewCount.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // 🚀 DÜZELTME: Toplam skor yerine sadece lider emojinin tekil skoru
                if (topReaction != null && topReaction.value > 0) {
                    val displayEmoji = VideoEmojis.getDisplayEmoji(topReaction.key)
                    Spacer(Modifier.width(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showReactionUsersSheet = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(displayEmoji, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${topReaction.value}", // 🚀 LİDER SKOR
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(start = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isSealed && !isTimeCapsule) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onShareClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        if (ses.shareCount > 0) {
                            Text(ses.shareCount.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = isCommentSectionClickable) {
                            navController.navigate(Constants.SES_COMMENTS_ROUTE.replace("{sesId}", ses.id))
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Comment, null, Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                    if (ses.commentsCount > 0) {
                        Text(ses.commentsCount.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (ses.personalNote.isNotBlank()) {
            var isExpanded by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .animateContentSize()
            ) {
                Text(
                    text = ses.personalNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
                if (!isExpanded && ses.personalNote.length > 80) {
                    Text(
                        text = "… devamı",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { isExpanded = true }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}