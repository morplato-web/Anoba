package com.neval.anoba.video.videoemoji

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.neval.anoba.video.VideoDao
import com.neval.anoba.video.VideoModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class VideoEmojiReactionRepository(
    private val firestore: FirebaseFirestore,
    private val videoDao: VideoDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "VideoEmojiReactionRepo"
    }

    private fun getUserVideoReactionRef(userId: String, videoId: String) =
        firestore.collection("userReactions").document(userId)
            .collection("video_reactions").document(videoId)

    private fun getVideoRef(videoId: String) = firestore.collection("videos").document(videoId)

    suspend fun toggleReaction(userId: String, videoId: String, emoji: String): Unit = withContext(Dispatchers.IO) {
        if (userId.isBlank() || videoId.isBlank() || emoji.isBlank()) return@withContext

        val userReactionRef = getUserVideoReactionRef(userId, videoId)
        val videoRef = getVideoRef(videoId)

        var currentEmoji: String? = null
        try {
            val userReactionSnap = userReactionRef.get().await()
            currentEmoji = if (userReactionSnap.exists()) userReactionSnap.getString("emoji") else null
        } catch (e: Exception) {
            Log.w(TAG, "Mevcut video reaksiyonu okunamadı: ${e.message}")
        }

        // -------------------------------------------------------------------------
        // ⚡ 1. ADIM: OPTIMISTIC LOCAL UPDATE (Room Veritabanı Anlık Güncelleme)
        // -------------------------------------------------------------------------
        var backupVideo: VideoModel? = null
        try {
            val localVideo = videoDao.getVideoById(videoId)
            if (localVideo != null) {
                backupVideo = localVideo
                val updatedReactions = localVideo.reactions.toMutableMap()

                if (currentEmoji == emoji) {
                    // Durum 1: Aynı emojiye tekrar basıldı -> Kaldır (-1)
                    val count = updatedReactions[emoji] ?: 1L
                    updatedReactions[emoji] = (count - 1).coerceAtLeast(0L)
                } else {
                    // Durum 2: Farklı bir emojiye geçildi -> Eski emojiyi düş (-1)
                    if (!currentEmoji.isNullOrBlank()) {
                        val oldCount = updatedReactions[currentEmoji] ?: 1L
                        updatedReactions[currentEmoji] = (oldCount - 1).coerceAtLeast(0L)
                    }
                    // Yeni emojiyi artır (+1)
                    val newCount = updatedReactions[emoji] ?: 0L
                    updatedReactions[emoji] = newCount + 1
                }

                videoDao.insertAll(listOf(localVideo.copy(reactions = updatedReactions)))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Optimistik yerel Room güncellemesi atlandı: ${e.message}")
        }

        // -------------------------------------------------------------------------
        // 🌐 2. ADIM: FIRESTORE TRANSACTION (Bulut Senkronizasyonu)
        // -------------------------------------------------------------------------
        try {
            var shouldSendNotification = false
            var targetOwnerId: String? = null

            val userDoc = firestore.collection("users").document(userId).get().await()
            val currentDisplayName = userDoc.getString("username")?.ifBlank {
                userDoc.getString("displayName") ?: "Bir kullanıcı"
            } ?: "Bir kullanıcı"
            val currentUserPhotoUrl = userDoc.getString("profileImageUrl") ?: userDoc.getString("photoUrl") ?: ""
            val currentUserBadgeType = userDoc.getString("badgeType") ?: "NONE"
            val currentUserIsOfficial = userDoc.getBoolean("isOfficial") ?: currentDisplayName.contains("anoba", ignoreCase = true)

            firestore.runTransaction { transaction ->
                val videoDoc = transaction.get(videoRef)
                targetOwnerId = videoDoc.getString("ownerId")
                val userReactionSnapshot = transaction.get(userReactionRef)
                val serverCurrentEmoji = if (userReactionSnapshot.exists()) userReactionSnapshot.getString("emoji") else null

                if (serverCurrentEmoji == emoji) {
                    transaction.delete(userReactionRef)
                    transaction.update(videoRef, "reactions.$emoji", FieldValue.increment(-1))
                } else {
                    if (!serverCurrentEmoji.isNullOrBlank()) {
                        transaction.update(videoRef, "reactions.$serverCurrentEmoji", FieldValue.increment(-1))
                    }

                    // --- TOPLULUK MODERASYONU (EŞİK KONTROLÜ) ---
                    val currentEmojiCount = (videoDoc.get("reactions.$emoji") as? Long) ?: 0L
                    val updatedCount = currentEmojiCount + 1

                    val shouldUnpublish = when (emoji) {
                        "🚫" -> updatedCount >= 5
                        "❌" -> updatedCount >= 5
                        else -> false
                    }

                    // ⭐ 1000+ VİTRİN SİSTEMİ EŞİĞİ
                    val alreadyPopular = videoDoc.getBoolean("is_popular") ?: false
                    val isPopularThresholdReached = !alreadyPopular && !listOf("🚫", "❌").contains(emoji) && updatedCount >= 10

                    if (isPopularThresholdReached) {
                        transaction.update(videoRef, "is_popular", true)

                        if (!targetOwnerId.isNullOrBlank()) {
                            val popularNotif = mapOf(
                                "type" to "POPULAR_OWNER_ALERT",
                                "targetId" to videoId,
                                "videoId" to videoId,
                                "title" to "Tebrikler! 🏆",
                                "senderName" to "Anoba",
                                "message" to "Paylaşımınız 1000+ sayfasında",
                                "body" to "Paylaşımınız 1000+ sayfasında",
                                "isRead" to false,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            val notifRef = firestore.collection("users").document(targetOwnerId!!).collection("notifications").document()
                            transaction.set(notifRef, popularNotif)
                        }

                        scope.launch {
                            try {
                                val reactors = firestore.collectionGroup("video_reactions")
                                    .whereEqualTo("videoId", videoId)
                                    .whereEqualTo("emoji", emoji)
                                    .get().await()

                                val reactorUids = reactors.documents.mapNotNull { it.getString("userId") }.distinct()

                                if (reactorUids.isNotEmpty()) {
                                    val batch = firestore.batch()
                                    val feedbackNotif = mapOf(
                                        "type" to "POPULAR_REACTOR_ALERT",
                                        "targetId" to videoId,
                                        "title" to "Başarıya ortaksınız! ✨",
                                        "senderName" to "Anoba",
                                        "message" to "Tepki verdiğiniz içerik 1000+ sayfasında",
                                        "isRead" to false,
                                        "timestamp" to FieldValue.serverTimestamp()
                                    )
                                    for (uid in reactorUids) {
                                        if (uid != targetOwnerId) {
                                            val ref = firestore.collection("users").document(uid).collection("notifications").document()
                                            batch.set(ref, feedbackNotif)
                                        }
                                    }
                                    batch.commit().await()
                                }
                            } catch (e: Exception) { Log.e(TAG, "Popular video notif error", e) }
                        }
                    }

                    if (shouldUnpublish) {
                        shouldSendNotification = false
                        transaction.update(videoRef, "is_published", false)
                        transaction.update(videoRef, "is_flagged", true)
                        transaction.update(videoRef, "flag_reason", "COMMUNITY_REPORT_$emoji")
                        transaction.update(videoRef, "flagged_at", FieldValue.serverTimestamp())

                        if (!targetOwnerId.isNullOrBlank()) {
                            val quarantineNotif = mapOf(
                                "type" to "QUARANTINE_ALERT",
                                "targetId" to videoId,
                                "videoId" to videoId,
                                "title" to "İçeriğiniz Karantinaya Alındı",
                                "senderName" to "Sistem",
                                "senderPhotoUrl" to "",
                                "message" to "Videonuz topluluk raporları nedeniyle inceleme havuzuna alınmıştır.",
                                "body" to "Videonuz topluluk raporları ($emoji) nedeniyle inceleme havuzuna alınmıştır.",
                                "isRead" to false,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            val notifRef = firestore.collection("users").document(targetOwnerId!!).collection("notifications").document()
                            transaction.set(notifRef, quarantineNotif)
                        }

                        scope.launch {
                            try {
                                val reportersSnapshot = firestore.collectionGroup("video_reactions")
                                    .whereEqualTo("videoId", videoId)
                                    .whereIn("emoji", listOf("🚫", "❌"))
                                    .get().await()

                                val reporterUids = reportersSnapshot.documents
                                    .mapNotNull { it.getString("userId") }
                                    .distinct()

                                if (reporterUids.isNotEmpty()) {
                                    val batch = firestore.batch()
                                    val feedbackNotif = mapOf(
                                        "type" to "REPORTER_QUARANTINE",
                                        "targetId" to videoId,
                                        "title" to "Geri bildiriminiz işleme alındı",
                                        "senderName" to "Anoba",
                                        "message" to "İşaretlediğiniz video karantinaya alındı.",
                                        "isRead" to false,
                                        "timestamp" to FieldValue.serverTimestamp()
                                    )

                                    for (reporterUid in reporterUids) {
                                        if (reporterUid != targetOwnerId) {
                                            val notifRef = firestore.collection("users").document(reporterUid)
                                                .collection("notifications").document()
                                            batch.set(notifRef, feedbackNotif)
                                        }
                                    }
                                    batch.commit().await()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error sending video reporter notifications", e)
                            }
                        }
                    }

                    val newReaction = VideoReaction(
                        userId = userId,
                        videoId = videoId,
                        emoji = emoji,
                        displayName = currentDisplayName,
                        userPhotoUrl = currentUserPhotoUrl,
                        userBadgeType = currentUserBadgeType,
                        isOfficial = currentUserIsOfficial
                    )
                    transaction.set(userReactionRef, newReaction)
                    transaction.update(videoRef, "reactions.$emoji", FieldValue.increment(1))
                    if (!shouldUnpublish) shouldSendNotification = true
                }
                null
            }.await()

            val ownerId = targetOwnerId
            if (shouldSendNotification && !ownerId.isNullOrBlank() && ownerId != userId) {
                val notificationData = mapOf(
                    "type" to "VIDEO_LIKE",
                    "targetId" to videoId,
                    "videoId" to videoId,
                    "title" to currentDisplayName,
                    "senderName" to currentDisplayName,
                    "senderPhotoUrl" to currentUserPhotoUrl,
                    "message" to "Videonuza tepki verdi $emoji",
                    "body" to "$currentDisplayName videonuza tepki verdi $emoji",
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(ownerId)
                    .collection("notifications").add(notificationData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "toggleReaction transaction hatası: ${e.message}", e)
            backupVideo?.let { original ->
                try {
                    videoDao.insertAll(listOf(original))
                } catch (re: Exception) {
                    Log.e(TAG, "Rollback hatası: ${re.message}")
                }
            }
        }
    }

    fun getUserVideoReactionsFlow(userId: String): Flow<Map<String, String>> {
        if (userId.isBlank()) {
            return flowOf(emptyMap())
        }
        return firestore.collection("userReactions").document(userId)
            .collection("video_reactions")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.associate { it.id to (it.getString("emoji") ?: "") }
            }
            .flowOn(Dispatchers.IO)
    }

    fun getVideoReactionUsers(videoId: String): Flow<List<VideoReactionUser>> = callbackFlow {
        if (videoId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // 🚀 DÜZELTME: .orderBy("timestamp") kaldırıldı.
        // Tek alanlı (videoId) filtreleme yapıldığı için Firestore composite index İSTEMEZ.
        // Sıralamayı aşağıda Kotlin seviyesinde yapıyoruz.
        val listener = firestore.collectionGroup("video_reactions")
            .whereEqualTo("videoId", videoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "Error fetching reaction users for video $videoId", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val usersList = snapshot.documents.mapNotNull { doc ->
                    val reaction = doc.toObject(VideoReaction::class.java)
                    if (reaction != null) {
                        Pair(
                            reaction.timestamp?.time ?: 0L,
                            VideoReactionUser(
                                userId = reaction.userId,
                                displayName = reaction.displayName.ifBlank { "Anonim" },
                                photoUrl = reaction.userPhotoUrl,
                                emoji = reaction.emoji,
                                badgeType = reaction.userBadgeType,
                                isOfficial = reaction.isOfficial
                            )
                        )
                    } else null
                }
                    .sortedByDescending { it.first } // Kotlin tarafında tarihe göre sıralama
                    .map { it.second }

                trySend(usersList)
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
}