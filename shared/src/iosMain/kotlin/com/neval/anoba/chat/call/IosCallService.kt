@file:Suppress("unused")

package com.neval.anoba.chat.call

class IosCallService : CallService {
    override fun startCall(channelName: String, isVideoCall: Boolean) {}
    override fun endCall() {}
    override fun toggleMute(isMuted: Boolean) {}
    override fun toggleCamera(isCameraOff: Boolean) {}
}
