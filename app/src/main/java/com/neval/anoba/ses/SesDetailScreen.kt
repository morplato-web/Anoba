package com.neval.anoba.ses

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
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
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.common.utils.formatDuration
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.photo.ShareState
import com.neval.anoba.ses.sesemoji.SesEmojiReactionRow
import com.neval.anoba.ses.sesemoji.SesReactionUsersBottomSheet
import com.neval.anoba.ui.theme.AnobaGold
import com.neval.anoba.video.videoemoji.VideoEmojis
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SoundWaveAnimation(isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "sound_wave_transition")
    val waveHeights = List(5) {
        val duration = remember { (900..1500).random() }
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = if (isPlaying) 1f else 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp)
    ) {
        val barCount = 50
        val barWidth = size.width / (barCount * 2)
        val maxBarHeight = size.height * 0.8f

        for (i in 0 until barCount) {
            val waveIndex = i % waveHeights.size
            val currentHeight = maxBarHeight * if (isPlaying) waveHeights[waveIndex].value else 0.1f
            val yOffset = (size.height - currentHeight) / 2

            drawRoundRect(
                color = Color.LightGray,
                topLeft = Offset(x = i * barWidth * 2 + barWidth / 2, y = yOffset),
                size = Size(width = barWidth, height = currentHeight),
                cornerRadius = CornerRadius(barWidth / 2)
            )
        }
    }
}

