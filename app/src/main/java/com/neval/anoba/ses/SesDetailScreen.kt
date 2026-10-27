package com.neval.anoba.ses

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.DateTimeUtils
import com.neval.anoba.common.utils.MediaCacheManager
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.login.state.AuthState
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SesDetailScreen(
    sesId: String,
    navController: NavController,
    viewModel: SesViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val sesState by viewModel.sesDetails.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val userRoleState by authViewModel.userRole.collectAsState()
    val userReactions by viewModel.userReactions.collectAsState()

    val currentUserId = (authState as? AuthState.Authenticated)?.uid
    val isAdmin = userRoleState?.equals("ADMIN", ignoreCase = true) == true ||
                  (authState as? AuthState.Authenticated)?.email == "morplato@gmail.com"

    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    val currentSes = (sesState as? UiState.Success)?.data

    val playbackStartTimeMs = currentSes?.trimStartMs ?: 0L
    val playbackEndTimeMs = currentSes?.trimEndMs ?: currentSes?.duration ?: 0L
    val clippedDuration = (playbackEndTimeMs - playbackStartTimeMs).coerceAtLeast(0L)

    val exoPlayer = remember(currentSes?.audioUrl) {
        currentSes?.audioUrl?.let { url ->
            MediaCacheManager.createPlayer(context.applicationContext).apply {
                val clipping = MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(playbackStartTimeMs)
                    .apply { if (playbackEndTimeMs > playbackStartTimeMs) setEndPositionMs(playbackEndTimeMs) }
                    .build()

                setMediaItem(MediaItem.Builder().setUri(url).setClippingConfiguration(clipping).build())
                prepare()
            }
        }
    }

    DisposableEffect(exoPlayer) {
        val player = exoPlayer ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    player.seekTo(0)
                    player.pause()
                }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener); player.release() }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer?.currentPosition ?: 0L
            delay(500L)
        }
    }

    LaunchedEffect(sesId) {
        viewModel.loadSesDetails(sesId)
        viewModel.incrementViewCount(sesId)
    }

    val detailModel = currentSes?.let { s ->
        SharedSesDetailModel(
            id = s.id,
            ownerId = s.ownerId,
            username = s.username,
            userProfileImageUrl = s.userProfileImageUrl,
            audioUrl = s.audioUrl,
            title = s.title ?: "",
            personalNote = s.personalNote,
            viewCount = s.viewCount.toInt(),
            commentsCount = s.commentsCount.toInt(),
            durationText = "00:00",
            durationMillis = clippedDuration,
            isOfficial = s.isOfficial,
            userBadgeType = s.userBadgeType,
            isPopular = s.isPopular,
            isFlagged = s.isFlagged,
            flagReason = s.flagReason,
            reactions = s.reactions.mapValues { it.value.toInt() },
            timeAgoText = DateTimeUtils.formatRelativeTime(context, s.publishTimestamp?.time ?: s.createdAt?.time ?: 0L)
        )
    }

    SharedSesDetailScreen(
        ses = detailModel,
        isLoading = sesState is UiState.Loading,
        errorMessage = (sesState as? UiState.Error)?.message,
        currentUserId = currentUserId,
        isAdminOrModerator = isAdmin,
        userReactions = userReactions,
        isPlaying = isPlaying,
        currentPositionMillis = currentPositionMs,
        totalDurationMillis = clippedDuration,
        onNavigateBack = { navController.popBackStack() },
        onHomeClick = {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        },
        onShareClick = {
            detailModel?.let { viewModel.shareSes(context, it.audioUrl, it.id) }
        },
        onOpenComments = {
            navController.navigate(Constants.SES_COMMENTS_ROUTE.replace("{sesId}", sesId))
        },
        onReactionClick = { emoji -> viewModel.toggleReaction(sesId, emoji) },
        onUnflagSes = { viewModel.unflagSes(sesId) },
        onPlayPauseToggle = {
            exoPlayer?.let { if (it.isPlaying) it.pause() else it.play() }
        },
        onSeekTo = { pos -> exoPlayer?.seekTo(pos) }
    )
}
