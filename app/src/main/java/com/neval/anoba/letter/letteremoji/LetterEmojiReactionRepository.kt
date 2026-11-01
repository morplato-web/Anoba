package com.neval.anoba.letter.letteremoji

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.neval.anoba.letter.LetterDao
import com.neval.anoba.letter.LetterModel
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

class LetterEmojiReactionRepository(
    private val firestore: FirebaseFirestore,
    private val letterDao: LetterDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val lettersCollection = firestore.collection("letters")

    companion object {
        private const val TAG = "LetterEmojiReactionRepo"
    }

    private fun getUserLetterReactionRef(userId: String, letterId: String): DocumentReference =
        firestore.collection("userReactions").document(userId)
            .collection("letter_reactions").document(letterId)

    suspend fun toggleReaction(userId: String, letterId: String, emoji: String): Unit = withContext(Dispatchers.IO) {
        if (userId.isBlank() || letterId.isBlank() || emoji.isBlank()) return@withContext

        val userReactionRef = getUserLetterReactionRef(userId, letterId)

        var currentEmoji: String? = null
        try {
            val userReactionSnap = userReactionRef.get().await()
            currentEmoji = if (userReactionSnap.exists()) userReactionSnap.getString("emoji") else null
        } catch (e: Exception) {
            Log.w(TAG, "Mevcut mektup reaksiyonu okunamadı: ${e.message}")
        }

        // -------------------------------------------------------------------------
        // ⚡ 1. ADIM: OPTIMISTIC LOCAL UPDATE (Room Veritabanı Anlık Güncelleme)
        // -------------------------------------------------------------------------
        var backupLetter: LetterModel? = null
        try {
            val localLetter = letterDao.getLetterById(letterId)
            if (localLetter != null) {
                backupLetter = localLetter
                val updatedReactions = localLetter.reactions.toMutableMap()

                if (currentEmoji == emoji) {
                    val count = updatedReactions[emoji] ?: 1L
                    updatedReactions[emoji] = (count - 1L).coerceAtLeast(0L)
                } else {
                    if (!currentEmoji.isNullOrBlank()) {
                        val oldCount = updatedReactions[currentEmoji] ?: 1L
                        updatedReactions[currentEmoji] = (oldCount - 1L).coerceAtLeast(0L)
                    }
                    val newCount = updatedReactions[emoji] ?: 0L
                    updatedReactions[emoji] = newCount + 1L
                }

                letterDao.insertAll(listOf(localLetter.copy(reactions = updatedReactions)))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Optimistik yerel Room mektup güncellemesi atlandı: ${e.message}")
        }

        // -------------------------------------------------------------------------
        // 🌐 2. ADIM: FIRESTORE TRANSACTION (Bulut Senkronizasyonu)
        // -------------------------------------------------------------------------
        try {
            val letterRef: DocumentReference = lettersCollection.document(letterId)
            var isNewReaction = false
            var targetOwnerId: String? = null

            val userDoc = firestore.collection("users").document(userId).get().await()
            val currentDisplayName = userDoc.getString("username")?.ifBlank {
                userDoc.getString("displayName") ?: "Bir kullanıcı"
            } ?: "Bir kullanıcı"
            val currentUserPhotoUrl = userDoc.getString("profileImageUrl") ?: userDoc.getString("photoUrl") ?: ""
            val currentUserBadgeType = userDoc.getString("badgeType") ?: "NONE"
            val currentUserIsOfficial = userDoc.getBoolean("isOfficial") ?: currentDisplayName.contains("anoba", ignoreCase = true)

            firestore.runTransaction { transaction ->
                val letterDoc = transaction.get(letterRef)
                targetOwnerId = letterDoc.getString("ownerId")
                val userReactionSnapshot = transaction.get(userReactionRef)
                val existingEmoji = if (userReactionSnapshot.exists()) userReactionSnapshot.getString("emoji") else null

                if (existingEmoji == emoji) {
                    transaction.delete(userReactionRef)
                    transaction.update(letterRef, "reactions.$emoji", FieldValue.increment(-1))
                } else {
                    if (!existingEmoji.isNullOrBlank()) {
                        transaction.update(letterRef, "reactions.$existingEmoji", FieldValue.increment(-1))
                    } else {
                        isNewReaction = true
                    }

                    val currentEmojiCount = (letterDoc.get("reactions.$emoji") as? Long) ?: 0L
                    val updatedCount = currentEmojiCount + 1

                    val shouldUnpublish = when (emoji) {
                        "🚫" -> updatedCount >= 5
                        "❌" -> updatedCount >= 5
                        else -> false
                    }

                    val alreadyPopular = letterDoc.getBoolean("is_popular") ?: false
                    val isPopularThresholdReached = !alreadyPopular && !listOf("🚫", "❌").contains(emoji) && updatedCount >= 10

                    if (isPopularThresholdReached) {
                        transaction.update(letterRef, "is_popular", true)

                        if (!targetOwnerId.isNullOrBlank()) {
                            val popularNotif = mapOf(
                                "type" to "POPULAR_OWNER_ALERT",
                                "targetId" to letterId,
                                "letterId" to letterId,
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
                                val reactors = firestore.collectionGroup("letter_reactions")
                                    .whereEqualTo("letterId", letterId)
                                    .whereEqualTo("emoji", emoji)
                                    .get().await()

                                val reactorUids = reactors.documents.mapNotNull { it.getString("userId") }.distinct()

                                if (reactorUids.isNotEmpty()) {
                                    val batch = firestore.batch()
                                    val feedbackNotif = mapOf(
                                        "type" to "POPULAR_REACTOR_ALERT",
                                        "targetId" to letterId,
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
                            } catch (e: Exception) { Log.e(TAG, "Popular letter notif error", e) }
                        }
                    }

                    if (shouldUnpublish) {
                        isNewReaction = false
                        transaction.update(letterRef, "is_published", false)
                        transaction.update(letterRef, "is_flagged", true)
                        transaction.update(letterRef, "flag_reason", "COMMUNITY_REPORT_$emoji")
                        transaction.update(letterRef, "flagged_at", FieldValue.serverTimestamp())

                        if (!targetOwnerId.isNullOrBlank()) {
                            val quarantineNotif = mapOf(
                                "type" to "QUARANTINE_ALERT",
                                "targetId" to letterId,
                                "letterId" to letterId,
                                "title" to "İçeriğiniz Karantinaya Alındı",
                                "senderName" to "Sistem",
                                "senderPhotoUrl" to "",
                                "message" to "Mektubunuz topluluk raporları nedeniyle inceleme havuzuna alınmıştır.",
                                "body" to "Mektubunuz topluluk raporları ($emoji) nedeniyle inceleme havuzuna alınmıştır.",
                                "isRead" to false,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            val notifRef = firestore.collection("users").document(targetOwnerId!!).collection("notifications").document()
                            transaction.set(notifRef, quarantineNotif)
                        }

                        scope.launch {
                            try {
                                val reportersSnapshot = firestore.collectionGroup("letter_reactions")
                                    .whereEqualTo("letterId", letterId)
                                    .whereIn("emoji", listOf("🚫", "❌"))
                                    .get().await()

                                val reporterUids = reportersSnapshot.documents
                                    .mapNotNull { it.getString("userId") }
                                    .distinct()

                                if (reporterUids.isNotEmpty()) {
                                    val batch = firestore.batch()
                                    val feedbackNotif = mapOf(
                                        "type" to "REPORTER_QUARANTINE",
                                        "targetId" to letterId,
                                        "title" to "Geri bildiriminiz işleme alındı",
                                        "senderName" to "Anoba",
                                        "message" to "İşaretlediğiniz mektup karantinaya alındı.",
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
                                Log.e(TAG, "Error sending letter reporter notifications", e)
                            }
                        }
                    }

                    val newReaction = LetterReaction(
                        userId = userId,
                        letterId = letterId,
                        emoji = emoji,
                        displayName = currentDisplayName,
                        userPhotoUrl = currentUserPhotoUrl,
                        userBadgeType = currentUserBadgeType,
                        isOfficial = currentUserIsOfficial
                    )
                    transaction.set(userReactionRef, newReaction)
                    transaction.update(letterRef, "reactions.$emoji", FieldValue.increment(1))
                }
                null
            }.await()

            val ownerId = targetOwnerId
            if (isNewReaction && !ownerId.isNullOrBlank() && ownerId != userId) {
                val notificationData = mapOf(
                    "type" to "LETTER_LIKE",
                    "targetId" to letterId,
                    "letterId" to letterId,
                    "title" to currentDisplayName,
                    "senderName" to currentDisplayName,
                    "senderPhotoUrl" to currentUserPhotoUrl,
                    "message" to "Mektubunuza tepki verdi $emoji",
                    "body" to "$currentDisplayName mektubunuza tepki verdi $emoji",
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(ownerId)
                    .collection("notifications").add(notificationData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling letter reaction", e)
            backupLetter?.let { original ->
                try {
                    letterDao.insertAll(listOf(original))
                } catch (re: Exception) {
                    Log.e(TAG, "Rollback hatası: ${re.message}")
                }
            }
        }
    }

    fun getUserLetterReactions(userId: String): Flow<Map<String, String>> {
        if (userId.isBlank()) {
            return flowOf(emptyMap())
        }
        return firestore.collection("userReactions").document(userId)
            .collection("letter_reactions")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.associate { doc ->
                    doc.id to (doc.getString("emoji") ?: "")
                }
            }
            .flowOn(Dispatchers.IO)
    }

    fun getLetterReactionUsers(letterId: String): Flow<List<LetterReactionUser>> = callbackFlow {
        if (letterId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collectionGroup("letter_reactions")
            .whereEqualTo("letterId", letterId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "Error fetching reaction users for letter $letterId", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val usersList = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("userId") ?: return@mapNotNull null
                    val emoji = doc.getString("emoji") ?: ""
                    val displayName = doc.getString("displayName")?.ifBlank { "Anonim" } ?: "Anonim"
                    val photoUrl = doc.getString("userPhotoUrl") ?: doc.getString("photoUrl")
                    val badgeType = doc.getString("userBadgeType") ?: doc.getString("badgeType") ?: "NONE"
                    val isOfficial = doc.getBoolean("isOfficial") ?: doc.getBoolean("official") ?: false
                    val timestampMillis = doc.getDate("timestamp")?.time ?: 0L

                    LetterReactionUser(
                        userId = uid,
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