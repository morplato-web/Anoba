package com.neval.anoba.video

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.MediaCacheManager
import com.neval.anoba.common.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs

@SuppressLint("ContextCast")
@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun VideoHomeScreen(
    navController: NavController,
    videoViewModel: VideoViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val videosState: UiState<List<VideoModel>> by videoViewModel.videosState.collectAsState()
    val isUploading: Boolean by videoViewModel.isUploading.collectAsState()
    val pinnedIds: Set<String> by videoViewModel.pinnedIds.collectAsState()

    val userRole: String? by authViewModel.userRole.collectAsStateWithLifecycle()
    val isAdmin = userRole.equals("ADMIN", ignoreCase = true)

    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 🚀 1. TEKİL EXOPLAYER INSTANCE (MediaCacheManager İle)
    val exoPlayer = remember {
        MediaCacheManager.createPlayer(context).apply {
            volume = 0f // Varsayılan sessiz otomatik oynatma
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    // Ekrandan çıkıldığında player'ı serbest bırak
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // 🚀 2. EKRANIN TAM ORTASINDAKİ (VIEWPORT CENTER) VİDEONUN INDEX'İNİ BULMA
    val activePlayingIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf -1

            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            visibleItems.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                abs(itemCenter - viewportCenter)
            }?.index ?: -1
        }
    }

    // 🚀 3. AKTİF VİDEO DEĞİŞTİKÇE MEDYA ÖĞESİNİ YÜKLE VE OYNAT (toUri() uzantısıyla)
    LaunchedEffect(activePlayingIndex, videosState) {
        if (videosState is UiState.Success) {
            val videos = (videosState as UiState.Success<List<VideoModel>>).data
            if (activePlayingIndex in videos.indices) {
                val currentVideo = videos[activePlayingIndex]
                if (currentVideo.videoUrl.isNotBlank()) {
                    val mediaItem = MediaItem.fromUri(currentVideo.videoUrl.toUri())
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                }
            } else {
                exoPlayer.pause()
            }
        }
    }

    val showBackToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    val isRefreshing = videosState is UiState.Loading

    val backText = stringResource(id = R.string.videohome_back)
    val homeText = stringResource(id = R.string.videohome_home)
    val loadingText = stringResource(id = R.string.state_loading)
    val noDataText = stringResource(id = R.string.state_no_data)

    LaunchedEffect(key1 = videoViewModel.eventFlow) {
        videoViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is VideoUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.videohome_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backText)
                    }
                },
                actions = {
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
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { videoViewModel.refresh() },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                    when (val state = videosState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Text(
                                        text = loadingText,
                                        modifier = Modifier.padding(top = 8.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        is UiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        is UiState.Success -> {
                            val videos = state.data
                            if (videos.isEmpty() && !isUploading) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(noDataText)
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    itemsIndexed(
                                        items = videos,
                                        key = { _, item -> item.id }
                                    ) { index: Int, video: VideoModel ->
                                        VideoCard(
                                            video = video,
                                            navController = navController,
                                            exoPlayer = exoPlayer,
                                            isPlaying = (index == activePlayingIndex),
                                            onClick = {
                                                val route = Constants.VIDEO_DETAIL_SCREEN.replace("{videoId}", video.id)
                                                navController.navigate(route)
                                            },
                                            isPinned = pinnedIds.contains(video.id),
                                            onPinClick = { videoViewModel.togglePin(video.id) },
                                            canDelete = isAdmin,
                                            onDeleteClick = {
                                                videoViewModel.deleteVideo(video.id) { success ->
                                                    if (success) {
                                                        Toast.makeText(context, "${context.getString(R.string.mycontent_sheet_delete)} ${context.getString(R.string.auth_process_completed)}", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, context.getString(R.string.mycontent_error_delete), Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        else -> {} // 🚀 'when' exhaustiveness gereksinimini karşılar
                    }
                }
            }

            AnimatedVisibility(
                visible = showBackToTop,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 100.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch { listState.animateScrollToItem(0) }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.shadow(8.dp, CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Back to Top")
                }
            }
        }
    }
}

@SuppressLint("ContextCast")
fun getVideoDuration(context: Context, uri: Uri): Long {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        time?.toLongOrNull() ?: 0L
    } catch (e: Exception) {
        Log.e("getVideoDuration", context.getString(R.string.videohome_error_duration), e)
        0L
    }
}
