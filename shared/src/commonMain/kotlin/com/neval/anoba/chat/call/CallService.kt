package com.neval.anoba.chat.call

interface CallService {
    fun startCall(channelName: String, isVideoCall: Boolean)
    fun endCall()
    fun toggleMute(isMuted: Boolean)
    fun toggleCamera(isCameraOff: Boolean)
}
