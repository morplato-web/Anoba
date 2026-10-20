package com.neval.anoba.video

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.camera.video.Recording
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class RecordingState {
    data object Idle : RecordingState()
    data object InProgress : RecordingState()
    data class Complete(val uri: Uri) : RecordingState()
    data class Error(val message: String) : RecordingState()
}

class CameraViewModel : ViewModel() {

    private val _hasCameraPermission = MutableStateFlow(false)
    val hasCameraPermission = _hasCameraPermission.asStateFlow()

    private val _hasAudioPermission = MutableStateFlow(false)
    val hasAudioPermission = _hasAudioPermission.asStateFlow()

    private val _recordedVideoUri = MutableStateFlow<Uri?>(null)
    val recordedVideoUri = _recordedVideoUri.asStateFlow()

    private val _capturedPhotoUri = MutableStateFlow<Uri?>(null)
    val capturedPhotoUri = _capturedPhotoUri.asStateFlow()

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState = _recordingState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private var activeRecording: Recording? = null

    fun setUploading(isUploading: Boolean) {
        _isUploading.value = isUploading
    }

    fun checkInitialPermissions(context: Context) {
        _hasCameraPermission.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        _hasAudioPermission.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onPermissionResult(
        permissions: Map<String, Boolean>,
        cameraPermission: String,
        audioPermission: String
    ) {
        _hasCameraPermission.value = permissions[cameraPermission] ?: _hasCameraPermission.value
        _hasAudioPermission.value = permissions[audioPermission] ?: _hasAudioPermission.value
    }

    @SuppressLint("MissingPermission")
    fun startRecording(recording: Recording) {
        if (_isRecording.value || _isUploading.value) {
            Log.w("CameraViewModel", "Kayıt zaten devam ediyor veya yükleme yapılıyor.")
            return
        }
        activeRecording = recording
        _isRecording.value = true
        _recordingState.value = RecordingState.InProgress
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        try {
            activeRecording?.stop()
            activeRecording = null
            _isRecording.value = false
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Durdurma hatası", e)
        }
    }

    fun onVideoRecorded(uri: Uri) {
        _recordedVideoUri.value = uri
        _recordingState.value = RecordingState.Complete(uri)
        _isRecording.value = false
        activeRecording = null
    }

    fun onPhotoCaptured(uri: Uri) {
        _capturedPhotoUri.value = uri
        _isRecording.value = false
    }

    fun clearMedia() {
        _recordedVideoUri.value = null
        _capturedPhotoUri.value = null
        _recordingState.value = RecordingState.Idle
        _isRecording.value = false
    }

    fun setRecordingError(message: String) {
        Log.e("CameraViewModel", "Recording Error: $message")
        _recordingState.value = RecordingState.Error(message)
        _isRecording.value = false
        activeRecording = null
    }

    override fun onCleared() {
        super.onCleared()
        activeRecording?.stop()
        activeRecording = null
    }
}