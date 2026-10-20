package com.neval.anoba.video

import androidx.annotation.OptIn
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.PlayCircleOutline
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.common.utils.formatDuration
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.video.videoemoji.VideoEmojiReactionRepository
import com.neval.anoba.video.videoemoji.VideoEmojis
import com.neval.anoba.video.videoemoji.VideoReactionUsersBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(UnstableApi::class)
@Composable
fun VideoCard(
    video: VideoModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer? = null,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    isCommentSectionClickable: Boolean = false,
    isPinned: Boolean = false,
    onPinClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onUserClick: ((String) -> Unit)? = null,
    imageLoader: ImageLoader = koinInject(),
    onDoubleTap: (VideoModel) -> Unit = {},
    reactionRepository: VideoEmojiReactionRepository = koinInject()
) {
    val topReaction = video.reactions.maxByOrNull { it.value }
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReactionsSheet by remember { mutableStateOf(false) }

    var isFirstFrameRendered by remember { mutableStateOf(false) }

    DisposableEffect(isPlaying, exoPlayer) {
        isFirstFrameRendered = false
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                isFirstFrameRendered = true
            }
        }
        if (isPlaying && exoPlayer != null) {
            exoPlayer.addListener(listener)
        }
        onDispose {
            exoPlayer?.removeListener(listener)
            isFirstFrameRendered = false
        }
    }

    val reactionUsers by remember(video.id, showReactionsSheet) {
        if (showReactionsSheet) {
            reactionRepository.getVideoReactionUsers(video.id)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    if (showReactionsSheet) {
        VideoReactionUsersBottomSheet(
            reactionUsers = reactionUsers,
            onDismiss = { showReactionsSheet = false }
        )
    }

    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.videocard_delete_confirm_title)) },
            text = { Text(stringResource(R.string.videocard_delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick()
                }) {
                    Text(stringResource(R.string.videocard_delete_confirm_yes), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.videocard_delete_confirm_no))
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. ÜST BAŞLIK (Profil, İsim, Pin, Tarih)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .then(
                            if (onUserClick != null) Modifier.clickable { onUserClick.invoke(video.ownerId) } else Modifier
                        )
                        .weight(1f)
                ) {
                    UserAvatar(
                        photoUrl = video.userProfileImageUrl,
                        username = video.username,
                        size = 32.dp,
                        imageLoader = imageLoader
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = video.username,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (video.isOfficial) AnobaGold else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    com.neval.anoba.common.ui.AnobaBadge(
                        badgeType = video.userBadgeType,
                        isOfficial = video.isOfficial
                    )
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
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    val context = LocalContext.current
                    val displayDate = video.publishTimestamp ?: video.createdAt
                    displayDate?.let {
                        Text(
                            text = DateTimeUtils.formatRelativeTime(context, it.time),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Visible,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            // 2. VİDEO ALANI
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onClick() },
                            onDoubleTap = {
                                scope.launch {
                                    onDoubleTap(video)
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
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 5f)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying && exoPlayer != null) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    player = exoPlayer
                                    layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            update = { playerView ->
                                if (playerView.player != exoPlayer) {
                                    playerView.player = exoPlayer
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (!isPlaying || !isFirstFrameRendered) {
                        AsyncImage(
                            model = video.thumbnailUrl ?: video.videoUrl,
                            imageLoader = imageLoader,
                            contentDescription = stringResource(id = R.string.videocard_video_title, video.title ?: ""),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            placeholder = painterResource(id = R.drawable.ic_default_profile),
                            error = painterResource(id = R.drawable.ic_default_profile)
                        )
                    }

                    if (!isPlaying) {
                        Icon(
                            imageVector = Icons.Filled.PlayCircleOutline,
                            contentDescription = stringResource(id = R.string.videocard_play_video),
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(64.dp)
                        )
                    }

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

                    video.duration?.let {
                        Text(
                            text = formatDuration(it, leadingZero = false),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 3. ALT BİLGİ (İzlenme, Emoji, Skor, Paylaşım, Yorum)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (video.viewCount > 0) {
                        Icon(Icons.Default.RemoveRedEye, null, Modifier.size(18.dp), Color.Gray)
                        Text(
                            text = video.viewCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                            color = Color.Gray
                        )
                    }

                    // 🚀 DÜZELTME: Toplam skor yerine sadece lider emojinin tekil skoru
                    if (topReaction != null && topReaction.value > 0) {
                        val displayEmoji = VideoEmojis.getDisplayEmoji(topReaction.key)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showReactionsSheet = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(displayEmoji, fontSize = 15.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${topReaction.value}", // 🚀 LİDER SKOR
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                    }
                    if (video.shareCount > 0) {
                        Text(video.shareCount.toString(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (isCommentSectionClickable) {
                                val route = Constants.VIDEO_COMMENTS_ROUTE.replace("{videoId}", video.id)
                                navController.navigate(route)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Comment, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                    }
                    if (video.commentsCount > 0) {
                        Text(video.commentsCount.toString(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            if (video.personalNote.isNotBlank()) {
                var isExpanded by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .animateContentSize()
                ) {
                    Text(
                        text = video.personalNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )
                    if (!isExpanded && video.personalNote.length > 80) {
                        Text(
                            text = "... devamı",
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