package com.neval.anoba.video

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import com.neval.anoba.common.state.UiState
import com.neval.anoba.mycontent.TrimState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@UnstableApi
class VideoEditViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _videoDetails = MutableStateFlow<UiState<VideoModel>>(UiState.Loading)
    val videoDetails = _videoDetails.asStateFlow()

    private val _trimState = MutableStateFlow<TrimState>(TrimState.Idle)
    val trimState = _trimState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _rotationDegrees = MutableStateFlow(0)
    val rotationDegrees = _rotationDegrees.asStateFlow()

    private val _speedMultiplier = MutableStateFlow(1.0f)
    val speedMultiplier = _speedMultiplier.asStateFlow()

    private val _videoDuration = MutableStateFlow(0L)
    val videoDuration = _videoDuration.asStateFlow()

    private val _sliderPositions = MutableStateFlow(0f..1f)
    val sliderPositions = _sliderPositions.asStateFlow()

    private var transformer: Transformer? = null

    fun loadVideoDetails(videoId: String) {
        viewModelScope.launch {
            _videoDetails.value = UiState.Loading
            try {
                val video = videoRepository.getVideoById(videoId)
                if (video != null) {
                    _videoDetails.value = UiState.Success(video)
                } else {
                    _videoDetails.value = UiState.Error("Video bulunamadı.")
                }
            } catch (e: Exception) {
                _videoDetails.value = UiState.Error(e.message ?: "Hata oluştu.")
            }
        }
    }

    fun setVideoDuration(duration: Long) {
        if (_videoDuration.value == 0L) {
            _videoDuration.value = duration
            _sliderPositions.value = 0f..duration.toFloat()
        }
    }

    fun updateSliderPositions(newPositions: ClosedFloatingPointRange<Float>) {
        _sliderPositions.value = newPositions
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun rotateVideo() {
        _rotationDegrees.value = (_rotationDegrees.value + 90) % 360
    }

    fun updateOnlyTextDetails(videoId: String, title: String, personalNote: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = videoRepository.updateVideoTextDetails(videoId, title, personalNote)
            withContext(Dispatchers.Main) {
                onComplete(success)
            }
        }
    }

    fun trimVideo(context: Context, uri: Uri) {
        val startMs = _sliderPositions.value.start.toLong()
        val endMs = _sliderPositions.value.endInclusive.toLong()
        val isMuted = _isMuted.value
        val rotationDegrees = _rotationDegrees.value
        val speedMultiplier = _speedMultiplier.value

        _trimState.value = TrimState.InProgress(0)

        val outputVideo = File(context.cacheDir, "trimmed_video_${System.currentTimeMillis()}.mp4")

        // 🚀 H.264, AAC ve Faststart uyumlu dışa aktarma konfigürasyonu
        val encoderFactory = DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(
                VideoEncoderSettings.Builder()
                    .setBitrate(3_500_000)
                    .build()
            )
            .build()

        transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .setEncoderFactory(encoderFactory)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    _trimState.value = TrimState.Success(Uri.fromFile(outputVideo))
                    transformer = null
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    _trimState.value = TrimState.Error(exportException.message ?: "Bilinmeyen bir hata oluştu")
                    transformer = null
                }
            })
            .build()

        val sourceMediaItem = MediaItem.fromUri(uri)

        val clippedMediaItem = sourceMediaItem.buildUpon()
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(startMs)
                    .setEndPositionMs(endMs)
                    .build()
            )
            .build()

        val videoEffects = mutableListOf<Effect>()
        if (rotationDegrees != 0) {
            val rotation = ScaleAndRotateTransformation.Builder()
                .setRotationDegrees(rotationDegrees.toFloat())
                .build()
            videoEffects.add(rotation)
        }
        if (speedMultiplier != 1.0f) {
            val speedEffect = SpeedChangeEffect(speedMultiplier)
            videoEffects.add(speedEffect)
        }
        // Maksimum 1080p dikey çerçeveleme
        videoEffects.add(Presentation.createForHeight(1920))

        val audioProcessors = mutableListOf<AudioProcessor>()
        if (speedMultiplier != 1.0f && !isMuted) {
            val sonicAudioProcessor = SonicAudioProcessor()
            audioProcessors.add(sonicAudioProcessor)
        }

        val editedMediaItem = EditedMediaItem.Builder(clippedMediaItem)
            .setRemoveAudio(isMuted)
            .setEffects(Effects(audioProcessors, videoEffects))
            .build()

        transformer?.start(editedMediaItem, outputVideo.absolutePath)

        viewModelScope.launch(Dispatchers.IO) {
            val progressHolder = ProgressHolder()
            while (transformer != null && _trimState.value is TrimState.InProgress) {
                withContext(Dispatchers.Main) {
                    transformer?.getProgress(progressHolder)
                    _trimState.value = TrimState.InProgress(progressHolder.progress)
                }
                delay(250)
            }
        }
    }

    fun saveChanges(videoId: String, newVideoUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _trimState.value = TrimState.Saving
            }
            val success = videoRepository.updateVideoFile(videoId, newVideoUri)
            withContext(Dispatchers.Main) {
                if (success) {
                    _trimState.value = TrimState.UploadComplete
                } else {
                    _trimState.value = TrimState.Error("Video güncellenemedi.")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        transformer?.cancel()
    }
}