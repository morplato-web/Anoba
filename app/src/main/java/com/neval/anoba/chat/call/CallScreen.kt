package com.neval.anoba.chat.call

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.PowerManager
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.neval.anoba.R
import com.neval.anoba.chat.ui.InitialsAvatar
import io.agora.rtc2.IRtcEngineEventHandler
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@SuppressLint("WakelockTimeout", "Wakelock")
@Composable
fun CallScreen(
    navController: NavController,
    channelName: String,
    isVideoCall: Boolean,
    isIncoming: Boolean,
    viewModel: CallViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val remoteUid by viewModel.remoteUid.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isCameraOff by viewModel.isCameraOff.collectAsState()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val callEnded by viewModel.callEnded.collectAsState()
    val endReason by viewModel.endReason.collectAsState()
    val partnerName by viewModel.partnerName.collectAsState()
    val partnerPhotoUrl by viewModel.partnerPhotoUrl.collectAsState()
    val callDuration by viewModel.callDuration.collectAsState()
    val currentStatus by viewModel.currentStatus.collectAsState()

    val formattedDuration = remember(callDuration) {
        val minutes = callDuration / 60
        val seconds = callDuration % 60
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    // 1. EKRANI UYANIK TUTMA
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // 2. YAKINLIK SENSÖRÜ (PROXIMITY SENSOR)
    DisposableEffect(isVideoCall, isSpeakerOn) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        var wakeLock: PowerManager.WakeLock? = null

        // Eğer sesli aramadaysak ve hoparlör açık değilse sensörü devreye sok
        if (!isVideoCall && !isSpeakerOn) {
            try {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                    "Anoba:CallProximityLock"
                )
                if (!wakeLock.isHeld) {
                    // Maksimum 3 saatlik timeout (Pil tasarrufu için best-practice)
                    wakeLock.acquire(3 * 60 * 60 * 1000L)
                }
            } catch (_: Exception) {
                // Bazı eski veya sensörsüz cihazlarda çökmemesi için catch bloğu
            }
        }

        // onDispose Compose'un finally bloğu gibi çalışır.
        onDispose {
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    // Geri tuşu yakalayıcı: Arama bitmemişse leaveChannel tetiklenir
    BackHandler(enabled = !callEnded) {
        viewModel.leaveChannel(channelName)
    }

    LaunchedEffect(callEnded, endReason) {
        if (callEnded) {
            if ((endReason == CallStatus.REJECTED.name) || (endReason == CallStatus.ENDED.name)) {
                delay(2000)
            }
            navController.navigateUp()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.all { it.value }
        if (granted) {
            // İzinler alındıktan SONRA motoru başlat ve kanala katıl
            viewModel.initEngine(
                context.applicationContext,
                isVideoCall,
                object : IRtcEngineEventHandler() {
                    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {}
                    override fun onUserJoined(uid: Int, elapsed: Int) {
                        viewModel.onRemoteUserJoined(uid)
                    }
                    override fun onUserOffline(uid: Int, reason: Int) {
                        viewModel.onRemoteUserLeft()
                        viewModel.leaveChannel(channelName)
                    }
                    override fun onLeaveChannel(stats: RtcStats?) {}
                },
            )
            viewModel.joinChannel(context, channelName, isVideoCall, isIncoming)
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()

        permissionLauncher.launch(permissions)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {

        // --- ARKA PLAN EFEKTİ (BLUR) ---
        if (!partnerPhotoUrl.isNullOrBlank() && (!isVideoCall || remoteUid == null)) {
            AsyncImage(
                model = partnerPhotoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 80.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        }

        // 1. UZAK KULLANICI VİDEOSU (TAM EKRAN)
        val activeRemoteUid = remoteUid
        if (isVideoCall && activeRemoteUid != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                key(activeRemoteUid) {
                    AndroidView(
                        factory = { ctx ->
                            SurfaceView(ctx)
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { surfaceView ->
                            viewModel.setupRemoteVideo(surfaceView, activeRemoteUid)
                        }
                    )
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = CircleShape,
                ) {
                    Text(
                        text = formattedDuration,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!partnerPhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = partnerPhotoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        InitialsAvatar(
                            name = partnerName ?: "Anoba",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = partnerName ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        endReason == CallStatus.REJECTED.name -> stringResource(R.string.call_rejected_by_partner)
                        remoteUid != null -> formattedDuration
                        currentStatus == "ACCEPTED" -> stringResource(R.string.call_voice_ongoing)
                        else -> stringResource(R.string.call_waiting)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (endReason == CallStatus.REJECTED.name) Color.Red else Color.White.copy(alpha = 0.8f),
                    fontWeight = if (endReason == CallStatus.REJECTED.name) FontWeight.Bold else FontWeight.Normal,
                )
                Spacer(modifier = Modifier.height(42.dp))
            }
        }

        // 2. LOKAL KULLANICI VİDEOSU (SAĞ ÜST KÜÇÜK PENCERE)
        if (isVideoCall && !isCameraOff) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .size(120.dp, 160.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.DarkGray),
            ) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceView(ctx).apply {
                            setZOrderMediaOverlay(true)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { surfaceView ->
                        viewModel.setupLocalVideo(surfaceView)
                    }
                )
            }
        }

        // 3. KONTROLLER (ALT ALAN)
        if (!callEnded) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = stringResource(R.string.ax_mute),
                            tint = if (isMuted) Color.Black else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.leaveChannel(channelName) },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = stringResource(R.string.ax_end_call),
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    if (isVideoCall) {
                        IconButton(
                            onClick = { viewModel.toggleCamera() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isCameraOff) Color.White else Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                contentDescription = stringResource(R.string.ax_camera),
                                tint = if (isCameraOff) Color.Black else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.toggleSpeaker() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeDown,
                                contentDescription = stringResource(R.string.ax_speaker),
                                tint = if (isSpeakerOn) Color.Black else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                if (isVideoCall) {
                    Spacer(modifier = Modifier.height(24.dp))
                    IconButton(
                        onClick = { viewModel.switchCamera() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = stringResource(R.string.ax_switch_camera),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}