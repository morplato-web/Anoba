package com.neval.anoba.mosaic

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.neval.anoba.R
import com.neval.anoba.common.utils.MediaCacheManager
import org.koin.androidx.compose.koinViewModel

@Composable
fun MosaicSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MosaicViewModel = koinViewModel()
) {
    val mosaicItems by viewModel.mosaicItems.collectAsState()
    val featuredItems by viewModel.featuredItems.collectAsState()
    var selectedItem by remember { mutableStateOf<MosaicItem?>(null) }

    val currentWindowColors = remember(isVisible) {
        listOf(
            Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC),
            Color(0xFF7E57C2), Color(0xFF5C6BC0), Color(0xFF42A5F5),
            Color(0xFF29B6F6), Color(0xFF26C6DA), Color(0xFF26A69A),
            Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFD4E157),
            Color(0xFFFFEE58), Color(0xFFFFCA28), Color(0xFFFFA726),
            Color(0xFF8D6E63), Color(0xFF78909C), Color(0xFFFF7043)
        ).shuffled()
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            viewModel.fetchMosaicItems()
        }
    }

    val sayfaArkaPlanRengi = Color(0xFFECEFF1)
    val sutunSayisi = 6
    val kartSayisi = 240
    val kartlarArasiBosluk = 10.dp
    val vitrinDikeyBosluk = 6.dp
    val baslikUstBoslugu = 48.dp

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier.zIndex(1000f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onDismiss() }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.95f)
                    .align(Alignment.CenterEnd),
                shape = RoundedCornerShape(16.dp),
                color = sayfaArkaPlanRengi,
                tonalElevation = 2.dp
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val kartBoyutu = (this.maxWidth - 32.dp - (kartlarArasiBosluk * (sutunSayisi - 1))) / sutunSayisi

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { 
                                    alpha = if (selectedItem != null) 0f else 1f 
                                }
                                .padding(top = baslikUstBoslugu)
                                .clickable(enabled = false) { }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.mosaic_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF37474F))
                                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, stringResource(R.string.mosaic_close), tint = Color.Gray) }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.6f))

                            if (featuredItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(vitrinDikeyBosluk))
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth().height(kartBoyutu),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(kartlarArasiBosluk)
                                ) {
                                    items(featuredItems) { item ->
                                        MosaicCard(
                                            modifier = Modifier.size(kartBoyutu),
                                            item = item,
                                            isAd = false,
                                            defaultColor = Color.White,
                                            onClick = {
                                                viewModel.trackClick(item.id)
                                                selectedItem = item
                                            },
                                            onImpression = { viewModel.trackImpression(item.id) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(vitrinDikeyBosluk))
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.6f))
                            }

                            Spacer(modifier = Modifier.height(kartlarArasiBosluk))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(sutunSayisi),
                                horizontalArrangement = Arrangement.spacedBy(kartlarArasiBosluk),
                                verticalArrangement = Arrangement.spacedBy(kartlarArasiBosluk),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 60.dp)
                            ) {
                                items(kartSayisi) { index ->
                                    val customItem = mosaicItems.find { it.index == index.toLong() }
                                    val isAdArea = index > 0 && index % 17 == 0
                                    MosaicCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        item = customItem,
                                        isAd = isAdArea,
                                        defaultColor = currentWindowColors[index % currentWindowColors.size],
                                        onClick = {
                                            customItem?.let {
                                                viewModel.trackClick(it.id)
                                                selectedItem = it
                                            }
                                        },
                                        onImpression = { customItem?.let { viewModel.trackImpression(it.id) } }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = selectedItem != null,
                            enter = fadeIn(tween(400)),
                            exit = fadeOut(tween(400)),
                            modifier = Modifier.zIndex(500f)
                        ) {
                            selectedItem?.let { item ->
                                BackHandler { selectedItem = null }
                                MosaicDetailOverlay(
                                    item = item,
                                    onDismiss = { selectedItem = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MosaicDetailOverlay(
    item: MosaicItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(false) }

    val exoPlayer = remember(item.id) {
        MediaCacheManager.createPlayer(context).apply {
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
            val listener = object : androidx.media3.common.Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isBuffering = playbackState == androidx.media3.common.Player.STATE_BUFFERING
                }
            }
            addListener(listener)
            
            // FIX: "Smart Cast impossible" fixed with local val
            val currentUrl = item.contentUrl
            if ((item.type == "video" || item.type == "audio") && !currentUrl.isNullOrEmpty()) {
                val mediaItem = androidx.media3.common.MediaItem.fromUri(currentUrl)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { 
            exoPlayer.stop()
            exoPlayer.release() 
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1B1F)) 
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (item.type) {
                        "video" -> {
                            Box(
                                contentAlignment = Alignment.Center, 
                                modifier = Modifier
                                    .fillMaxSize()
                                    // 🚀 Gemini'nin uyarısı: Siyah arka plan tamamen kaldırıldı
                                    .background(Color.Transparent)
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        // 🚀 Lint uyarısını önlemek için FrameLayout kullanıyoruz
                                        val parent = android.widget.FrameLayout(ctx)
                                        val view = android.view.LayoutInflater.from(ctx).inflate(R.layout.transparent_player_view, parent, false) as androidx.media3.ui.PlayerView
                                        view.apply {
                                            layoutParams = android.view.ViewGroup.LayoutParams(
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            player = exoPlayer
                                            useController = true
                                            controllerShowTimeoutMs = 3000 
                                            setKeepContentOnPlayerReset(true)
                                        }
                                    },
                                    update = { playerView ->
                                        if (playerView.player != exoPlayer) {
                                            playerView.player = exoPlayer
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isBuffering) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                        }
                        "image" -> {
                            AsyncImage(
                                model = item.contentUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        "text" -> {
                            Text(
                                text = item.textContent ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                        "audio" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.Mic, null, modifier = Modifier.size(100.dp), tint = Color.White)
                                Spacer(modifier = Modifier.height(24.dp))
                                AndroidView(
                                    factory = { ctx ->
                                        androidx.media3.ui.PlayerView(ctx).apply {
                                            player = exoPlayer
                                            useController = true
                                        }
                                    },
                                    update = { playerView ->
                                        if (playerView.player != exoPlayer) {
                                            playerView.player = exoPlayer
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(100.dp)
                                )
                            }
                        }
                        "animation" -> {
                            val composition by rememberLottieComposition(LottieCompositionSpec.Url(item.contentUrl ?: ""))
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .zIndex(10f)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MosaicCard(
    item: MosaicItem?,
    isAd: Boolean,
    defaultColor: Color,
    onClick: () -> Unit,
    onImpression: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(item?.id) { if (item != null && item.id.isNotEmpty()) onImpression() }

    val backgroundColor = when {
        item != null -> Color.Transparent
        else -> defaultColor
    }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item != null || isAd) 0.dp else 0.5.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            when (item?.type) {
                "image" -> {
                    AsyncImage(
                        model = item.contentUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                "text" -> {
                    Text(
                        text = item.textContent ?: "",
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF37474F),
                        textAlign = TextAlign.Center,
                        fontSize = 7.sp
                    )
                }
                "video" -> {
                    Icon(Icons.Default.PlayCircle, stringResource(R.string.mosaic_video_label), tint = Color(0xFF37474F).copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
                "audio" -> {
                    Icon(Icons.Default.Mic, stringResource(R.string.mosaic_audio_label), tint = Color(0xFF37474F).copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
                "animation" -> {
                    val composition by rememberLottieComposition(LottieCompositionSpec.Url(item.contentUrl ?: ""))
                    LottieAnimation(
                        composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (isAd) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.05f)), 
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.mosaic_ad_label),
                        color = Color(0xFF37474F).copy(alpha = 0.4f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
