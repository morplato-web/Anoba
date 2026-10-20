package com.neval.anoba.chat.call

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.SurfaceView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.neval.anoba.R
import com.neval.anoba.common.utils.Constants
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CallViewModel(
    private val callRepository: CallRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private var rtcEngine: RtcEngine? = null
    private var mediaPlayer: MediaPlayer? = null
    private var firestoreListenJob: Job? = null

    private var currentCallDocId: String? = null
    private var activeChannelName: String? = null

    private val _remoteUid = MutableStateFlow<Int?>(null)
    val remoteUid: StateFlow<Int?> = _remoteUid.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isCameraOff = MutableStateFlow(false)
    val isCameraOff: StateFlow<Boolean> = _isCameraOff.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _callEnded = MutableStateFlow(false)
    val callEnded: StateFlow<Boolean> = _callEnded.asStateFlow()

    private val _partnerName = MutableStateFlow<String?>(null)
    val partnerName: StateFlow<String?> = _partnerName.asStateFlow()

    private val _partnerPhotoUrl = MutableStateFlow<String?>(null)
    val partnerPhotoUrl: StateFlow<String?> = _partnerPhotoUrl.asStateFlow()

    private val _endReason = MutableStateFlow<String?>(null)
    val endReason: StateFlow<String?> = _endReason.asStateFlow()

    private val _currentStatus = MutableStateFlow<String?>(null)
    val currentStatus: StateFlow<String?> = _currentStatus.asStateFlow()

    private val _callDuration = MutableStateFlow(0)
    val callDuration: StateFlow<Int> = _callDuration.asStateFlow()

    private var timerJob: Job? = null
    private var timeoutJob: Job? = null
    private var callStartTime: Long = 0

    private fun startRingtone(context: Context) {
        try {
            stopRingtone()
            mediaPlayer = MediaPlayer.create(context, R.raw.ahizeli_telefon).apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            Log.e("CallViewModel", "Zil sesi hatası: ${e.message}")
        }
    }

    private fun stopRingtone() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            Log.e("CallViewModel", "Zil sesi durdurma hatası: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    fun initEngine(context: Context, isVideoCall: Boolean, handler: IRtcEngineEventHandler) {
        if (rtcEngine != null) return
        try {
            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = Constants.AGORA_APP_ID
                mEventHandler = handler
                mChannelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION
                mAudioScenario = io.agora.rtc2.Constants.AUDIO_SCENARIO_DEFAULT
            }
            rtcEngine = RtcEngine.create(config).apply {
                setChannelProfile(io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION)
                enableAudio()
                enableLocalAudio(true)
                adjustRecordingSignalVolume(100)
                adjustPlaybackSignalVolume(100)
            }

            if (isVideoCall) {
                rtcEngine?.enableVideo()
                rtcEngine?.startPreview()
                rtcEngine?.setEnableSpeakerphone(true)
                _isSpeakerOn.value = true

                val videoConfig = VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE,
                )
                rtcEngine?.setVideoEncoderConfiguration(videoConfig)
            } else {
                rtcEngine?.disableVideo()
                rtcEngine?.setEnableSpeakerphone(false)
                _isSpeakerOn.value = false
            }
        } catch (e: Exception) {
            Log.e("CallViewModel", "Agora Engine Init Error: ${e.message}")
        }
    }

    fun joinChannel(context: Context, channelName: String, isVideoCall: Boolean, isIncoming: Boolean) {
        _callEnded.value = false
        _endReason.value = null
        _remoteUid.value = null
        callStartTime = Timestamp.now().seconds - 2
        activeChannelName = channelName

        val options = ChannelMediaOptions().apply {
            autoSubscribeAudio = true
            autoSubscribeVideo = isVideoCall
            publishMicrophoneTrack = true
            publishCameraTrack = isVideoCall
            clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION
        }

        rtcEngine?.joinChannel(null, channelName, 0, options)
        rtcEngine?.muteLocalAudioStream(false)

        if (isVideoCall) {
            rtcEngine?.muteLocalVideoStream(false)
        }

        val currentUserId = auth.currentUser?.uid ?: ""
        val partnerId = channelName.split("_").find { it != currentUserId } ?: ""

        if (partnerId.isNotEmpty()) {
            viewModelScope.launch {
                val (name, photo) = callRepository.getPartnerInfo(partnerId)
                _partnerName.value = name ?: "Anoba Kullanıcısı"
                _partnerPhotoUrl.value = photo
            }
        }

        if (!isIncoming) {
            startRingtone(context)
            val newDocId = "${channelName}_${UUID.randomUUID()}"
            currentCallDocId = newDocId

            timeoutJob = viewModelScope.launch {
                delay(45000)
                if (_remoteUid.value == null && (_currentStatus.value == CallStatus.RINGING.name || _currentStatus.value == null)) {
                    leaveChannel(channelName)
                }
            }

            viewModelScope.launch {
                val callData = CallModel(
                    id = newDocId,
                    callerId = currentUserId,
                    receiverId = partnerId,
                    channelName = channelName,
                    isVideoCall = isVideoCall,
                    status = CallStatus.RINGING.name,
                    timestamp = Timestamp.now(),
                )
                callRepository.createCall(callData)
                listenToFirestoreCall(newDocId)
            }
        } else {
            viewModelScope.launch {
                val activeDocId = callRepository.findActiveRingingCall(channelName, currentUserId)
                if (activeDocId != null) {
                    currentCallDocId = activeDocId
                    callRepository.updateCallStatus(activeDocId, CallStatus.ACCEPTED)
                    listenToFirestoreCall(activeDocId)
                }
            }
        }
    }

    private fun listenToFirestoreCall(docId: String) {
        firestoreListenJob?.cancel()
        firestoreListenJob = viewModelScope.launch {
            callRepository.listenToCall(docId).collect { call ->
                val status = call?.status
                val timestamp = call?.timestamp

                _currentStatus.value = status

                if (status == CallStatus.ACCEPTED.name) {
                    stopRingtone()
                    timeoutJob?.cancel()
                }

                if (status == CallStatus.REJECTED.name || status == CallStatus.ENDED.name) {
                    if (timestamp != null && timestamp.seconds >= callStartTime) {
                        localLeave(status)
                    } else if (timestamp == null) {
                        localLeave(status)
                    }
                }
            }
        }
    }

    fun leaveChannel(channelName: String) {
        stopTimer()
        stopRingtone()
        timeoutJob?.cancel()
        rtcEngine?.stopPreview()
        firestoreListenJob?.cancel()

        viewModelScope.launch {
            val docIdToUpdate = currentCallDocId
            if (docIdToUpdate != null) {
                callRepository.updateCallStatus(docIdToUpdate, CallStatus.ENDED)
            } else {
                callRepository.endAnyActiveCall(channelName)
            }

            rtcEngine?.leaveChannel()
            _remoteUid.value = null
            _callEnded.value = true
            currentCallDocId = null
            activeChannelName = null
        }
    }

    private fun localLeave(reason: String?) {
        stopTimer()
        stopRingtone()
        timeoutJob?.cancel()
        rtcEngine?.stopPreview()
        rtcEngine?.leaveChannel()
        firestoreListenJob?.cancel()
        currentCallDocId = null
        activeChannelName = null
        _remoteUid.value = null
        _endReason.value = reason
        _callEnded.value = true
    }

    fun toggleMute() {
        val newState = !_isMuted.value
        _isMuted.value = newState
        rtcEngine?.muteLocalAudioStream(newState)
    }

    fun toggleCamera() {
        val newState = !_isCameraOff.value
        _isCameraOff.value = newState
        rtcEngine?.muteLocalVideoStream(newState)
    }

    fun switchCamera() {
        rtcEngine?.switchCamera()
    }

    fun toggleSpeaker() {
        val newState = !_isSpeakerOn.value
        _isSpeakerOn.value = newState
        rtcEngine?.setEnableSpeakerphone(newState)
    }

    fun setupLocalVideo(surfaceView: SurfaceView) {
        rtcEngine?.setupLocalVideo(io.agora.rtc2.video.VideoCanvas(surfaceView, io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        rtcEngine?.setupRemoteVideo(io.agora.rtc2.video.VideoCanvas(surfaceView, io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    fun onRemoteUserJoined(uid: Int) {
        _remoteUid.value = uid
        stopRingtone()
        timeoutJob?.cancel()
        rtcEngine?.muteRemoteAudioStream(uid, false)
        startTimer()
    }

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            val maxDurationSeconds = 45 * 60 // 45 Dakika (Güvenlik Sınırı)
            while (true) {
                delay(1000)
                _callDuration.value++
                
                // 🚀 GÜVENLİK ZAMANLAYICISI (Maliyet Kontrolü)
                if (_callDuration.value >= maxDurationSeconds) {
                    Log.w("CallViewModel", "Arama güvenlik sınırı (45 dk) aşıldı, otomatik sonlandırılıyor.")
                    leaveChannel(activeChannelName ?: "")
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun onRemoteUserLeft() {
        _remoteUid.value = null
        stopTimer()
        _callEnded.value = true
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        stopRingtone()
        timeoutJob?.cancel()
        firestoreListenJob?.cancel()
        
        rtcEngine?.leaveChannel()
        rtcEngine?.stopPreview()
        rtcEngine = null
    }
}