@SuppressLint("ContextCast")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SesDetailScreen(
    sesId: String,
    navController: NavController,
    viewModel: SesViewModel = koinViewModel(),
    authViewModel: com.neval.anoba.common.viewmodel.AuthViewModel = koinViewModel(),
    imageLoader: ImageLoader = koinInject()
) {
    val sesState by viewModel.sesDetails.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val userRoleState by authViewModel.userRole.collectAsState()

    val currentUserId = (authState as? AuthState.Authenticated)?.uid
    val isAnoba = userRoleState?.equals("ADMIN", ignoreCase = true) == true ||
                  (authState as? AuthState.Authenticated)?.email == "morplato@gmail.com"

    val shareState by viewModel.shareState.collectAsState()

    var showReactionUsersSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val shareChooserTitle = stringResource(id = R.string.sesdetail_share_chooser_title)
    val sharePreparingText = stringResource(id = R.string.sesdetail_share_preparing)
    val backText = stringResource(id = R.string.sesdetail_back)
    val shareText = stringResource(id = R.string.sesdetail_share)
    val homeText = stringResource(id = R.string.sesdetail_home)
    val viewCountText = stringResource(id = R.string.sesdetail_view_count)
    val commentText = stringResource(id = R.string.sesdetail_comment)
    val closeButtonText = stringResource(id = R.string.sesdetail_close_button)
    val anonymousText = stringResource(id = R.string.sesdetail_anonymous)
    val stopText = stringResource(id = R.string.sesdetail_stop)
    val playText = stringResource(id = R.string.sesdetail_play)

    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    LaunchedEffect(shareState) {
        when (val state = shareState) {
            is ShareState.Success -> {
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, state.uri)
                    type = "audio/m4a"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, shareChooserTitle)
                shareLauncher.launch(chooser)
                viewModel.resetShareState()
            }
            is ShareState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetShareState()
            }
            is ShareState.Processing -> {
                Toast.makeText(context, sharePreparingText, Toast.LENGTH_SHORT).show()
            }
            ShareState.Idle -> { }
        }
    }

    LaunchedEffect(sesId) {
        viewModel.loadSesDetails(sesId)
        viewModel.incrementViewCount(sesId)
    }

    when (val state = sesState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(id = R.string.sesdetail_loading))
                }
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success<SesModel> -> {
            val currentSes = state.data

            if (showReactionUsersSheet) {
                SesReactionUsersBottomSheet(
                    sesId = currentSes.id,
                    reactions = currentSes.reactions,
                    onDismiss = { showReactionUsersSheet = false }
                )
            }

            val playbackStartTimeMs = currentSes.trimStartMs ?: 0L
            val playbackEndTimeMs = currentSes.trimEndMs ?: currentSes.duration
            val clippedDuration = playbackEndTimeMs - playbackStartTimeMs

            var isPlaying by remember { mutableStateOf(false) }
            var currentPositionInClip by remember { mutableLongStateOf(0L) }

            val exoPlayer = remember(currentSes.audioUrl) {
                com.neval.anoba.common.utils.MediaCacheManager.createPlayer(context.applicationContext).apply {
                    val clippingBuilder = MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(playbackStartTimeMs)

                    if (playbackEndTimeMs > playbackStartTimeMs) {
                        clippingBuilder.setEndPositionMs(playbackEndTimeMs)
                    }

                    val mediaItem = MediaItem.Builder()
                        .setUri(currentSes.audioUrl)
                        .setClippingConfiguration(clippingBuilder.build())
                        .build()

                    setMediaItem(mediaItem)
                    prepare()
                }
            }

            DisposableEffect(exoPlayer) {
                val listener = object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            exoPlayer.seekTo(0)
                            exoPlayer.pause()
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

            LaunchedEffect(key1 = isPlaying) {
                while (isPlaying) {
                    currentPositionInClip = exoPlayer.currentPosition
                    delay(1000L)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backText)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                if (shareState !is ShareState.Processing) {
                                    viewModel.shareSes(context, currentSes.audioUrl, currentSes.id)
                                }
                            }) {
                                Icon(Icons.Default.Share, contentDescription = shareText)
                            }
                            IconButton(onClick = {
                                navController.navigate(Constants.HOME_SCREEN) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }) {
                                Icon(Icons.Default.Home, contentDescription = homeText)
                            }
                        }
                    )
                },
                content = { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        QuarantineBanner(isFlagged = currentSes.isFlagged, flagReason = currentSes.flagReason)

                        if (currentSes.isFlagged) {
                            Button(
                                onClick = { 
                                    if (isAnoba) {
                                        viewModel.unflagSes(currentSes.id) 
                                    } else {
                                        Toast.makeText(context, "Sadece yetkililer karantinadan çıkarabilir.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(0.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        UserAvatar(
                                            photoUrl = currentSes.userProfileImageUrl,
                                            username = currentSes.username,
                                            size = 32.dp,
                                            imageLoader = imageLoader
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = currentSes.senderName.ifBlank { anonymousText },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (currentSes.isOfficial) AnobaGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f, fill = false),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (currentSes.isOfficial) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            OfficialBadge(size = 14.dp)
                                        }

                                        if (currentSes.isPopular) {
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
                                            targetUserId = currentSes.ownerId,
                                            currentUserId = currentUserId,
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    }
                                    val displayDate = currentSes.publishTimestamp ?: currentSes.createdAt
                                    displayDate?.let {
                                        Text(
                                            text = DateTimeUtils.formatRelativeTime(context, it.time),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                SoundWaveAnimation(isPlaying = isPlaying)

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                        },
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) stopText else playText,
                                            modifier = Modifier.fillMaxSize(),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Slider(
                                        value = currentPositionInClip.toFloat(),
                                        onValueChange = { newPosition ->
                                            exoPlayer.seekTo(newPosition.toLong())
                                            currentPositionInClip = newPosition.toLong()
                                        },
                                        valueRange = 0f..clippedDuration.toFloat().coerceAtLeast(0f),
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .graphicsLayer(scaleY = 0.5f)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = formatDuration(currentPositionInClip), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = formatDuration(clippedDuration), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val userReactions by viewModel.userReactions.collectAsState()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentSes.viewCount > 0) {
                                Icon(
                                    imageVector = Icons.Default.RemoveRedEye,
                                    contentDescription = viewCountText,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentSes.viewCount.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            val topReaction = currentSes.reactions.maxByOrNull { it.value }
                            if (topReaction != null && topReaction.value > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showReactionUsersSheet = true }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = VideoEmojis.getDisplayEmoji(topReaction.key),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${topReaction.value}", // 🚀 TEKİL LİDER SKOR
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        SesEmojiReactionRow(
                            reactions = currentSes.reactions,
                            selectedEmoji = userReactions[sesId],
                            onReact = { emoji -> viewModel.toggleReaction(sesId, emoji) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val route = Constants.SES_COMMENTS_ROUTE.replace("{sesId}", sesId)
                                    navController.navigate(route)
                                }
                                .padding(vertical = 18.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Comment,
                                    contentDescription = commentText,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = commentText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (currentSes.commentsCount > 0) {
                                Text(
                                    text = stringResource(id = R.string.sesdetail_comments_count, currentSes.commentsCount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.width(200.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(closeButtonText)
                        }

                        Spacer(modifier = Modifier.height(34.dp))
                    }
                }
            )
        }
        else -> { }
    }
}
