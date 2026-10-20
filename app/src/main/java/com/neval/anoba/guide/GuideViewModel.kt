package com.neval.anoba.guide

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.neval.anoba.home.ContentItem
import com.neval.anoba.letter.LetterModel
import com.neval.anoba.photo.PhotoModel
import com.neval.anoba.ses.SesModel
import com.neval.anoba.video.VideoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GuideViewModel(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _guideContent = MutableStateFlow<List<ContentItem>>(emptyList())
    val guideContent: StateFlow<List<ContentItem>> = _guideContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadGuideContent()
    }

    fun loadGuideContent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val combined = supervisorScope {
                    val lettersDeferred = async {
                        try {
                            firestore.collection("letters").whereEqualTo("is_guide_content", true).get().await()
                                .toObjects(LetterModel::class.java).map { ContentItem.LetterContent(it) }
                        } catch (e: Exception) {
                            Log.e("GuideViewModel", "Letters loading error", e)
                            emptyList()
                        }
                    }

                    val photosDeferred = async {
                        try {
                            firestore.collection("photos").whereEqualTo("is_guide_content", true).get().await()
                                .toObjects(PhotoModel::class.java).map { ContentItem.PhotoContent(it) }
                        } catch (e: Exception) {
                            Log.e("GuideViewModel", "Photos loading error", e)
                            emptyList()
                        }
                    }

                    val videosDeferred = async {
                        try {
                            firestore.collection("videos").whereEqualTo("is_guide_content", true).get().await()
                                .toObjects(VideoModel::class.java).map { ContentItem.VideoContent(it) }
                        } catch (e: Exception) {
                            Log.e("GuideViewModel", "Videos loading error", e)
                            emptyList()
                        }
                    }

                    // 🚀 DÜZELTME: "ses" yerine projede standart kullanılan "sesler" yapıldı
                    val sesDeferred = async {
                        try {
                            firestore.collection("sesler").whereEqualTo("is_guide_content", true).get().await()
                                .toObjects(SesModel::class.java).map { ContentItem.AudioContent(it) }
                        } catch (e: Exception) {
                            Log.e("GuideViewModel", "Audio loading error", e)
                            emptyList()
                        }
                    }

                    lettersDeferred.await() + photosDeferred.await() +
                            videosDeferred.await() + sesDeferred.await()
                }

                // 🚀 DÜZELTME: Güvenli tarih sıralaması
                withContext(Dispatchers.IO) {
                    val sorted = combined.sortedByDescending { item ->
                        when (item) {
                            is ContentItem.LetterContent -> item.letter.publishTimestamp?.time ?: item.letter.createdAt?.time ?: 0L
                            is ContentItem.PhotoContent -> item.photo.publishTimestamp?.time ?: item.photo.createdAt?.time ?: 0L
                            is ContentItem.VideoContent -> item.video.publishTimestamp?.time ?: item.video.createdAt?.time ?: 0L
                            is ContentItem.AudioContent -> item.audio.publishTimestamp?.time ?: item.audio.createdAt?.time ?: 0L
                        }
                    }
                    _guideContent.value = sorted
                }

                Log.d("GuideViewModel", "Rehber içerikleri başarıyla ve hızla yüklendi.")
            } catch (e: Exception) {
                Log.e("GuideViewModel", "Rehber yükleme hatası", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}