package com.neval.anoba.common.media

interface CameraController {
    fun takePhoto(onPhotoCaptured: (filePath: String) -> Unit)
    fun startRecording(onVideoRecorded: (filePath: String) -> Unit)
    fun stopRecording()
}
