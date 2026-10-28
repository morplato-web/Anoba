package com.neval.anoba.chat.call

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.PowerManager
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.neval.anoba.R
import io.agora.rtc2.IRtcEngineEventHandler
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

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

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(isVideoCall, isSpeakerOn) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        var wakeLock: PowerManager.WakeLock? = null

        if (!isVideoCall && !isSpeakerOn) {
            try {
                @Suppress("DEPRECATION")
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                    "Anoba:CallProximityLock"
                )
                if (!wakeLock.isHeld) {
                    wakeLock.acquire(3 * 60 * 60 * 1000L)
                }
            } catch (_: Exception) {}
        }

        onDispose {
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (_: Exception) {}
        }
    }

    BackHandler(enabled = !callEnded) {
        viewModel.leaveChannel(channelName)
    }

    LaunchedEffect(callEnded, endReason) {
        if (callEnded) {
            if (endReason == CallStatus.REJECTED.name || endReason == CallStatus.ENDED.name) {
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

    val currentStatusText = when {
        endReason == CallStatus.REJECTED.name -> stringResource(R.string.call_rejected_by_partner)
        remoteUid != null -> formattedDuration
        currentStatus == "ACCEPTED" -> stringResource(R.string.call_voice_ongoing)
        else -> stringResource(R.string.call_waiting)
    }

    val activeRemoteUid = remoteUid

    SharedCallScreen(
        partnerName = partnerName ?: "Anoba",
        partnerPhotoUrl = partnerPhotoUrl,
        isVideoCall = isVideoCall,
        isMuted = isMuted,
        isCameraOff = isCameraOff,
        isSpeakerOn = isSpeakerOn,
        callEnded = callEnded,
        formattedDuration = formattedDuration,
        currentStatusText = currentStatusText,
        onToggleMute = { viewModel.toggleMute() },
        onToggleCamera = { viewModel.toggleCamera() },
        onToggleSpeaker = { viewModel.toggleSpeaker() },
        onSwitchCamera = { viewModel.switchCamera() },
        onEndCall = { viewModel.leaveChannel(channelName) },
        remoteVideoSlot = {
            if (activeRemoteUid != null) {
                key(activeRemoteUid) {
                    AndroidView(
                        factory = { ctx -> SurfaceView(ctx) },
                        update = { surfaceView ->
                            viewModel.setupRemoteVideo(surfaceView, activeRemoteUid)
                        }
                    )
                }
            }
        },
        localVideoSlot = {
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).apply { setZOrderMediaOverlay(true) }
                },
                update = { surfaceView ->
                    viewModel.setupLocalVideo(surfaceView)
                }
            )
        }
    )
}
