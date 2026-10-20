package com.neval.anoba.photo

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.photo.photoemoji.PhotoEmojiReactionRepository
import com.neval.anoba.photo.photoemoji.PhotoReactionUsersBottomSheet
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.video.videoemoji.VideoEmojis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PhotoCard(
    photo: PhotoModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    isCommentSectionClickable: Boolean = true,
    isPinned: Boolean = false,
    onPinClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onUserClick: ((String) -> Unit)? = null,
    onDoubleTap: (PhotoModel) -> Unit = {},
    imageLoader: ImageLoader = koinInject(),
    reactionRepository: PhotoEmojiReactionRepository = koinInject()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReactionsSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val reactionUsers by remember(photo.id, showReactionsSheet) {
        if (showReactionsSheet) {
            reactionRepository.getPhotoReactionUsers(photo.id)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    if (showReactionsSheet) {
        PhotoReactionUsersBottomSheet(
            reactionUsers = reactionUsers,
            onDismiss = { showReactionsSheet = false }
        )
    }

    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }

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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .then(
                                if (onUserClick != null) Modifier.clickable { onUserClick.invoke(photo.ownerId) } else Modifier
                            )
                            .weight(1f)
                    ) {
                        UserAvatar(
                            photoUrl = photo.userProfileImageUrl,
                            username = photo.username,
                            size = 32.dp,
                            imageLoader = imageLoader
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = photo.username.ifBlank { stringResource(id = R.string.photocard_anonymous) },
                            style = MaterialTheme.typography.titleMedium,
                            color = if (photo.isOfficial) AnobaGold else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        com.neval.anoba.common.ui.AnobaBadge(
                            badgeType = photo.userBadgeType,
                            isOfficial = photo.isOfficial
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                tint = if (isPinned) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.5f
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        val context = LocalContext.current
                        val displayDate = photo.publishTimestamp ?: photo.createdAt
                        Text(
                            text = displayDate?.let {
                                DateTimeUtils.formatRelativeTime(
                                    context,
                                    it.time
                                )
                            } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 🚀 DİNAMİK ORANLI FOTOĞRAF ALANI (Kırpılmayı engeller, orijinal en/boyu korur)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.1f))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    navController.navigate(
                                        Constants.PHOTO_DETAIL_SCREEN.replace("{photoId}", photo.id)
                                    )
                                },
                                onDoubleTap = {
                                    scope.launch {
                                        onDoubleTap(photo)
                                        heartScale.snapTo(0f)
                                        heartAlpha.snapTo(1f)
                                        launch { heartScale.animateTo(1.5f, spring(dampingRatio = 0.5f)) }
                                        launch {
                                            delay(600)
                                            heartAlpha.animateTo(0f, tween(300))
                                        }
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = photo.photoUrl,
                        contentDescription = stringResource(
                            id = R.string.photocard_photo_description,
                            photo.id
                        ),
                        imageLoader = imageLoader,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth,
                        placeholder = painterResource(id = R.drawable.ic_default_profile),
                        error = painterResource(id = R.drawable.ic_default_profile)
                    )

                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(100.dp)
                            .graphicsLayer(
                                scaleX = heartScale.value,
                                scaleY = heartScale.value,
                                alpha = heartAlpha.value
                            )
                            .zIndex(1f)
                    )
                }

                val topReaction = photo.reactions.maxByOrNull { it.value }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (photo.viewCount > 0) {
                            Icon(Icons.Default.RemoveRedEye, null, Modifier.size(16.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = photo.viewCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (topReaction != null && topReaction.value > 0) {
                            val displayEmoji = VideoEmojis.getDisplayEmoji(topReaction.key)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showReactionsSheet = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(displayEmoji, fontSize = 14.sp)
                                Text(
                                    text = "${topReaction.value}", // 🚀 TOPLAM YERİNE TEKİL LİDER SKOR
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(start = 3.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onShareClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Share, null, Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            if (photo.shareCount > 0) {
                                Text(photo.shareCount.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.width(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = isCommentSectionClickable) {
                                    val route = Constants.PHOTO_COMMENTS_ROUTE.replace("{photoId}", photo.id)
                                    navController.navigate(route)
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Comment, null, Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                            if (photo.commentsCount > 0) {
                                Text(photo.commentsCount.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                if (photo.personalNote.isNotBlank()) {
                    var isExpanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .animateContentSize()
                    ) {
                        Text(
                            text = photo.personalNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { isExpanded = !isExpanded }
                        )
                        if (!isExpanded && photo.personalNote.length > 80) {
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
    }
}