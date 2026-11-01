package com.neval.anoba.ses.sesemoji

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.neval.anoba.ses.SesDao
import com.neval.anoba.ses.SesModel
import com.neval.anoba.ses.SesRepository
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

class SesEmojiReactionRepository(
    private val firestore: FirebaseFirestore,
    private val sesRepository: SesRepository,
    private val sesDao: SesDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "SesEmojiReactionRepo"
    }

    private fun getUserSesReactionRef(userId: String, sesId: String) =
        firestore.collection("userReactions").document(userId)
            .collection("ses_reactions").document(sesId)

    private fun getSesRef(sesId: String) = firestore.collection("sesler").document(sesId)

    suspend fun toggleReaction(
        userId: String,
        sesId: String,
        emoji: String
    ): Unit = withContext(Dispatchers.IO) {
        if (userId.isBlank() || sesId.isBlank() || emoji.isBlank()) {
            Log.w(TAG, "toggleReaction: Geçersiz parametreler - userId: $userId, sesId: $sesId, emoji: $emoji")
            return@withContext
        }

        val userReactionRef = getUserSesReactionRef(userId, sesId)
        val sesRef = getSesRef(sesId)

        var currentEmoji: String? = null
        try {
            val userReactionSnap = userReactionRef.get().await()
            currentEmoji = if (userReactionSnap.exists()) userReactionSnap.getString("emoji") else null
        } catch (e: Exception) {
            Log.w(TAG, "Mevcut ses reaksiyonu okunamadı: ${e.message}")
        }

        // -------------------------------------------------------------------------
        // ⚡ 1. ADIM: OPTIMISTIC LOCAL UPDATE (Room Veritabanı Anlık Güncelleme)
        // -------------------------------------------------------------------------
        var backupSes: SesModel? = null
        try {
            val localSes = sesDao.getSesById(sesId)
            if (localSes != null) {
                backupSes = localSes
                val updatedReactions = localSes.reactions.toMutableMap()

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

                sesDao.insertAll(listOf(localSes.copy(reactions = updatedReactions)))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Optimistik yerel Room ses güncellemesi atlandı: ${e.message}")
        }

        // -------------------------------------------------------------------------
        // 🌐 2. ADIM: FIRESTORE TRANSACTION (Bulut Senkronizasyonu)
        // -------------------------------------------------------------------------
        try {
            var shouldNotify = false

            // Kullanıcı bilgilerini önceden alalım (Denormalizasyon için)
            val userDoc = firestore.collection("users").document(userId).get().await()
            val currentDisplayName = userDoc.getString("username")?.ifBlank {
                userDoc.getString("displayName") ?: "Bir kullanıcı"
            } ?: "Bir kullanıcı"
            val currentUserPhotoUrl = userDoc.getString("profileImageUrl") ?: userDoc.getString("photoUrl") ?: ""
            val currentUserBadgeType = userDoc.getString("badgeType") ?: "NONE"
            val currentUserIsOfficial = userDoc.getBoolean("isOfficial") ?: currentDisplayName.contains("anoba", ignoreCase = true)

            firestore.runTransaction { transaction ->
                val sesDoc = transaction.get(sesRef)
                val userReactionSnapshot = transaction.get(userReactionRef)
                val currentEmoji = if (userReactionSnapshot.exists()) userReactionSnapshot.getString("emoji") else null

                if (currentEmoji == emoji) {
                    transaction.delete(userReactionRef)
                    transaction.set(sesRef, mapOf("reactions" to mapOf(emoji to FieldValue.increment(-1))), SetOptions.merge())
                } else {
                    if (!currentEmoji.isNullOrBlank()) {
                        transaction.set(sesRef, mapOf("reactions" to mapOf(currentEmoji to FieldValue.increment(-1))), SetOptions.merge())
                    }

                    // --- TOPLULUK MODERASYONU (EŞİK KONTROLÜ) ---
                    val currentEmojiCount = (sesDoc.get("reactions.$emoji") as? Long) ?: 0L
                    val updatedCount = currentEmojiCount + 1

                    val shouldUnpublish = when (emoji) {
                        "🚫" -> updatedCount >= 5
                        "❌" -> updatedCount >= 5
                        else -> false
                    }

                    // ⭐ 1000+ VİTRİN SİSTEMİ EŞİĞİ (Test için 10, orijinal 1000)
                    val alreadyPopular = sesDoc.getBoolean("is_popular") ?: false
                    val isPopularThresholdReached = !alreadyPopular && !listOf("🚫", "❌").contains(emoji) && updatedCount >= 10

                    if (isPopularThresholdReached) {
                        transaction.update(sesRef, "is_popular", true)

                        val ownerId = sesDoc.getString("ownerId")
                        if (!ownerId.isNullOrBlank()) {
                            val popularNotif = mapOf(
                                "type" to "POPULAR_OWNER_ALERT",
                                "targetId" to sesId,
                                "sesId" to sesId,
                                "title" to "Tebrikler! 🏆",
                                "senderName" to "Anoba",
                                "message" to "Paylaşımınız 1000+ sayfasında",
                                "body" to "Paylaşımınız 1000+ sayfasında",
                                "isRead" to false,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            val notifRef = firestore.collection("users").document(ownerId).collection("notifications").document()
                            transaction.set(notifRef, popularNotif)
                        }

                        scope.launch {
                            try {
                                val reactors = firestore.collectionGroup("ses_reactions")
                                    .whereEqualTo("sesId", sesId)
                                    .whereEqualTo("emoji", emoji)
                                    .get().await()

                                val reactorUids = reactors.documents.mapNotNull { it.getString("userId") }.distinct()

                                if (reactorUids.isNotEmpty()) {
                                    val batch = firestore.batch()
                                    val feedbackNotif = mapOf(
                                        "type" to "POPULAR_REACTOR_ALERT",
                                        "targetId" to sesId,
                                        "title" to "Başarıya ortaksınız! ✨",
                                        "senderName" to "Anoba",
                                        "message" to "Tepki verdiğiniz içerik 1000+ sayfasında",
                                        "isRead" to false,
                                        "timestamp" to FieldValue.serverTimestamp()
                                    )
                                    for (uid in reactorUids) {
                                        if (uid != ownerId) {
                                            val ref = firestore.collection("users").document(uid).collection("notifications").document()
                                            batch.set(ref, feedbackNotif)
                                        }
                                    }
                                    batch.commit().await()
                                }
                            } catch (e: Exception) { Log.e(TAG, "Popular ses notif error", e) }
                        }
                    }

                    if (shouldUnpublish) {
                        shouldNotify = false // 🚀 KRİTİK: Karantina varsa normal "beğeni" bildirimi atılmasın
                        transaction.update(sesRef, "is_published", false)
                        transaction.update(sesRef, "is_flagged", true)
                        transaction.update(sesRef, "flag_reason", "COMMUNITY_REPORT_$emoji")
                        transaction.update(sesRef, "flagged_at", FieldValue.serverTimestamp())

                        // 🟢🛡️✅ BİLDİRİM 1: İçerik sahibine karantina bildirimi gönder (Transaction İçinde)
                        val ownerId = sesDoc.getString("ownerId")
                        if (!ownerId.isNullOrBlank()) {
                            val quarantineNotif = mapOf(
                                "type" to "QUARANTINE_ALERT",
                                "targetId" to sesId,
                                "sesId" to sesId,
                                "title" to "İçeriğiniz Karantinaya Alındı",
                                "senderName" to "Sistem",
                                "senderPhotoUrl" to "",
                                "message" to "Sesli paylaşımınız topluluk raporları nedeniyle inceleme havuzuna alınmıştır.",
                                "body" to "Sesli paylaşımınız topluluk raporları ($emoji) nedeniyle inceleme havuzuna alınmıştır.",
                                "isRead" to false,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            val notifRef = firestore.collection("users").document(ownerId).collection("notifications").document()
                            transaction.set(notifRef, quarantineNotif)
                        }

                        // 🟢🛡️✅ BİLDİRİM 2: İşaretleyen (🚫 veya ❌) tüm kullanıcılara geri bildirim gönder
                        scope.launch {
                            try {
                                val reportersSnapshot = firestore.collectionGroup("ses_reactions")
                                    .whereEqualTo("sesId", sesId)
                                    .whereIn("emoji", listOf("🚫", "❌"))
                                    .get().await()

                                val reporterUids = reportersSnapshot.documents
                                    .mapNotNull { it.getString("userId") }
                                    .distinct()

                                if (reporterUids.isNotEmpty()) {
                                    val batch = firestore.batch()
                                    val feedbackNotif = mapOf(
                                        "type" to "REPORTER_QUARANTINE",
                                        "targetId" to sesId,
                                        "title" to "Geri bildiriminiz işleme alındı",
                                        "senderName" to "Anoba",
                                        "message" to "İşaretlediğiniz ses kaydı karantinaya alındı.",
                                        "isRead" to false,
                                        "timestamp" to FieldValue.serverTimestamp()
                                    )

                                    for (reporterUid in reporterUids) {
                                        if (reporterUid != ownerId) {
                                            val notifRef = firestore.collection("users").document(reporterUid)
                                                .collection("notifications").document()
                                            batch.set(notifRef, feedbackNotif)
                                        }
                                    }
                                    batch.commit().await()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error sending ses reporter notifications", e)
                            }
                        }
                    }
                    // --------------------------------------------

                    val newReaction = SesReaction(
                        userId = userId,
                        sesId = sesId,
                        emoji = emoji,
                        displayName = currentDisplayName,
                        userPhotoUrl = currentUserPhotoUrl,
                        userBadgeType = currentUserBadgeType,
                        isOfficial = currentUserIsOfficial
                    )
                    transaction.set(userReactionRef, newReaction)
                    transaction.set(sesRef, mapOf("reactions" to mapOf(emoji to FieldValue.increment(1))), SetOptions.merge())
                    if (!shouldUnpublish) shouldNotify = true
                }
                null
            }.await()

            if (shouldNotify) {
                sesRepository.sendSesNotification(
                    sesId = sesId,
                    senderUsername = currentDisplayName,
                    senderPhotoUrl = currentUserPhotoUrl,
                    notifType = "SES_LIKE",
                    messageText = "Ses kaydınıza tepki verdi $emoji"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "toggleReaction transaction hatası: ${e.message}", e)
            backupSes?.let { original ->
                try {
                    sesDao.insertAll(listOf(original))
                } catch (re: Exception) {
                    Log.e(TAG, "Rollback hatası: ${re.message}")
                }
            }
        }
    }

    fun getUserSesReactionsFlow(userId: String): Flow<Map<String, String>> {
        if (userId.isBlank()) {
            return flowOf(emptyMap())
        }
        return firestore.collection("userReactions").document(userId)
            .collection("ses_reactions")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.associate { it.id to (it.getString("emoji") ?: "") }
            }
            .flowOn(Dispatchers.IO)
    }

    fun getSesReactionUsers(sesId: String): Flow<List<ReactionUserData>> = callbackFlow {
        if (sesId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collectionGroup("ses_reactions")
            .whereEqualTo("sesId", sesId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "Error fetching reaction users for ses $sesId", error)
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

                    ReactionUserData(
                        uid = userId,
                        displayName = displayName,
                        photoUrl = photoUrl,
                        badgeType = badgeType,
                        isOfficial = isOfficial,
                        emoji = emoji
                    ) to timestampMillis
                }
                .sortedByDescending { it.second }
                .map { it.first }

                trySend(usersList)
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
}