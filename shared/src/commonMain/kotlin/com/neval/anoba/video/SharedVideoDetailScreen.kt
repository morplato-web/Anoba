package com.neval.anoba.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.neval.anoba.photo.SharedReactionUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AnobaGold = Color(0xFFFFD700)

fun formatDurationMillis(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedVideoDetailScreen(
    video: SharedVideoDetailModel? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    currentUserId: String? = null,
    isAdminOrModerator: Boolean = false,
    userReactions: Map<String, String> = emptyMap(),
    reactionUsers: List<SharedReactionUser> = emptyList(),
    isFollowingAuthor: Boolean = false,
    isPlaying: Boolean = false,
    isBuffering: Boolean = false,
    currentPositionMillis: Long = 0L,
    durationMillis: Long = 0L,
    isMuted: Boolean = false,
    playbackSpeed: Float = 1f,
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onShareClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onOpenComments: () -> Unit = {},
    onReactionClick: (String) -> Unit = {},
    onToggleFollow: () -> Unit = {},
    onUnflagVideo: () -> Unit = {},
    onRequestReactionUsers: () -> Unit = {},
    onPlayPauseToggle: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onRestart: () -> Unit = {},
    onSeekBack: () -> Unit = {},
    onSeekForward: () -> Unit = {},
    onMuteToggle: () -> Unit = {},
    onPlaybackSpeedChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
    videoPlayerContent: (@Composable () -> Unit)? = null
) {
    var showReactionsSheet by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    val dimAlpha by remember(scale.value) {
        derivedStateOf { ((scale.value - 1f) / 2f).coerceIn(0f, 0.7f) }
    }

    LaunchedEffect(currentPositionMillis, isSeeking) {
        if (!isSeeking) {
            sliderPosition = currentPositionMillis.toFloat()
        }
    }

    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(3500L)
            controlsVisible = false
        }
    }

    if (showReactionsSheet) {
        SharedVideoReactionUsersBottomSheet(
            reactionUsers = reactionUsers,
            onDismiss = { showReactionsSheet = false }
        )
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            AnimatedVisibility(
                visible = controlsVisible && scale.value == 1f,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (video != null && (currentUserId == video.ownerId || isAdminOrModerator)) {
                            IconButton(onClick = onEditClick) {
                                Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = Color.White)
                            }
                        }
                        IconButton(onClick = onShareClick) {
                            Icon(Icons.Default.Share, contentDescription = "Paylaş", tint = Color.White)
                        }
                        IconButton(onClick = onHomeClick) {
                            Icon(Icons.Default.Home, contentDescription = "Ana Sayfa", tint = Color.White)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                isLoading && video == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                errorMessage != null && video == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
                    }
                }

                video != null -> {
                    val topReaction = video.reactions.maxByOrNull { it.value }

                    if (video.isFlagged) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(10f)
                                .align(Alignment.TopCenter)
                                .background(Color(0xFFDC2626).copy(alpha = 0.9f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = video.flagReason ?: "Bu video inceleme altındadır.",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (isAdminOrModerator) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onUnflagVideo,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Karantinadan Çıkar", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = dimAlpha))
                            .clipToBounds()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scope.launch {
                                        val newScale = (scale.value * zoom).coerceIn(1f, 4f)
                                        scale.snapTo(newScale)
                                        if (newScale > 1f) {
                                            offsetX.snapTo(offsetX.value + pan.x)
                                            offsetY.snapTo(offsetY.value + pan.y)
                                        }
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.changes.all { !it.pressed }) {
                                            scope.launch {
                                                launch { scale.animateTo(1f) }
                                                launch { offsetX.animateTo(0f) }
                                                launch { offsetY.animateTo(0f) }
                                            }
                                        }
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        scope.launch {
                                            onReactionClick("❤️")
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
                            }
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (scale.value == 1f) {
                                    controlsVisible = !controlsVisible
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale.value,
                                    scaleY = scale.value,
                                    translationX = offsetX.value,
                                    translationY = offsetY.value
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (videoPlayerContent != null) {
                                videoPlayerContent()
                            } else {
                                if (video.thumbnailUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = video.thumbnailUrl,
                                        contentDescription = video.title,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("🎬 Video", color = Color.Gray, fontSize = 16.sp)
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.Center)
                                .graphicsLayer(
                                    scaleX = heartScale.value,
                                    scaleY = heartScale.value,
                                    alpha = heartAlpha.value
                                )
                                .zIndex(1f)
                        )

                        if (isBuffering) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color.White.copy(alpha = 0.8f))
                            }
                        }

                        AnimatedVisibility(
                            visible = controlsVisible && scale.value == 1f,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.55f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF334155)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!video.userProfileImageUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = video.userProfileImageUrl,
                                                    contentDescription = video.username,
                                                    modifier = Modifier.matchParentSize()
                                                )
                                            } else {
                                                Text(
                                                    text = video.username.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Text(
                                            text = video.username.ifBlank { "Anonim" },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (video.isOfficial) AnobaGold else Color.White,
                                            modifier = Modifier.weight(1f, fill = false),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (currentUserId != null && currentUserId != video.ownerId) {
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Surface(
                                                color = if (isFollowingAuthor) Color.White.copy(alpha = 0.2f) else AnobaGold,
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .clickable { onToggleFollow() }
                                            ) {
                                                Text(
                                                    text = if (isFollowingAuthor) "Takipte" else "Takip Et",
                                                    color = if (isFollowingAuthor) Color.White else Color.Black,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (video.personalNote.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = video.personalNote,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 13.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatDurationMillis(sliderPosition.toLong()),
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Slider(
                                            value = sliderPosition,
                                            onValueChange = {
                                                isSeeking = true
                                                sliderPosition = it
                                                controlsVisible = true
                                            },
                                            onValueChangeFinished = {
                                                onSeekTo(sliderPosition.toLong())
                                                isSeeking = false
                                            },
                                            valueRange = 0f..durationMillis.toFloat().coerceAtLeast(1f),
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color.White,
                                                activeTrackColor = Color.White,
                                                inactiveTrackColor = Color.Gray
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = formatDurationMillis(durationMillis),
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = {
                                            onPlayPauseToggle()
                                            controlsVisible = true
                                        }) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Oynat/Durdur",
                                                tint = Color.White,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }

                                        IconButton(onClick = {
                                            onRestart()
                                            controlsVisible = true
                                        }) {
                                            Icon(Icons.Default.SkipPrevious, contentDescription = "Başa Sar", tint = Color.White)
                                        }

                                        IconButton(onClick = {
                                            onSeekBack()
                                            controlsVisible = true
                                        }) {
                                            Icon(Icons.Default.Replay10, contentDescription = "10sn Geri", tint = Color.White)
                                        }

                                        IconButton(onClick = {
                                            onSeekForward()
                                            controlsVisible = true
                                        }) {
                                            Icon(Icons.Default.Forward10, contentDescription = "10sn İleri", tint = Color.White)
                                        }

                                        IconButton(onClick = {
                                            onMuteToggle()
                                            controlsVisible = true
                                        }) {
                                            Icon(
                                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Ses Aç/Kapat",
                                                tint = Color.White
                                            )
                                        }

                                        val speeds = listOf(1f, 1.25f, 1.5f, 2f)
                                        IconButton(onClick = {
                                            val nextSpeed = speeds[(speeds.indexOf(playbackSpeed) + 1) % speeds.size]
                                            onPlaybackSpeedChange(nextSpeed)
                                            controlsVisible = true
                                        }) {
                                            Text(
                                                text = "${if (playbackSpeed == 1f) "1" else playbackSpeed}x",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .clickable { onOpenComments() }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            if (video.commentsCount > 0) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(video.commentsCount.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (topReaction != null && topReaction.value > 0) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    onRequestReactionUsers()
                                                    showReactionsSheet = true
                                                }
                                                .background(Color.White.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(topReaction.key, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${topReaction.value}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    SharedVideoEmojiReactionRow(
                                        reactions = video.reactions,
                                        selectedEmoji = userReactions[video.id],
                                        onReact = onReactionClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
