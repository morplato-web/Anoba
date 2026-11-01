package com.neval.anoba.video

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.neval.anoba.common.utils.MediaCacheManager

@Suppress("unused", "UnusedSymbol")
@UnstableApi
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    showControls: Boolean = true,
    scale: Int = C.VIDEO_SCALING_MODE_SCALE_TO_FIT,
    onPlayerError: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember {
        MediaCacheManager.createPlayer(context).apply {
            repeatMode = Player.REPEAT_MODE_OFF
            videoScalingMode = scale
        }
    }

    // SAĞLAMLAŞTIRMA: Uygulama arka plana atıldığında videoyu durdur, gelince devam et.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (autoPlay) exoPlayer.play()
                }
                Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    LaunchedEffect(videoUrl) {
        if (videoUrl.isNotBlank()) {
            try {
                val mediaItem = MediaItem.fromUri(videoUrl.toUri())
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            } catch (e: Exception) {
                Log.e("VideoPlayer", "Hazırlama hatası", e)
                onPlayerError?.invoke("Video hazırlanamadı.")
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = showControls
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier.background(Color.Black)
    )
}
