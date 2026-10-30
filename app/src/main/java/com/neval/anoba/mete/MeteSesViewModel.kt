package com.neval.anoba.mete

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.firestore.FirebaseFirestore
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.utils.AnalyticsHelper
import com.neval.anoba.common.utils.MediaCacheManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

enum class MeteLanguage(val suffix: String) {
    TR("tr"),
    EN("en"),
    FR("fr"),
    DE("de"),
    AR("ar"),
    ES("es"),
    IT("it"),
    RU("ru"),
    SW("sw"),
    AF("af"),
    JP("jp"),
    ZH("zh"),
    PT("pt"),
    KO("ko"),
    HI("hi"),
}

data class MeteSectionGroup(
    val sectionName: String,
    val words: List<MeteWordItem>,
)

@OptIn(UnstableApi::class)
class MeteSesViewModel(
    private val repository: MeteRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val authService: AuthServiceInterface,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private var exoPlayer: ExoPlayer? = null
    private var wordsJob: Job? = null
    private var storiesJob: Job? = null

    private val _selectedLanguage = MutableStateFlow(MeteLanguage.TR)
    val selectedLanguage: StateFlow<MeteLanguage> = _selectedLanguage.asStateFlow()

    // Sol panel: Bölüm grupları ve içindeki kelimeler
    private val _sectionGroups = MutableStateFlow<List<MeteSectionGroup>>(emptyList())
    val sectionGroups: StateFlow<List<MeteSectionGroup>> = _sectionGroups.asStateFlow()

    // Sağ panel: Hikaye / Çizgi filmler
    private val _storyVideos = MutableStateFlow<List<MeteStoryItem>>(emptyList())
    val storyVideos: StateFlow<List<MeteStoryItem>> = _storyVideos.asStateFlow()

    // Oyun modu için tüm kelimelerin düz listesi
    private val _allPuzzlesForGame = MutableStateFlow<List<MeteWordItem>>(emptyList())
    val allPuzzlesForGame: StateFlow<List<MeteWordItem>> = _allPuzzlesForGame.asStateFlow()

    private val _isSystemAudioPlaying = MutableStateFlow(value = false)
    val isSystemAudioPlaying: StateFlow<Boolean> = _isSystemAudioPlaying.asStateFlow()

    private val _unlockedSections = MutableStateFlow<Set<String>>(emptySet())
    val unlockedSections: StateFlow<Set<String>> = _unlockedSections.asStateFlow()

    fun loadUnlockedSections(context: Context) {
        val prefs = context.getSharedPreferences("mete_unlocked_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("unlocked_sections", emptySet()) ?: emptySet()
        _unlockedSections.value = set
    }

    fun unlockSection(context: Context, sectionName: String) {
        val updated = _unlockedSections.value + sectionName
        _unlockedSections.value = updated
        val prefs = context.getSharedPreferences("mete_unlocked_prefs", Context.MODE_PRIVATE)
        prefs.edit { putStringSet("unlocked_sections", updated) }
    }

    fun startLoading() {
        if (_sectionGroups.value.isEmpty() && authService.isLoggedInFirebase()) {
            loadLanguageData(_selectedLanguage.value)
        }
    }

    private fun loadLanguageData(lang: MeteLanguage) {
        wordsJob?.cancel()
        storiesJob?.cancel()

        val wordsCollection = "mete_${lang.suffix}"
        val storiesCollection = "mete_stories_${lang.suffix}"

        // 1. SOL PANEL KELİMELERİNİ ÇEK VE KARIŞTIR
        wordsJob = viewModelScope.launch {
            if (authService.isLoggedInFirebase()) {
                try {
                    repository.getWordsFromCollection(wordsCollection)
                        .catch { e ->
                            Log.e("MeteSesViewModel", "Kelimeler çekilemedi: $wordsCollection", e)
                            emit(emptyList())
                        }
                        .collect { items: List<MeteWordItem> ->
                            _allPuzzlesForGame.value = items

                            val grouped = items.groupBy { it.sectionName }
                                .map { (name, list) ->
                                    MeteSectionGroup(
                                        sectionName = name,
                                        words = list.shuffled(),
                                    )
                                }.shuffled()

                            _sectionGroups.value = grouped
                        }
                } catch (e: Exception) {
                    if (e is CancellationException) {
                        throw e
                    }
                    Log.e("MeteSesViewModel", "Hata kelimeler: ${e.message}", e)
                }
            }
        }

        // 2. SAĞ PANEL HİKAYELERİNİ ÇEK VE KARIŞTIR
        storiesJob = viewModelScope.launch {
            if (authService.isLoggedInFirebase()) {
                try {
                    repository.getStoriesFromCollection(storiesCollection)
                        .catch { e ->
                            Log.e("MeteSesViewModel", "Hikayeler çekilemedi: $storiesCollection", e)
                            emit(emptyList())
                        }
                        .collect { stories: List<MeteStoryItem> ->
                            _storyVideos.value = stories.shuffled()
                        }
                } catch (e: Exception) {
                    if (e is CancellationException) {
                        throw e
                    }
                    Log.e("MeteSesViewModel", "Hata hikayeler: ${e.message}", e)
                }
            }
        }
    }

    fun selectLanguage(lang: MeteLanguage) {
        if (_selectedLanguage.value == lang) return
        _selectedLanguage.value = lang
        analyticsHelper.logEvent("mete_language_changed") {
            putString("language", lang.name)
        }
        loadLanguageData(lang)
    }

    fun addNewSection(sectionName: String) {
        val collName = "mete_${_selectedLanguage.value.suffix}"
        val docId = "ornek_${System.currentTimeMillis().toString().takeLast(6)}"

        val cleanTemplate = mapOf(
            "id" to 1L,
            "sectionName" to sectionName,
            "type" to "image",
            "imageUrl" to null,
            "audioUrl" to null,
            "animationUrl" to null,
            "videoUrl" to null,
        )

        firestore.collection(collName).document(docId).set(cleanTemplate)
            .addOnSuccessListener {
                Log.d("MeteSesViewModel", "Yeni temiz bölüm şablonu açıldı: $sectionName ($docId)")
            }
    }

    @SuppressLint("DiscouragedApi")
    fun playSound(context: Context, soundSource: String?) {
        if (soundSource.isNullOrBlank()) return

        if (exoPlayer == null) {
            exoPlayer = MediaCacheManager.createPlayer(context.applicationContext).apply {
                addListener(
                    object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isSystemAudioPlaying.value = isPlaying
                        }
                    },
                )
            }
        }

        try {
            val mediaItem = if (soundSource.startsWith("http")) {
                MediaItem.fromUri(soundSource)
            } else {
                val resId = context.resources.getIdentifier(soundSource, "raw", context.packageName)
                if (resId != 0) {
                    MediaItem.fromUri("rawresource:///${context.packageName}/$resId")
                } else return
            }

            exoPlayer?.apply {
                stop()
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        } catch (e: Exception) {
            Log.e("MeteSesViewModel", "Ses çalma hatası", e)
        }
    }

    fun stopSound() {
        exoPlayer?.stop()
        _isSystemAudioPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        wordsJob?.cancel()
        storiesJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}
