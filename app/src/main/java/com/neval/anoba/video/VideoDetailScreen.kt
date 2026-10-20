package com.neval.anoba.video

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil3.ImageLoader
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.ui.FollowButton
import com.neval.anoba.common.ui.OfficialBadge
import com.neval.anoba.common.ui.QuarantineBanner
import com.neval.anoba.common.ui.UserAvatar
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.MediaCacheManager
import com.neval.anoba.common.utils.formatDuration
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.photo.ShareState
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.video.videoemoji.VideoEmojiReactionRepository
import com.neval.anoba.video.videoemoji.VideoEmojiReactionRow
import com.neval.anoba.video.videoemoji.VideoEmojis
import com.neval.anoba.video.videoemoji.VideoReactionUsersBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoDetailScreen(
    videoId: String,
    navController: NavController,
    videoViewModel: VideoViewModel = koinViewModel(),
    authViewModel: com.neval.anoba.common.viewmodel.AuthViewModel = koinViewModel(),
    imageLoader: ImageLoader = koinInject(),
    reactionRepository: VideoEmojiReactionRepository = koinInject()
) {
    val video by videoViewModel.videoDetails.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val userRoleState by authViewModel.userRole.collectAsState()

    val currentUserId = (authState as? AuthState.Authenticated)?.uid
    val isAnoba = userRoleState?.equals("ADMIN", ignoreCase = true) == true ||
                  (authState as? AuthState.Authenticated)?.email == "morplato@gmail.com"

    val userReactions by videoViewModel.userReactions.collectAsState()
    val shareState by videoViewModel.shareState.collectAsState()

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    var showReactionsSheet by remember { mutableStateOf(false) }

    val reactionUsers by remember(videoId, showReactionsSheet) {
        if (showReactionsSheet) {
            reactionRepository.getVideoReactionUsers(videoId)
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

    val shareLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    DisposableEffect(isLandscape) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (isLandscape) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            val window = (view.context as? Activity)?.window
            window?.let {
                val insetsController = WindowCompat.getInsetsController(it, view)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(key1 = videoViewModel.eventFlow) {
        videoViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is VideoUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val shareChooserTitle = stringResource(R.string.video_detail_share_chooser_title)
    val sharePreparingMessage = stringResource(R.string.video_detail_share_preparing)

    LaunchedEffect(shareState) {
        when (val state = shareState) {
            is ShareState.Success -> {
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, state.uri)
                    type = "video/mp4"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, shareChooserTitle)

                shareLauncher.launch(chooser)
                videoViewModel.resetShareState()
            }
            is ShareState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                videoViewModel.resetShareState()
            }
            is ShareState.Processing -> {
                Toast.makeText(context, sharePreparingMessage, Toast.LENGTH_SHORT).show()
            }
            ShareState.Idle -> {}
        }
    }

    LaunchedEffect(videoId) {
        videoViewModel.loadVideoDetails(videoId)
        videoViewModel.incrementViewCount(videoId)
    }

    when (val state = video) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success<VideoModel> -> {
            val currentVideo = state.data
            val exoPlayer = remember(currentVideo.videoUrl) {
                MediaCacheManager.createPlayer(context.applicationContext).apply {
                    setMediaItem(MediaItem.fromUri(currentVideo.videoUrl))
                    prepare()
                }
            }

            var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
            var isBuffering by remember { mutableStateOf(false) }
            var totalDuration by remember { mutableLongStateOf(0L) }
            var currentTime by remember { mutableLongStateOf(0L) }
            var controlsVisible by remember { mutableStateOf(true) }
            var sliderPosition by remember { mutableFloatStateOf(0f) }
            var isSeeking by remember { mutableStateOf(false) }

            val scale = remember { Animatable(1f) }
            val offsetX = remember { Animatable(0f) }
            val offsetY = remember { Animatable(0f) }
            val dimAlpha by remember(scale.value) {
                derivedStateOf { ((scale.value - 1f) / 2f).coerceIn(0f, 0.7f) }
            }

            val heartScale = remember { Animatable(0f) }
            val heartAlpha = remember { Animatable(0f) }

            DisposableEffect(exoPlayer) {
                val listener = object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                        isPlaying = isPlayingValue
                        if (!isPlayingValue) {
                            controlsVisible = true
                        }
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isBuffering = playbackState == Player.STATE_BUFFERING
                        if (playbackState == Player.STATE_READY) {
                            totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                        }
                    }
                }
                exoPlayer.addListener(listener)
                onDispose {
                    exoPlayer.removeListener(listener)
                    exoPlayer.stop()
                    exoPlayer.release()
                }
            }

            LaunchedEffect(isPlaying, isSeeking) {
                while (isPlaying && !isSeeking) {
                    currentTime = exoPlayer.currentPosition.coerceAtLeast(0L)
                    sliderPosition = currentTime.toFloat()
                    delay(1000L)
                }
            }

            LaunchedEffect(controlsVisible, isPlaying) {
                if (controlsVisible && isPlaying) {
                    delay(3000L)
                    if (controlsVisible) {
                        controlsVisible = false
                    }
                }
            }

            Scaffold(
                containerColor = Color.Black,
                topBar = {
                    AnimatedVisibility(
                        visible = controlsVisible && !isLandscape && scale.value == 1f,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(
                                        photoUrl = currentVideo.userProfileImageUrl,
                                        username = currentVideo.username,
                                        size = 32.dp,
                                        imageLoader = imageLoader
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = currentVideo.username,
                                        color = if (currentVideo.isOfficial) AnobaGold else Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f, fill = false),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (currentVideo.isOfficial) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        OfficialBadge(size = 14.dp)
                                    }

                                    if (currentVideo.isPopular) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            color = AnobaGold,
                                            shape = CircleShape,
                                            modifier = Modifier.size(24.dp),
                                            shadowElevation = 4.dp
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("1k", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }

                                    FollowButton(
                                        targetUserId = currentVideo.ownerId,
                                        currentUserId = currentUserId,
                                        isDarkBackground = true,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { navController.popBackStack() }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        stringResource(id = R.string.video_detail_back),
                                        tint = Color.White
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    if (shareState !is ShareState.Processing) {
                                        videoViewModel.shareVideo(context, currentVideo.videoUrl, currentVideo.id)
                                    }
                                }) { Icon(Icons.Default.Share, stringResource(id = R.string.video_detail_share), tint = Color.White) }
                                IconButton(
                                    onClick = {
                                        navController.navigate(Constants.HOME_SCREEN) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Home, stringResource(id = R.string.videohome_home), tint = Color.White) }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = dimAlpha))
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
                                        videoViewModel.toggleReaction(currentVideo.id, "❤️")
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
                            )
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false
                                }
                            },
                            update = { view ->
                                view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            },
                            modifier = Modifier.align(Alignment.Center).fillMaxSize()
                        )
                    }

                    // --- KARANTİNA AFİŞİ VE KURTARMA BUTONU ---
                    Box(modifier = Modifier.zIndex(10f).align(Alignment.TopCenter).padding(innerPadding)) {
                        Column {
                            QuarantineBanner(isFlagged = currentVideo.isFlagged, flagReason = currentVideo.flagReason)
                            
                            if (currentVideo.isFlagged) {
                                Button(
                                    onClick = { 
                                        if (isAnoba) {
                                            videoViewModel.unflagVideo(currentVideo.id) 
                                        } else {
                                            Toast.makeText(context, "Sadece yetkililer karantinadan çıkarabilir.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAnoba) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.6f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isAnoba) "KARANTİNADAN ÇIKAR" else "İNCELEMEDE",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White.copy(alpha = 0.7f))
                        }
                    }

                    AnimatedVisibility(
                        visible = controlsVisible && scale.value == 1f,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f))) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(formatDuration(sliderPosition.toLong()), color = Color.White, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Slider(
                                        value = sliderPosition,
                                        onValueChange = {
                                            isSeeking = true
                                            sliderPosition = it
                                            controlsVisible = true
                                        },
                                        onValueChangeFinished = {
                                            exoPlayer.seekTo(sliderPosition.toLong())
                                            isSeeking = false
                                        },
                                        valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.Gray
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(formatDuration(totalDuration), color = Color.White, style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                        controlsVisible = true
                                    }) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = stringResource(id = R.string.videodetail_play_pause),
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = {
                                        exoPlayer.seekTo(0)
                                        controlsVisible = true
                                    }) { Icon(Icons.Default.SkipPrevious, stringResource(id = R.string.videodetail_restart), tint = Color.White) }
                                    Spacer(modifier = Modifier.width(2.dp))
                                    IconButton(onClick = {
                                        exoPlayer.seekBack()
                                        controlsVisible = true
                                    }) { Icon(Icons.Default.Replay10, stringResource(id = R.string.videodetail_rewind), tint = Color.White) }
                                    Spacer(modifier = Modifier.width(2.dp))
                                    IconButton(onClick = {
                                        exoPlayer.seekForward()
                                        controlsVisible = true
                                    }) { Icon(Icons.Default.Forward10, stringResource(id = R.string.videodetail_forward), tint = Color.White) }
                                    Spacer(modifier = Modifier.width(2.dp))
                                    var isMuted by remember { mutableStateOf(exoPlayer.volume == 0f) }
                                    IconButton(onClick = {
                                        isMuted = !isMuted
                                        exoPlayer.volume = if (isMuted) 0f else 1f
                                        controlsVisible = true
                                    }) {
                                        Icon(
                                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = if (isMuted) stringResource(id = R.string.videodetail_unmute) else stringResource(id = R.string.videodetail_mute),
                                            tint = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(2.dp))
                                    var playbackSpeed by remember { mutableFloatStateOf(1f) }
                                    val speeds = listOf(1f, 1.25f, 1.5f, 2f)
                                    IconButton(onClick = {
                                        val currentIndex = speeds.indexOf(playbackSpeed)
                                        playbackSpeed = speeds[(currentIndex + 1) % speeds.size]
                                        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
                                        controlsVisible = true
                                    }) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${if (playbackSpeed == 1f) "1" else playbackSpeed.toString()}x",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                            Text(
                                                text = stringResource(id = R.string.videodetail_playback_speed),
                                                modifier = Modifier.size(0.dp),
                                                color = Color.Transparent
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                if (!isLandscape) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(Modifier.width(12.dp))
                                        if (currentVideo.viewCount > 0) {
                                            Icon(Icons.Default.RemoveRedEye, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "${currentVideo.viewCount} ${stringResource(id = R.string.mycontent_sheet_views)}",
                                                color = Color.White.copy(alpha = 0.7f),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }

                                        val topReaction = currentVideo.reactions.maxByOrNull { it.value }
                                        if (topReaction != null && (topReaction.value > 0)) {
                                            Spacer(Modifier.width(16.dp))
                                            val displayEmoji = VideoEmojis.getDisplayEmoji(topReaction.key)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { showReactionsSheet = true }
                                                    .background(Color.White.copy(alpha = 0.1f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(displayEmoji, fontSize = 12.sp)
                                                Spacer(Modifier.width(3.dp))
                                                Text(
                                                    text = "${topReaction.value}", // 🚀 TEKİL LİDER SKOR
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.weight(1f))

                                        Row(
                                            modifier = Modifier.clickable {
                                                val route = Constants.VIDEO_COMMENTS_ROUTE.replace("{videoId}", videoId)
                                                navController.navigate(route)
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Comment, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            if (currentVideo.commentsCount > 0) {
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    stringResource(id = R.string.videodetail_comments_count, currentVideo.commentsCount),
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(18.dp))
                                    VideoEmojiReactionRow(
                                        reactions = currentVideo.reactions,
                                        selectedEmoji = userReactions[currentVideo.id],
                                        onReact = { emoji -> videoViewModel.toggleReaction(currentVideo.id, emoji) }
                                    )
                                    Spacer(Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        is UiState.Idle -> {}
    }
}
