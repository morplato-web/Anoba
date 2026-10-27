package com.neval.anoba.video

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedCameraScreen(
    capturedMedia: CapturedMedia? = null,
    isRecording: Boolean = false,
    isUploading: Boolean = false,
    hasPermissions: Boolean = true,
    onRequestPermissions: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onHomeClick: () -> Unit = onNavigateBack,
    onSwitchCamera: () -> Unit = {},
    onTakePhoto: () -> Unit = {},
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {},
    onClearMedia: () -> Unit = {},
    onSaveMedia: (CapturedMedia) -> Unit = {},
    modifier: Modifier = Modifier,
    cameraPreviewContent: (@Composable () -> Unit)? = null,
    videoPlayerContent: (@Composable () -> Unit)? = null
) {
    var showFlashEffect by remember { mutableStateOf(false) }

    LaunchedEffect(showFlashEffect) {
        if (showFlashEffect) {
            delay(100L)
            showFlashEffect = false
        }
    }

    val buttonScale by animateFloatAsState(
        targetValue = if (isRecording) 1.25f else 1.0f,
        animationSpec = tween(200),
        label = "shutterButtonScale"
    )

    Scaffold(
        containerColor = Color.Black,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // İzin Yoksa
                !hasPermissions -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Kamera ve mikrofon izinleri gereklidir.",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(
                                onClick = onRequestPermissions,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("İzin Ver")
                            }
                        }
                    }
                }

                // Fotoğraf Çekildi -> Onay/İptal Ekranı
                capturedMedia is CapturedMedia.Photo -> {
                    PhotoPreviewOverlay(
                        media = capturedMedia,
                        isUploading = isUploading,
                        onCancel = onClearMedia,
                        onConfirm = { onSaveMedia(capturedMedia) }
                    )
                }

                // Video Çekildi -> Oynat/Onay/İptal Ekranı
                capturedMedia is CapturedMedia.Video -> {
                    VideoPreviewOverlay(
                        media = capturedMedia,
                        isUploading = isUploading,
                        onCancel = onClearMedia,
                        onConfirm = { onSaveMedia(capturedMedia) },
                        videoPlayerContent = videoPlayerContent
                    )
                }

                // Canlı Kamera Vizörü
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (cameraPreviewContent != null) {
                            cameraPreviewContent()
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Kamera Vizörü Hazır",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Deklanşör Flaş Efekti
                        if (showFlashEffect) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.6f))
                            )
                        }

                        // Üst Bar (Geri ve Ana Sayfa)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = onHomeClick) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Ana Sayfa",
                                    tint = Color.White
                                )
                            }
                        }

                        // Alt Bar: Kamera Değiştir + Hibrit Deklanşör
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isRecording) "Kayıt yapılıyor... (Bırakınca durur)" else "Fotoğraf için dokunun, video için basılı tutun",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 14.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Ön/Arka Kamera Değiştir
                                IconButton(
                                    onClick = onSwitchCamera,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlipCameraAndroid,
                                        contentDescription = "Kamera Değiştir",
                                        tint = Color.White
                                    )
                                }

                                // Hibrit Deklanşör (Tek tık: Fotoğraf | Basılı tutma: Video)
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .scale(buttonScale)
                                        .border(4.dp, Color.White, CircleShape)
                                        .padding(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isRecording) Color.Red else Color.White)
                                        .pointerInput(isRecording) {
                                            detectTapGestures(
                                                onTap = {
                                                    if (!isRecording) {
                                                        showFlashEffect = true
                                                        onTakePhoto()
                                                    }
                                                },
                                                onLongPress = {
                                                    onStartRecording()
                                                },
                                                onPress = {
                                                    tryAwaitRelease()
                                                    if (isRecording) {
                                                        onStopRecording()
                                                    }
                                                }
                                            )
                                        }
                                )

                                Spacer(modifier = Modifier.size(48.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoPreviewOverlay(
    media: CapturedMedia.Photo,
    isUploading: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = media.uriOrPath,
            contentDescription = "Çekilen Fotoğraf",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.65f))
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onCancel,
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("İptal")
            }

            Button(
                onClick = onConfirm,
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C), contentColor = Color.White),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Kaydet")
            }
        }
    }
}

@Composable
private fun VideoPreviewOverlay(
    media: CapturedMedia.Video,
    isUploading: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    videoPlayerContent: (@Composable () -> Unit)? = null
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (videoPlayerContent != null) {
            videoPlayerContent()
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Video Önizlemesi", color = Color.White, fontSize = 16.sp)
            }
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Video yükleniyor...", color = Color.White)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.65f))
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (media.durationMillis > 0L) {
                Slider(
                    value = currentPositionMs,
                    onValueChange = { currentPositionMs = it },
                    valueRange = 0f..media.durationMillis.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            IconButton(
                onClick = { isPlaying = !isPlaying },
                enabled = !isUploading
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Oynat/Durdur",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onCancel,
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("İptal")
                }

                Button(
                    onClick = onConfirm,
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C), contentColor = Color.White),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Kaydet")
                }
            }
        }
    }
}
