package com.neval.anoba.photo.photoemoji

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.neval.anoba.photo.PhotoDao
import com.neval.anoba.photo.PhotoModel
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

class PhotoEmojiReactionRepository(
    private val firestore: FirebaseFirestore,
    private val photoDao: PhotoDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "PhotoEmojiReactionRepo"
    }

    private fun getUserPhotoReactionRef(userId: String, photoId: String) =
        firestore.collection("userReactions").document(userId)
            .collection("photo_reactions").document(photoId)

    private fun getPhotoRef(photoId: String) = firestore.collection("photos").document(photoId)

    suspend fun toggleReaction(userId: String, photoId: String, emoji: String): Unit = withContext(Dispatchers.IO) {
        if (userId.isBlank() || photoId.isBlank() || emoji.isBlank()) return@withContext

        val userReactionRef = getUserPhotoReactionRef(userId, photoId)
        val photoRef = getPhotoRef(photoId)

        var currentEmoji: String? = null
        try {
            val userReactionSnap = userReactionRef.get().await()
            currentEmoji = if (userReactionSnap.exists()) userReactionSnap.getString("emoji") else null
        } catch (e: Exception) {
            Log.w(TAG, "Mevcut foto reaksiyonu okunamadı: ${e.message}")
        }

        // -------------------------------------------------------------------------
        // ⚡ 1. ADIM: OPTIMISTIC LOCAL UPDATE (Room Veritabanı Anlık Güncelleme)
        // -------------------------------------------------------------------------
        var backupPhoto: PhotoModel? = null
        try {
            val localPhoto = photoDao.getPhotoById(photoId)
            if (localPhoto != null) {
                backupPhoto = localPhoto
                val updatedReactions = localPhoto.reactions.toMutableMap()

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

                photoDao.insertAll(listOf(localPhoto.copy(reactions = updatedReactions)))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Optimistik yerel Room foto güncellemesi atlandı: ${e.message}")
        }

        // -------------------------------------------------------------------------
        // 🌐 2. ADIM: FIRESTORE TRANSACTION (Bulut Senkronizasyonu)
        // -------------------------------------------------------------------------
        try {
            var isNewReaction = false
            var targetOwnerId: String? = null

            // Kullanıcı bilgilerini önceden alalım (Denormalizasyon için)
            val userDoc = firestore.collection("users").document(userId).get().await()
            val currentDisplayName = userDoc.getString("username")?.ifBlank {
                userDoc.getString("displayName") ?: "Bir kullanıcı"
            } ?: "Bir kullanıcı"
            val currentUserPhotoUrl = userDoc.getString("profileImageUrl") ?: userDoc.getString("photoUrl") ?: ""
            val currentUserBadgeType = userDoc.getString("badgeType") ?: "NONE"
            val currentUserIsOfficial = userDoc.getBoolean("isOfficial") ?: currentDisplayName.contains("anoba", ignoreCase = true)

            firestore.runTransaction { transaction ->
                val photoDoc = transaction.get(photoRef)
                targetOwnerId = photoDoc.getString("ownerId")
                val userReactionSnapshot = transaction.get(userReactionRef)
                val serverCurrentEmoji = if (userReactionSnapshot.exists()) userReactionSnapshot.getString("emoji") else null

                if (serverCurrentEmoji == emoji) {
                    transaction.delete(userReactionRef)
                    transaction.update(photoRef, "reactions.$emoji", FieldValue.increment(-1))
                } else {
                    if (!serverCurrentEmoji.isNullOrBlank()) {
                        transaction.update(photoRef, "reactions.$serverCurrentEmoji", FieldValue.increment(-1))
                    } else {
                        isNewReaction = true
                    }
                    
                    // --- TOPLULUK MODERASYONU (EŞİK KONTROLÜ) ---
                    val currentEmojiCount = (photoDoc.get("reactions.$emoji") as? Long) ?: 0L
                    val updatedCount = currentEmojiCount + 1
                    
                    val shouldUnpublish = when (emoji) {
                        "🚫" -> updatedCount >= 5
                        "❌" -> updatedCount >= 5
                        else -> false
                    }

                    // ⭐ 1000+ VİTRİN SİSTEMİ EŞİĞİ (Test için 10, orijinal 1000)
                    // 🚀 DÜZELTME: !alreadyPopular kilidi eklendi (Bildirimler sadece 1 kez gider)
                    val alreadyPopular = photoDoc.getBoolean("is_popular") ?: false
                    val isPopularThresholdReached = !alreadyPopular && !listOf("🚫", "❌").contains(emoji) && updatedCount >= 10

                    if (isPopularThresholdReached) {
                        transaction.update(photoRef, "is_popular", true)

                        // 🏆 BİLDİRİM 1: İçerik sahibine tebrik bildirimi (Transaction İçinde)
                        if (!targetOwnerId.isNullOrBlank()) {
                            val popularNotif = mapOf(
                                "type" to "POPULAR_OWNER_ALERT",
                                "targetId" to photoId,
                                "photoId" to photoId,
                                "title" to "Anoba",
                                "senderName" to "Anoba",
                                "message" to "", // Boş bırakıyoruz, UI dille dolduracak
                                "body" to "",
                                "isRead" to false,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            val notifRef = firestore.collection("users").document(targetOwnerId!!).collection("notifications").document()
                            transaction.set(notifRef, popularNotif)
                        }

                        // 🏆 BİLDİRİM 2: Bu emojiyi bırakanlara geri bildirim gönder
                        scope.launch {
                            try {
                                val reactors = firestore.collectionGroup("photo_reactions")
                                    .whereEqualTo("photoId", photoId)
                                    .whereEqualTo("emoji", emoji)
                                    .get().await()

                                val reactorUids = reactors.documents
                                    .mapNotNull { it.getString("userId") }
                                    .distinct()

                                if (reactorUids.isNotEmpty()) {
                                    val batch = firestore.batch()
                                    val feedbackNotif = mapOf(
                                        "type" to "POPULAR_REACTOR_ALERT",
                                        "targetId" to photoId,
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
                            } catch (e: Exception) { Log.e(TAG, "Popular notif error", e) }
                        }
                    }

                    if (shouldUnpublish) {
                        isNewReaction = false // 🚀 KRİTİK: Karantina varsa normal "beğeni" bildirimi atılmasın
                        transaction.update(photoRef, "is_published", false)
                        transaction.update(photoRef, "is_flagged", true)
                        transaction.update(photoRef, "flag_reason", "COMMUNITY_REPORT_$emoji")
                        transaction.update(photoRef, "flagged_at", FieldValue.serverTimestamp())

                        // 🟢🛡️✅ BİLDİRİM 1: İçerik sahibine karantina bildirimi gönder (Transaction İçinde)
                        if (!targetOwnerId.isNullOrBlank()) {
                            val quarantineNotif = mapOf(
                                "type" to "QUARANTINE_ALERT",
                                "targetId" to photoId,
                                "photoId" to photoId,
                                "title" to "İçeriğiniz Karantinaya Alındı",
                                "senderName" to "Sistem",
                                "senderPhotoUrl" to "",
                                "message" to "Fotoğrafınız topluluk raporları nedeniyle inceleme havuzuna alınmıştır.",
                                "body" to "Fotoğrafınız topluluk raporları ($emoji) nedeniyle inceleme havuzuna alınmıştır.",
                                "isRead" to false,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            val notifRef = firestore.collection("users").document(targetOwnerId!!).collection("notifications").document()
                            transaction.set(notifRef, quarantineNotif)
                        }

                        // 🟢🛡️✅ BİLDİRİM 2: İşaretleyen (🚫 veya ❌) tüm kullanıcılara geri bildirim gönder
                        // Transaction dışında, içerik karantinaya girdiği kesinleştiğinde tetikleyelim.
                        scope.launch(Dispatchers.IO) {
                            try {
                                val reportersSnapshot = firestore.collectionGroup("photo_reactions")
                                    .whereEqualTo("photoId", photoId)
                                    .whereIn("emoji", listOf("🚫", "❌"))
                                    .get().await()

                                val reporterUids = reportersSnapshot.documents
                                    .mapNotNull { it.getString("userId") }
                                    .distinct()

                                if (reporterUids.isNotEmpty()) {
                                    val batch = firestore.batch()
                                    val feedbackNotif = mapOf(
                                        "type" to "REPORTER_QUARANTINE",
                                        "targetId" to photoId,
                                        "title" to "Geri bildiriminiz işleme alındı",
                                        "senderName" to "Anoba",
                                        "message" to "İşaretlediğiniz fotoğraf karantinaya alındı.",
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
                                Log.e(TAG, "Error sending reporter notifications", e)
                            }
                        }
                    }
                    // --------------------------------------------

                    val newReaction = PhotoReaction(
                        userId = userId,
                        photoId = photoId,
                        emoji = emoji,
                        displayName = currentDisplayName,
                        userPhotoUrl = currentUserPhotoUrl,
                        userBadgeType = currentUserBadgeType,
                        isOfficial = currentUserIsOfficial
                        // timestamp zaten @ServerTimestamp ile nesne içinde yönetiliyor
                    )
                    transaction.set(userReactionRef, newReaction)
                    transaction.update(photoRef, "reactions.$emoji", FieldValue.increment(1))
                }
                null
            }.await()

            val ownerId = targetOwnerId
            if (isNewReaction && !ownerId.isNullOrBlank() && ownerId != userId) {
                val notificationData = mapOf(
                    "type" to "PHOTO_LIKE",
                    "targetId" to photoId,
                    "photoId" to photoId,
                    "title" to currentDisplayName,
                    "senderName" to currentDisplayName,
                    "senderPhotoUrl" to currentUserPhotoUrl,
                    "message" to "Fotoğrafınıza tepki verdi $emoji",
                    "body" to "$currentDisplayName fotoğrafınıza tepki verdi $emoji",
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(ownerId)
                    .collection("notifications").add(notificationData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling photo reaction", e)
            backupPhoto?.let { original ->
                try {
                    photoDao.insertAll(listOf(original))
                } catch (re: Exception) {
                    Log.e(TAG, "Rollback hatası: ${re.message}")
                }
            }
        }
    }

    fun getUserPhotoReactionsFlow(userId: String): Flow<Map<String, String>> {
        if (userId.isBlank()) {
            return flowOf(emptyMap())
        }
        return firestore.collection("userReactions").document(userId)
            .collection("photo_reactions")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.associate { it.id to (it.getString("emoji") ?: "") }
            }
            .flowOn(Dispatchers.IO)
    }

    fun getPhotoReactionUsers(photoId: String): Flow<List<PhotoReactionUser>> = callbackFlow {
        if (photoId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collectionGroup("photo_reactions")
            .whereEqualTo("photoId", photoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "Error fetching reaction users for photo $photoId", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val usersList = snapshot.documents.mapNotNull { doc ->
                    val userId = doc.getString("userId") ?: return@mapNotNull null
                    val emoji = doc.getString("emoji") ?: ""
                    val displayName = doc.getString("displayName")?.ifBlank { "Anonim" } ?: "Anonim"
                    val photoUrl = doc.getString("userPhotoUrl") ?: doc.getString("photoUrl")
                    val badgeType = doc.getString("userBadgeType") ?: doc.getString("badgeType") ?: "NONE"
                    val isOfficial = doc.getBoolean("isOfficial") ?: doc.getBoolean("official") ?: false
                    val timestampMillis = doc.getDate("timestamp")?.time ?: 0L

                    PhotoReactionUser(
                        userId = userId,
                        displayName = displayName,
                        photoUrl = photoUrl,
                        emoji = emoji,
                        badgeType = badgeType,
                        isOfficial = isOfficial
                    ) to timestampMillis
                }
                .sortedByDescending { it.second }
                .map { it.first }

                trySend(usersList)
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
}