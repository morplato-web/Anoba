package com.neval.anoba.video

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.neval.anoba.R
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.MediaCacheManager
import com.neval.anoba.common.utils.formatDuration
import com.neval.anoba.mycontent.TrimState
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnsafeOptInUsageError", "ContextCast")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditScreen(
    navController: NavController,
    videoId: String,
    videoUri: Uri,
    viewModel: VideoEditViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val trimState by viewModel.trimState.collectAsState()
    val videoDetails by viewModel.videoDetails.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val rotationDegrees by viewModel.rotationDegrees.collectAsState()
    val speedMultiplier by viewModel.speedMultiplier.collectAsState()
    val videoDuration by viewModel.videoDuration.collectAsState()
    val sliderPositions by viewModel.sliderPositions.collectAsState()

    var title by remember { mutableStateOf("") }
    var personalNote by remember { mutableStateOf("") }
    var localSliderPositions by remember(sliderPositions) { mutableStateOf(sliderPositions) }
    var isPlaying by remember { mutableStateOf(false) }

    val editSuccessText = stringResource(id = R.string.video_edit_success)

    val exoPlayer = remember {
        MediaCacheManager.createPlayer(context).apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    LaunchedEffect(videoId) {
        viewModel.loadVideoDetails(videoId)
    }

    LaunchedEffect(videoDetails) {
        val state = videoDetails
        if (state is UiState.Success<VideoModel>) {
            val video = state.data
            title = video.title ?: ""
            personalNote = video.personalNote
        }
    }

    LaunchedEffect(isMuted) { exoPlayer.volume = if (isMuted) 0f else 1f }
    LaunchedEffect(speedMultiplier) { exoPlayer.playbackParameters = PlaybackParameters(speedMultiplier) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) viewModel.setVideoDuration(exoPlayer.duration)
            }
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    LaunchedEffect(trimState) {
        when (val state = trimState) {
            is TrimState.Success -> {
                viewModel.saveChanges(videoId, state.newUri)
            }
            is TrimState.UploadComplete -> {
                Toast.makeText(context, editSuccessText, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is TrimState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.video_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    // HIZLI KAYDET: Sadece metinleri günceller
                    IconButton(onClick = {
                        viewModel.updateOnlyTextDetails(videoId, title, personalNote) { success ->
                            if (success) {
                                Toast.makeText(context, editSuccessText, Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Video Önizleme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { PlayerView(it).apply { player = exoPlayer; useController = false } },
                    modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotationDegrees.toFloat() }
                )
                if (!isPlaying) {
                    IconButton(onClick = { exoPlayer.play() }, modifier = Modifier.size(64.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().clickable { exoPlayer.pause() })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kırpma Alanı
            if (videoDuration > 0) {
                RangeSlider(
                    value = localSliderPositions,
                    onValueChange = { localSliderPositions = it },
                    onValueChangeFinished = {
                        viewModel.updateSliderPositions(localSliderPositions)
                        exoPlayer.seekTo(localSliderPositions.start.toLong())
                    },
                    valueRange = 0f..videoDuration.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDuration(localSliderPositions.start.toLong()), fontSize = 12.sp)
                    Text(formatDuration(localSliderPositions.endInclusive.toLong()), fontSize = 12.sp)
                }
            }

            // Döndür & Sessiz
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = { viewModel.toggleMute() }) {
                    Icon(if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp, null)
                }
                IconButton(onClick = { viewModel.rotateVideo() }) {
                    Icon(Icons.Default.Rotate90DegreesCcw, null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BAŞLIK VE NOT ALANLARI
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.photoedit_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = personalNote,
                onValueChange = { personalNote = it },
                label = { Text(stringResource(R.string.photoedit_note_label)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // UYGULA VE KAYDET BUTONU (Kırpma işlemi ile beraber)
            Button(
                onClick = { viewModel.trimVideo(context, videoUri) },
                enabled = videoDuration > 0 && trimState !is TrimState.InProgress,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (trimState is TrimState.InProgress || trimState is TrimState.Saving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(id = R.string.video_edit_apply_and_save))
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
