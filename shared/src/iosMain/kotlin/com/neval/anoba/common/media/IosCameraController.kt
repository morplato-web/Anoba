package com.neval.anoba.common.media

class IosCameraController : CameraController {
    override fun takePhoto(onPhotoCaptured: (filePath: String) -> Unit) {}
    override fun startRecording(onVideoRecorded: (filePath: String) -> Unit) {}
    override fun stopRecording() {}
}
