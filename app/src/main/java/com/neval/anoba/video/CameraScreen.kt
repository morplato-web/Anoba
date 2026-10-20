package com.neval.anoba.video

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import com.neval.anoba.common.utils.formatDuration
import com.neval.anoba.photo.PhotoViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@SuppressLint("ContextCast")
@Composable
fun CameraScreen(
    navController: NavController,
    videoViewModel: VideoViewModel = koinViewModel(),
    photoViewModel: PhotoViewModel = koinViewModel(),
    cameraViewModel: CameraViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val hasCameraPermission by cameraViewModel.hasCameraPermission.collectAsState()
    val hasAudioPermission by cameraViewModel.hasAudioPermission.collectAsState()
    val recordedVideoUri by cameraViewModel.recordedVideoUri.collectAsState()
    val capturedPhotoUri by cameraViewModel.capturedPhotoUri.collectAsState()
    val isUploading by cameraViewModel.isUploading.collectAsState()
    val recordingState by cameraViewModel.recordingState.collectAsState()

    val uploadSuccessMessage = stringResource(R.string.video_upload_success)
    val uploadErrorMessageTemplate = stringResource(R.string.video_upload_error)
    val unknownErrorMessage = stringResource(R.string.unknown_error)
    val videoTitleTemplate = stringResource(id = R.string.camera_video_title, "")

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraViewModel.onPermissionResult(
            permissions = permissions,
            cameraPermission = Manifest.permission.CAMERA,
            audioPermission = Manifest.permission.RECORD_AUDIO
        )
    }

    LaunchedEffect(Unit) {
        cameraViewModel.checkInitialPermissions(context)
    }

    LaunchedEffect(hasCameraPermission, hasAudioPermission) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    LaunchedEffect(recordingState) {
        if (recordingState is RecordingState.Error) {
            val errorMsg = (recordingState as RecordingState.Error).message
            val displayMsg = if (errorMsg.contains("5") || errorMsg.contains("6")) {
                "Kamera şu an meşgul. Lütfen bir saniye bekleyip tekrar deneyin."
            } else errorMsg
            Toast.makeText(context, displayMsg, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission && hasAudioPermission) {
            val videoUri = recordedVideoUri
            val photoUri = capturedPhotoUri

            when {
                // 1. VİDEO ÖNİZLEME VE KAYDETME
                videoUri != null -> {
                    VideoPreview(
                        uri = videoUri,
                        isUploading = isUploading,
                        onBack = { cameraViewModel.clearMedia() },
                        onSave = {
                            if (!isUploading) {
                                cameraViewModel.setUploading(true)
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                videoViewModel.uploadVideo(
                                    context = context,
                                    videoUri = videoUri,
                                    title = videoTitleTemplate.format(timestamp),
                                    duration = getVideoDuration(context, videoUri),
                                    onComplete = { success, message ->
                                        cameraViewModel.setUploading(false)
                                        val toastMessage = if (success) uploadSuccessMessage else String.format(uploadErrorMessageTemplate, message ?: unknownErrorMessage)
                                        ContextCompat.getMainExecutor(context).execute {
                                            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                                            if (success) {
                                                navController.navigate(Constants.MY_CONTENT_NAV_GRAPH) {
                                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
                // 2. FOTOĞRAF ÖNİZLEME VE KAYDETME
                photoUri != null -> {
                    PhotoPreview(
                        uri = photoUri,
                        isUploading = isUploading,
                        onBack = { cameraViewModel.clearMedia() },
                        onSave = {
                            if (!isUploading) {
                                cameraViewModel.setUploading(true)
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                photoViewModel.uploadPhotoAndCreateRecord(
                                    context = context,
                                    photoUri = photoUri,
                                    title = "Fotoğraf $timestamp",
                                    onComplete = { success, _ ->
                                        cameraViewModel.setUploading(false)
                                        ContextCompat.getMainExecutor(context).execute {
                                            if (success) {
                                                Toast.makeText(context, "Fotoğraf taslağa eklendi", Toast.LENGTH_SHORT).show()
                                                navController.navigate(Constants.MY_CONTENT_NAV_GRAPH) {
                                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                Toast.makeText(context, "Fotoğraf kaydedilemedi", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
                // 3. CANLI BİRLEŞİK KAMERA
                else -> {
                    UnifiedCameraCapture(viewModel = cameraViewModel)
                }
            }
        } else {
            PermissionRequestUI(permissionLauncher)
        }

        if (recordedVideoUri == null && capturedPhotoUri == null) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.camera_back), tint = Color.White)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun UnifiedCameraCapture(
    viewModel: CameraViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val resolutionSelector = remember {
        ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO))
            .build()
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // 🚀 KRİSTAL NETLİK: Kalite öncelikli çekim
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO) // ⚡ Otomatik flaş desteği
            .setResolutionSelector(resolutionSelector) // 🎯 UYUM: Önizleme ile aynı oranı korur
            .build()
    }

    val qualitySelector = QualitySelector.from(Quality.HD, FallbackStrategy.higherQualityOrLowerThan(Quality.SD))
    val recorder = remember { Recorder.Builder().setQualitySelector(qualitySelector).build() }
    val videoCapture = remember { VideoCapture.withOutput(recorder) }
    val isRecording by viewModel.isRecording.collectAsState()
    var showFlashEffect by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            if (isRecording) viewModel.stopRecording() // 🚀 VİDEO KORUMASI: Yarım kalan kaydı kurtar
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                } catch (e: Exception) {
                    Log.e("CameraCapture", "Kamera serbest bırakılamadı", e)
                }
            }, ContextCompat.getMainExecutor(context))
            cameraExecutor.shutdown()
        }
    }

    fun takePhoto() {
        if (isCapturing) return // Çekim sürerken ikinci tıkı engelle
        isCapturing = true
        showFlashEffect = true // ⚡ Flaş efektini başlat

        val name = "photo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    isCapturing = false
                    outputFileResults.savedUri?.let { viewModel.onPhotoCaptured(it) }
                }
                override fun onError(exception: ImageCaptureException) {
                    isCapturing = false
                    Log.e("CameraCapture", "Fotoğraf çekme hatası", exception)
                }
            }
        )
    }

    fun startRecording() {
        if (isRecording) return

        val name = "video_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(Date())}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        val activeRecording = videoCapture.output
            .prepareRecording(context, mediaStoreOutput)
            .withAudioEnabled()
            .start(cameraExecutor) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> Log.d("CameraCapture", "Kayıt başladı")
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            viewModel.onVideoRecorded(recordEvent.outputResults.outputUri)
                        } else {
                            viewModel.setRecordingError("Video hatası: ${recordEvent.error}")
                        }
                    }
                }
            }
        viewModel.startRecording(activeRecording)
    }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture, videoCapture)
            } catch (exc: Exception) {
                Log.e("CameraCapture", "Kamera bağlama hatası", exc)
                viewModel.setRecordingError("Kamera başlatılamadı.")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(showFlashEffect) {
        if (showFlashEffect) {
            delay(120) // ⚡ İdeal flaş süresi (Gemini tavsiyesi)
            showFlashEffect = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // ⚡ GÖRSEL FLAŞ EFEKTİ (Deklanşör Hissiyatı)
        if (showFlashEffect) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }

        // WHATSAPP TARZI DEKLANŞÖR (Tek Tık: Fotoğraf | Basılı Tutma: Video)
        val buttonScale by animateFloatAsState(
            targetValue = if (isRecording) 1.25f else 1.0f,
            animationSpec = tween(200),
            label = "buttonScale"
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRecording) "Kayıt yapılıyor... (Bırakınca biter)" else "Fotoğraf için dokunun, video için basılı tutun",
                color = Color.White.copy(alpha = 0.8f),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(buttonScale)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color.Red else Color.White)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (!isRecording) takePhoto()
                            },
                            onLongPress = {
                                startRecording()
                            },
                            onPress = {
                                tryAwaitRelease()
                                if (isRecording) {
                                    viewModel.stopRecording()
                                }
                            }
                        )
                    }
            )
        }
    }
}

