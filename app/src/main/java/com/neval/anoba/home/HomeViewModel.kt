@file:OptIn(ExperimentalCoroutinesApi::class)

package com.neval.anoba.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.neval.anoba.common.repository.PinnedItemRepository
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.state.UiState
import com.neval.anoba.letter.LetterModel
import com.neval.anoba.letter.LetterRepository
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.models.AnobaNotification
import com.neval.anoba.photo.PhotoModel
import com.neval.anoba.photo.PhotoRepository
import com.neval.anoba.photo.ShareState
import com.neval.anoba.photo.photoemoji.PhotoEmojiReactionRepository
import com.neval.anoba.ses.SesModel
import com.neval.anoba.ses.SesRepository
import com.neval.anoba.video.VideoModel
import com.neval.anoba.video.VideoRepository
import com.neval.anoba.follow.FollowRepository
import com.neval.anoba.video.videoemoji.VideoEmojiReactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AnimationPackage(
    val assets: List<AnimationAsset>,
    val soundUrl: String? = null
)

class HomeViewModel(
    private val letterRepository: LetterRepository,
    private val photoRepository: PhotoRepository,
    private val videoRepository: VideoRepository,
    private val sesRepository: SesRepository,
    private val firestore: FirebaseFirestore,
    private val authService: AuthServiceInterface,
    private val pinnedItemRepository: PinnedItemRepository,
    private val followRepository: FollowRepository,
    private val photoReactionRepository: PhotoEmojiReactionRepository,
    private val videoReactionRepository: VideoEmojiReactionRepository
) : ViewModel() {

    private val _allContentItems = MutableStateFlow<UiState<List<ContentItem>>>(UiState.Idle)
    val allContentItems: StateFlow<UiState<List<ContentItem>>> = _allContentItems.asStateFlow()

    // 🚀 KARANTİNA (İNCELEME) HAVUZU
    private val _quarantineContentItems = MutableStateFlow<List<ContentItem>>(emptyList())
    val quarantineContentItems: StateFlow<List<ContentItem>> = _quarantineContentItems.asStateFlow()

    // 🏆 1000+ VİTRİN HAVUZU
    private val _popularContentItems = MutableStateFlow<List<ContentItem>>(emptyList())
    val popularContentItems: StateFlow<List<ContentItem>> = _popularContentItems.asStateFlow()

    // 🌍 HARİTA İÇİN FİLTRESİZ GLOBAL VERİ
    private val _globalMapItems = MutableStateFlow<List<ContentItem>>(emptyList())
    val globalMapItems: StateFlow<List<ContentItem>> = _globalMapItems.asStateFlow()

    private val _shareState = MutableStateFlow<ShareState>(ShareState.Idle)
    val shareState: StateFlow<ShareState> = _shareState.asStateFlow()

    private val _isPinnedFilterActive = MutableStateFlow(false)
    val isPinnedFilterActive = _isPinnedFilterActive.asStateFlow()

    private val _isFollowingFilterActive = MutableStateFlow(false)
    val isFollowingFilterActive = _isFollowingFilterActive.asStateFlow()

    val pinnedIds: StateFlow<Set<String>> = authService.authStateFlow.flatMapLatest { state ->
        if (state is AuthState.Authenticated) {
            pinnedItemRepository.getAllPinnedItems(state.uid).map { list -> list.map { it.contentId }.toSet() }
        } else {
            flowOf(emptySet())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _notifications = MutableStateFlow<List<AnobaNotification>>(emptyList())
    val notifications: StateFlow<List<AnobaNotification>> = _notifications.asStateFlow()

    val hasUnreadNotifications: StateFlow<Boolean> = _notifications.map { list ->
        list.any { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var notificationListener: ListenerRegistration? = null

    private var _stableFeedList = emptyList<ContentItem>()
    private val _sessionShuffledOldMap = mutableMapOf<String, Long>()
    private val freshPoolLimit = 10
    private val _refreshTrigger = MutableStateFlow(0)

    private val _packageFall = MutableStateFlow<AnimationPackage?>(null)
    private val _packageFloat = MutableStateFlow<AnimationPackage?>(null)
    private val _packageFly = MutableStateFlow<AnimationPackage?>(null)
    private val _packagePop = MutableStateFlow<AnimationPackage?>(null)

    val policeAssets = MutableStateFlow<List<AnimationAsset>>(emptyList())
    val commandoAssets = MutableStateFlow<List<AnimationAsset>>(emptyList())
    val bekciAssets = MutableStateFlow<List<AnimationAsset>>(emptyList())

    val isPoliceAnimationActive = MutableStateFlow(false)
    val isCommandoAnimationActive = MutableStateFlow(false)
    val isBekciAnimationActive = MutableStateFlow(false)

    val policeSoundUrl = MutableStateFlow<String?>(null)
    val commandoSoundUrl = MutableStateFlow<String?>(null)
    val bekciSoundUrl = MutableStateFlow<String?>(null)

    private var policeJob: Job? = null
    private var commandoJob: Job? = null
    private var bekciJob: Job? = null

    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        _allContentItems.value = UiState.Loading
        stopAllAnimationsOnStartup()

        loadContent()
        loadAnimationAssets()
        listenAnimationStatus()
        observeUserForNotifications()
    }

    private fun observeUserForNotifications() {
        viewModelScope.launch {
            authService.authStateFlow.collect { state ->
                if (state is AuthState.Authenticated) {
                    listenToNotifications(state.uid)
                } else {
                    notificationListener?.remove()
                    _notifications.value = emptyList()
                }
            }
        }
    }

    private fun listenToNotifications(userId: String) {
        if (userId.isBlank()) return
        notificationListener?.remove()

        notificationListener = firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(AnobaNotification::class.java)?.apply { id = doc.id }
                }

                _notifications.value = list.sortedWith(
                    compareBy<AnobaNotification> { it.isRead }
                        .thenByDescending { it.timestampLong }
                )
            }
    }

    fun markNotificationAsRead(notificationId: String) {
        val uid = authService.getCurrentUserId()
        if (uid.isBlank() || notificationId.isBlank()) return
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(uid)
                    .collection("notifications")
                    .document(notificationId)
                    .update("isRead", true)
                    .await()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Bildirim okundu güncelleme hatası: ${e.message}")
            }
        }
    }

    fun markAllNotificationsAsRead() {
        val uid = authService.getCurrentUserId()
        if (uid.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val unreadDocs = firestore.collection("users")
                    .document(uid)
                    .collection("notifications")
                    .whereEqualTo("isRead", false)
                    .get()
                    .await()

                if (!unreadDocs.isEmpty) {
                    val batch = firestore.batch()
                    for (doc in unreadDocs.documents) {
                        batch.update(doc.reference, "isRead", true)
                    }
                    batch.commit().await()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Tümünü okundu yapma hatası: ${e.message}")
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        val uid = authService.getCurrentUserId()
        if (uid.isBlank() || notificationId.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("users")
                    .document(uid)
                    .collection("notifications")
                    .document(notificationId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Bildirim silme hatası: ${e.message}")
            }
        }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                photoRepository.deletePhoto(photoId)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Fotoğraf silme hatası: ${e.message}")
            }
        }
    }

    fun unpublishPhoto(photoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("photos").document(photoId).update("is_published", false).await()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Fotoğraf yayından kaldırma hatası: ${e.message}")
            }
        }
    }

    fun deleteVideo(videoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                videoRepository.deleteVideo(videoId)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Video silme hatası: ${e.message}")
            }
        }
    }

    fun unpublishVideo(videoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("videos").document(videoId).update("is_published", false).await()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Video yayından kaldırma hatası: ${e.message}")
            }
        }
    }

    fun deleteSes(sesId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sesRepository.deleteSes(sesId)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Ses silme hatası: ${e.message}")
            }
        }
    }

    fun unpublishSes(sesId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("ses").document(sesId).update("is_published", false).await()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Ses yayından kaldırma hatası: ${e.message}")
            }
        }
    }

    fun deleteLetter(letterId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                letterRepository.deleteLetter(letterId)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Mektup silme hatası: ${e.message}")
            }
        }
    }

    fun unpublishLetter(letterId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("letters").document(letterId).update("is_published", false).await()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Mektup yayından kaldırma hatası: ${e.message}")
            }
        }
    }

    fun deleteAllNotifications() {
        val uid = authService.getCurrentUserId()
        if (uid.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allDocs = firestore.collection("users")
                    .document(uid)
                    .collection("notifications")
                    .get()
                    .await()

                if (!allDocs.isEmpty) {
                    val batch = firestore.batch()
                    for (doc in allDocs.documents) {
                        batch.delete(doc.reference)
                    }
                    batch.commit().await()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Tüm bildirimleri silme hatası: ${e.message}")
            }
        }
    }

    private fun stopAllAnimationsOnStartup() {
        viewModelScope.launch {
            val anims = listOf("police_rain", "commando_rain", "bekci_rain")
            anims.forEach { doc ->
                try {
                    firestore.collection("animations").document(doc)
                        .set(mapOf("isActive" to false), SetOptions.merge())
                } catch (_: Exception) { }
            }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            if (_stableFeedList.isEmpty()) {
                _allContentItems.value = UiState.Loading
            }

            launch(Dispatchers.IO) {
                photoRepository.getPhotos().catch { Log.e("HomeSync", "Foto çekme hatası", it) }.collect()
                letterRepository.getLetters().catch { Log.e("HomeSync", "Mektup çekme hatası", it) }.collect()
                sesRepository.getSesler().catch { Log.e("HomeSync", "Ses çekme hatası", it) }.collect()
                videoRepository.getVideos().catch { Log.e("HomeSync", "Video çekme hatası", it) }.collect()
            }

            // 🚀 KARANTİNA AKIŞI
            combine(
                photoRepository.getQuarantinePhotos(),
                letterRepository.getQuarantineLetters(),
                sesRepository.getQuarantineSesler(),
                videoRepository.getQuarantineVideos()
            ) { p, l, s, v ->
                mapToContentItems(p, l, s, v)
            }.onEach { _quarantineContentItems.value = it }.launchIn(this)

            // 🏆 1000+ VİTRİN AKIŞI (Filtreli: Karantinadakiler veya Yayından Kalkanlar Görünmez)
            combine(
                photoRepository.getPopularPhotos(),
                letterRepository.getPopularLetters(),
                sesRepository.getPopularSesler(),
                videoRepository.getPopularVideos()
            ) { p, l, s, v ->
                mapToContentItems(p, l, s, v).filter { item ->
                    val isFlagged = when(item) {
                        is ContentItem.PhotoContent -> item.photo.isFlagged
                        is ContentItem.VideoContent -> item.video.isFlagged
                        is ContentItem.LetterContent -> item.letter.isFlagged
                        is ContentItem.AudioContent -> item.audio.isFlagged
                    }
                    val isPublished = when(item) {
                        is ContentItem.PhotoContent -> item.photo.isPublished
                        is ContentItem.VideoContent -> item.video.isPublished
                        is ContentItem.LetterContent -> item.letter.isPublished
                        is ContentItem.AudioContent -> item.audio.isPublished
                    }
                    !isFlagged && isPublished
                }
            }.onEach { _popularContentItems.value = it }.launchIn(this)

            val rawContentFlow = combine(
                photoRepository.getPhotosOffline(),
                letterRepository.getLettersOffline(),
                sesRepository.getSeslerOffline(),
                videoRepository.getVideosOffline(),
                _refreshTrigger
            ) { p, l, s, v, _ ->
                mapToContentItems(p, l, s, v)
            }

            // 🌍 HARİTA İÇİN TEMİZ VERİ (Filtresiz)
            rawContentFlow.onEach { _globalMapItems.value = it }.launchIn(this)

            // 🏠 ANA AKIŞ İÇİN FİLTRELİ VERİ
            rawContentFlow
                .map { newRawList ->
                    if (newRawList.isEmpty()) return@map emptyList()

                    val sortedAll = newRawList.sortedByDescending { it.sortDate.time }
                    val freshPool = sortedAll.take(freshPoolLimit)
                    val oldPool = sortedAll.drop(freshPoolLimit)

                    oldPool.forEach { item ->
                        if (!_sessionShuffledOldMap.containsKey(item.id)) {
                            _sessionShuffledOldMap[item.id] = System.nanoTime() + (0..1000000).random()
                        }
                    }

                    val stableOldPool = oldPool.sortedBy { _sessionShuffledOldMap[it.id] ?: 0L }
                    freshPool + stableOldPool
                }
                .combine(isPinnedFilterActive) { items, isPinnedActive ->
                    if (isPinnedActive) {
                        val pinnedSet = pinnedIds.value
                        items.filter { it.id in pinnedSet }
                    } else {
                        items
                    }
                }
                .combine(isFollowingFilterActive) { items, isFollowingActive ->
                    items to isFollowingActive
                }
                .combine(followRepository.getFollowedUserIdsFlow()) { (items, isFollowingActive), followedIds ->
                    if (isFollowingActive) {
                        items.filter { item ->
                            val ownerId = when (item) {
                                is ContentItem.PhotoContent -> item.photo.ownerId
                                is ContentItem.LetterContent -> item.letter.ownerId
                                is ContentItem.AudioContent -> item.audio.ownerId
                                is ContentItem.VideoContent -> item.video.ownerId
                            }
                            followedIds.contains(ownerId)
                        }
                    } else {
                        items
                    }
                }
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect { items ->
                    _allContentItems.value = UiState.Success(items)
                }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _allContentItems.value = UiState.Loading
            _stableFeedList = emptyList()
            _sessionShuffledOldMap.clear()
            _refreshTrigger.value++
        }
    }

    fun togglePinnedFilter() {
        _isPinnedFilterActive.value = !_isPinnedFilterActive.value
    }

    fun toggleFollowingFilter() {
        _isFollowingFilterActive.value = !_isFollowingFilterActive.value
    }

    fun togglePin(contentId: String, contentType: String) {
        val uid = authService.getCurrentUserId()
        if (uid.isEmpty()) return
        viewModelScope.launch {
            pinnedItemRepository.togglePin(uid, contentId, contentType)
        }
    }

    fun togglePhotoReaction(photoId: String, emoji: String) {
        val uid = authService.getCurrentUserId()
        if (uid.isEmpty()) return
        viewModelScope.launch {
            try {
                photoReactionRepository.toggleReaction(uid, photoId, emoji)
            } catch (_: Exception) { }
        }
    }

    fun toggleVideoReaction(videoId: String, emoji: String) {
        val uid = authService.getCurrentUserId()
        if (uid.isEmpty() || videoId.isEmpty()) return
        viewModelScope.launch {
            try {
                videoReactionRepository.toggleReaction(
                    userId = uid,
                    videoId = videoId,
                    emoji = emoji
                )
            } catch (_: Exception) { }
        }
    }

    fun sharePhoto(context: Context, photoUrl: String, photoId: String) {
        viewModelScope.launch {
            _shareState.value = ShareState.Processing
            try {
                val uri = photoRepository.getPhotoUriForSharing(context, photoUrl, photoId)
                _shareState.value = ShareState.Success(uri)
            } catch (e: Exception) {
                _shareState.value = ShareState.Error(e.message ?: "Hata")
            }
        }
    }

    fun shareVideo(context: Context, videoUrl: String, videoId: String) {
        viewModelScope.launch {
            _shareState.value = ShareState.Processing
            try {
                val uri = videoRepository.getVideoUriForSharing(context, videoUrl, videoId)
                _shareState.value = ShareState.Success(uri)
            } catch (e: Exception) {
                _shareState.value = ShareState.Error(e.message ?: "Hata")
            }
        }
    }

    fun shareSes(context: Context, audioUrl: String, sesId: String) {
        viewModelScope.launch {
            _shareState.value = ShareState.Processing
            try {
                val uri = sesRepository.getAudioUriForSharing(context, audioUrl, sesId)
                _shareState.value = ShareState.Success(uri)
            } catch (e: Exception) {
                _shareState.value = ShareState.Error(e.message ?: "Hata")
            }
        }
    }

    fun incrementLetterShareCount(letterId: String) {
        viewModelScope.launch {
            letterRepository.incrementShareCount(letterId)
        }
    }

    fun resetShareState() {
        _shareState.value = ShareState.Idle
    }

    private fun loadAnimationAssets() {
        val assetCollections = listOf(
            "police_assets" to policeAssets,
            "commando_assets" to commandoAssets,
            "bekci_assets" to bekciAssets
        )

        assetCollections.forEach { (coll, flow) ->
            val registration = firestore.collection(coll).addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val assets = snapshot.documents.mapNotNull { doc ->
                        val url = doc.getString("imageUrl")
                        val typeStr = doc.getString("type")?.uppercase() ?: "DRIVE"
                        val type = try { AnimationType.valueOf(typeStr) } catch(_: Exception) { AnimationType.DRIVE }
                        val hasSiren = doc.getBoolean("hasSiren") ?: false

                        url?.let { AnimationAsset(it, type, hasSiren) }
                    }
                    flow.value = assets
                }
            }
            listeners.add(registration)
        }

        viewModelScope.launch {
            val fallDef = async { fetchPackageFromFirestore(listOf("flowers", "anim_fall"), AnimationType.FALL) }
            val floatDef = async { fetchPackageFromFirestore(listOf("anim_float"), AnimationType.FLOAT) }
            val flyDef = async { fetchPackageFromFirestore(listOf("anim_fly"), AnimationType.FLY) }
            val popDef = async { fetchPackageFromFirestore(listOf("anim_pop"), AnimationType.POP) }

            _packageFall.value = fallDef.await()
            _packageFloat.value = floatDef.await()
            _packageFly.value = flyDef.await()
            _packagePop.value = popDef.await()
        }
    }

    private suspend fun fetchPackageFromFirestore(collections: List<String>, type: AnimationType): AnimationPackage? {
        val allAssets = mutableListOf<AnimationAsset>()
        var foundSoundUrl: String? = null

        val results = collections.map { coll ->
            viewModelScope.async {
                try {
                    firestore.collection(coll).get().await()
                } catch (_: Exception) {
                    null
                }
            }
        }

        results.forEach { deferred ->
            deferred.await()?.documents?.forEach { doc ->
                val img = doc.getString("imageUrl")
                val hasSiren = doc.getBoolean("hasSiren") ?: false
                if (img != null) allAssets.add(AnimationAsset(img, type, hasSiren))

                val snd = doc.getString("soundUrl")
                if (snd != null) foundSoundUrl = snd
            }
        }

        return if (allAssets.isEmpty()) null else AnimationPackage(allAssets, foundSoundUrl)
    }

    fun getRandomAnimationPackage(): AnimationPackage? {
        val packages = listOfNotNull(_packageFall.value, _packageFloat.value, _packageFly.value, _packagePop.value)
        return if (packages.isNotEmpty()) packages.random() else null
    }

    private fun listenAnimationStatus() {
        val anims = listOf("police_rain" to isPoliceAnimationActive, "commando_rain" to isCommandoAnimationActive, "bekci_rain" to isBekciAnimationActive)
        anims.forEach { (doc, flow) ->
            val registration = firestore.collection("animations").document(doc).addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    val active = snap.getBoolean("isActive") ?: false
                    flow.value = active
                    when(doc) {
                        "police_rain" -> { policeSoundUrl.value = snap.getString("soundUrl"); if(active && policeJob == null) policeJob = viewModelScope.launch { delay(15000); togglePoliceAnimation(false) } }
                        "commando_rain" -> { commandoSoundUrl.value = snap.getString("soundUrl"); if(active && commandoJob == null) commandoJob = viewModelScope.launch { delay(15000); toggleCommandoAnimation(false) } }
                        "bekci_rain" -> { bekciSoundUrl.value = snap.getString("soundUrl"); if(active && bekciJob == null) bekciJob = viewModelScope.launch { delay(15000); toggleBekciAnimation(false) } }
                    }
                }
            }
            listeners.add(registration)
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationListener?.remove()
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    fun togglePoliceAnimation(status: Boolean) { policeJob?.cancel(); if (!status) policeJob = null; viewModelScope.launch { try { firestore.collection("animations").document("police_rain").set(mapOf("isActive" to status), SetOptions.merge()) } catch (_: Exception) { } } }
    fun toggleCommandoAnimation(status: Boolean) { commandoJob?.cancel(); if (!status) commandoJob = null; viewModelScope.launch { try { firestore.collection("animations").document("commando_rain").set(mapOf("isActive" to status), SetOptions.merge()) } catch (_: Exception) { } } }
    fun toggleBekciAnimation(status: Boolean) { bekciJob?.cancel(); if (!status) bekciJob = null; viewModelScope.launch { try { firestore.collection("animations").document("bekci_rain").set(mapOf("isActive" to status), SetOptions.merge()) } catch (_: Exception) { } } }

    private fun mapToContentItems(photos: List<PhotoModel>, letters: List<LetterModel>, audios: List<SesModel>, videos: List<VideoModel>): List<ContentItem> {
        return (photos.map { ContentItem.PhotoContent(it) } +
                letters.map { ContentItem.LetterContent(it) } +
                audios.map { ContentItem.AudioContent(it) } +
                videos.map { ContentItem.VideoContent(it) })
            .filter { item ->
                when (item) {
                    is ContentItem.LetterContent -> item.letter.privacy.trim().uppercase() == "PUBLIC"
                    is ContentItem.AudioContent -> item.audio.privacy.trim().uppercase() == "PUBLIC"
                    else -> true
                }
            }
    }
}