@Composable
private fun PhotoPreview(
    uri: Uri,
    isUploading: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        if (isUploading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("İptal")
            }

            Button(
                onClick = onSave,
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C), contentColor = Color.White),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Kaydet")
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun VideoPreview(
    uri: Uri,
    isUploading: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember(context, uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    val playerView = remember {
        PlayerView(context).apply {
            player = exoPlayer
            useController = false
        }
    }

    var isPlaying by remember(exoPlayer) { mutableStateOf(exoPlayer.isPlaying) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var currentTime by remember { mutableLongStateOf(0L) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) { isPlaying = isPlayingValue }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isUploading) { if (isUploading) exoPlayer.pause() }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                currentTime = exoPlayer.currentPosition.coerceAtLeast(0L)
                delay(1000)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { playerView }, modifier = Modifier.fillMaxSize())

        if (isUploading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(id = R.string.video_uploading), color = Color.White)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Slider(
                value = currentTime.toFloat(),
                onValueChange = { exoPlayer.seekTo(it.toLong()) },
                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.Gray),
                enabled = !isUploading
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = formatDuration(currentTime), color = Color.White)
                Text(text = formatDuration(totalDuration), color = Color.White)
            }

            Spacer(modifier = Modifier.size(16.dp))

            IconButton(
                onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                enabled = !isUploading
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.size(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onBack,
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(id = R.string.camera_cancel))
                }

                Button(
                    onClick = onSave,
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C), contentColor = Color.White),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(id = R.string.camera_save))
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestUI(permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(id = R.string.camera_permission_request_text))
            Button(onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }) {
                Text(stringResource(id = R.string.camera_permission_request_button))
            }
        }
    }
}