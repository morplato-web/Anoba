package com.neval.anoba.letter

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Suppress("unused")
class LetterRepository(
    private val firestore: FirebaseFirestore,
    private val storage: com.google.firebase.storage.FirebaseStorage,
    private val letterDao: LetterDao
) {
    companion object {
        private const val TAG = "LetterRepository"
    }

    private val lettersCollection = firestore.collection("letters")
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getLetters(): Flow<List<LetterModel>> {
        return lettersCollection
            .whereEqualTo("is_published", true)
            .orderBy("publish_timestamp", Query.Direction.DESCENDING)
            .dataObjects<LetterModel>()
            .onEach { letters ->
                letterDao.resetPublishedLetters()
                letterDao.insertAll(letters)
            }
            .flowOn(Dispatchers.IO)
    }

    fun getLettersOffline(): Flow<List<LetterModel>> = letterDao.getAllPublishedLetters()

    fun getQuarantineLetters(): Flow<List<LetterModel>> {
        return lettersCollection
            .whereEqualTo("is_flagged", true)
            .dataObjects<LetterModel>()
            .catch { e ->
                Log.e(TAG, "Error getting quarantine letters", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    fun getPopularLetters(): Flow<List<LetterModel>> {
        return lettersCollection
            .whereEqualTo("is_popular", true)
            .dataObjects<LetterModel>()
            .catch { e ->
                Log.e(TAG, "Error getting popular letters", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    fun getLetterById(letterId: String): Flow<LetterModel?> {
        return lettersCollection.document(letterId).dataObjects()
    }

    suspend fun getLetter(letterId: String): LetterModel? = withContext(Dispatchers.IO) {
        return@withContext try {
            lettersCollection.document(letterId).get().await().toObject<LetterModel>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching single letter: $letterId", e)
            null
        }
    }

    fun getUserLetters(userId: String): Flow<List<LetterModel>> {
        return lettersCollection
            .whereEqualTo("ownerId", userId)
            .dataObjects<LetterModel>()
            .onEach { letters ->
                letterDao.resetPublishedLetters()
                letterDao.insertAll(letters)
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun saveLetter(
        ownerId: String,
        username: String,
        userProfileImageUrl: String?,
        badgeType: String?,
        content: String,
        privacy: String,
        recipientId: String?,
        recipientUsername: String?,
        unlockTimestamp: Long? = null,
        isPublished: Boolean,
        pseudonym: String? = null,
        isAskAnoba: Boolean = false,
        audioFile: java.io.File? = null,
        audioDuration: Long? = null
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val newDocRef = lettersCollection.document()

            var uploadedAudioUrl: String? = null
            if (audioFile != null) {
                // 🚀 DÜZELTME: Standart "sesler" dizini kullanıldı
                val storageRef = storage.reference.child("sesler/letter_audio_${newDocRef.id}.m4a")
                storageRef.putFile(android.net.Uri.fromFile(audioFile)).await()
                uploadedAudioUrl = storageRef.downloadUrl.await().toString()
            }

            val isAudio = uploadedAudioUrl != null
            val senderUsername = if (isAskAnoba && !pseudonym.isNullOrBlank()) pseudonym else username.ifBlank { "Bir kullanıcı" }

            val letter = LetterModel(
                id = newDocRef.id,
                ownerId = ownerId,
                username = username,
                userProfileImageUrl = userProfileImageUrl,
                content = content,
                privacy = privacy,
                recipientId = recipientId,
                recipientUsername = recipientUsername,
                unlockTimestamp = unlockTimestamp,
                isPublished = isPublished,
                viewCount = 0,
                anobaResponse = null,
                isOfficial = username.contains("anoba", ignoreCase = true),
                countryCode = java.util.Locale.getDefault().country,
                userBadgeType = badgeType,
                pseudonym = pseudonym,
                isAskAnoba = isAskAnoba,
                audioUrl = uploadedAudioUrl,
                audioDuration = audioDuration
            )
            newDocRef.set(letter).await()

            val updates = mutableMapOf<String, Any>(
                "timestamp" to FieldValue.serverTimestamp()
            )
            if (isPublished) {
                updates["publish_timestamp"] = FieldValue.serverTimestamp()
            }
            newDocRef.update(updates).await()

            letterDao.insertLetter(letter)

            // 1. MÜHÜRLÜ (KİŞİYE ÖZEL) MEKTUP BİLDİRİMİ
            if (privacy == "SEALED" && !recipientId.isNullOrBlank() && recipientId != ownerId) {
                val notifMsg = if (isAudio) {
                    "$senderUsername sana özel mühürlü bir sesli mektup gönderdi."
                } else {
                    "$senderUsername sana özel mühürlü bir mektup gönderdi."
                }

                val notificationData = mapOf(
                    "type" to "SEALED_LETTER",
                    "targetId" to newDocRef.id,
                    "letterId" to newDocRef.id,
                    "title" to senderUsername,
                    "senderName" to senderUsername,
                    "senderPhotoUrl" to (userProfileImageUrl ?: ""),
                    "message" to notifMsg,
                    "body" to notifMsg,
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(recipientId)
                    .collection("notifications").add(notificationData)
            }

            // 2. ANOBA ABLASI DERTLERİ İÇİN ADMİNE BİLDİRİM
            if (isAskAnoba) {
                val adminNotifTitle = if (isAudio) "Yeni Dert (Sesli Mektup) 🎙️" else "Yeni Dert Mektubu ✉️"
                val adminNotificationData = mapOf(
                    "type" to "ANOBA_NEW_REQUEST",
                    "targetId" to newDocRef.id,
                    "letterId" to newDocRef.id,
                    "title" to adminNotifTitle,
                    "senderName" to senderUsername,
                    "senderPhotoUrl" to (userProfileImageUrl ?: ""),
                    "message" to "$senderUsername Anoba Ablası'na bir mektup bıraktı.",
                    "body" to "$senderUsername Anoba Ablası'na bir mektup bıraktı.",
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("admin_notifications").add(adminNotificationData)
            }

            newDocRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error saving letter", e)
            null
        }
    }

    suspend fun updateLetter(letterId: String, updates: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            lettersCollection.document(letterId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating letter: $letterId", e)
            false
        }
    }

    suspend fun unflagLetter(letterId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            lettersCollection.document(letterId).update(
                mapOf(
                    "is_flagged" to false,
                    "is_published" to true,
                    "flag_reason" to null,
                    "flagged_at" to null
                )
            ).await()

            // 🟢🛡️✅ BİLDİRİM: İşaretleyenlere "Onaylandı" bildirimi gönder
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
                            "type" to "REPORTER_UNFLAG",
                            "targetId" to letterId,
                            "title" to "İnceleme tamamlandı",
                            "senderName" to "Anoba",
                            "message" to "İçerik kurallara uygun bulundu ve yayına devam ediyor.",
                            "isRead" to false,
                            "timestamp" to FieldValue.serverTimestamp()
                        )

                        for (reporterUid in reporterUids) {
                            val notifRef = firestore.collection("users").document(reporterUid)
                                .collection("notifications").document()
                            batch.set(notifRef, feedbackNotif)
                        }
                        batch.commit().await()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending unflag notifications to reporters", e)
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unflagging letter: $letterId", e)
            false
        }
    }

    suspend fun togglePublishState(letterId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val docRef = lettersCollection.document(letterId)
            val snapshot = docRef.get().await()
            val currentState = snapshot.getBoolean("is_published") ?: false

            if (!currentState) {
                docRef.update(mapOf(
                    "is_published" to true,
                    "publish_timestamp" to FieldValue.serverTimestamp()
                )).await()
            } else {
                docRef.update(mapOf(
                    "is_published" to false,
                    "publish_timestamp" to FieldValue.delete()
                )).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling publish state for letter: $letterId", e)
            throw e
        }
    }

    suspend fun deleteLetter(letterId: String): Boolean = withContext(Dispatchers.IO) {
        if (letterId.isBlank()) return@withContext false
        return@withContext try {
            val letterDoc = lettersCollection.document(letterId).get().await()
            val audioUrl = letterDoc.getString("audioUrl")

            // 🟢🛡️✅ BİLDİRİM: İşaretleyenlere "Silindi" bildirimi gönder
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
                            "type" to "REPORTER_DELETED",
                            "targetId" to letterId,
                            "title" to "Geri bildiriminiz sonuçlandı",
                            "senderName" to "Anoba",
                            "message" to "İşaretlediğiniz içerik sistemden kaldırıldı.",
                            "isRead" to false,
                            "timestamp" to FieldValue.serverTimestamp()
                        )

                        for (reporterUid in reporterUids) {
                            val notifRef = firestore.collection("users").document(reporterUid)
                                .collection("notifications").document()
                            batch.set(notifRef, feedbackNotif)
                        }
                        batch.commit().await()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending delete notifications to reporters", e)
                }
            }

            lettersCollection.document(letterId).delete().await()
            letterDao.deleteLetterById(letterId)

            if (!audioUrl.isNullOrEmpty()) {
                try {
                    storage.getReferenceFromUrl(audioUrl).delete().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Storage delete fail for $audioUrl", e)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting letter: $letterId", e)
            false
        }
    }

    suspend fun deleteAllUserLetters(userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val snapshot = lettersCollection.whereEqualTo("ownerId", userId).get().await()
            for (doc in snapshot.documents) {
                val docId = doc.id
                val audioUrl = doc.getString("audioUrl")
                doc.reference.delete().await()
                letterDao.deleteLetterById(docId)

                if (!audioUrl.isNullOrEmpty()) {
                    try {
                        storage.getReferenceFromUrl(audioUrl).delete().await()
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all letters for user $userId", e)
        }
    }

    suspend fun incrementLetterViewCount(letterId: String): Unit = withContext(Dispatchers.IO) {
        if (letterId.isBlank()) return@withContext
        try {
            val letterRef = lettersCollection.document(letterId)
            letterRef.update("viewCount", FieldValue.increment(1)).await()
            letterDao.incrementLocalViewCount(letterId)
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing view count for letter: $letterId", e)
        }
    }

    suspend fun incrementShareCount(letterId: String): Unit = withContext(Dispatchers.IO) {
        try {
            lettersCollection.document(letterId)
                .update("shareCount", FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing share count for letter $letterId", e)
        }
    }

    fun getPendingAnobaRequests(): Flow<List<LetterModel>> = callbackFlow {
        val listener = lettersCollection
            .whereEqualTo("is_ask_anoba", true)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e(TAG, "Error listening pending letters", err)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toObject<LetterModel>() }
                    ?.filter { it.anobaResponse.isNullOrBlank() }
                    ?.sortedByDescending { it.publishTimestamp?.time ?: it.createdAt?.time ?: 0L }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    fun getAnsweredAnobaRequests(): Flow<List<LetterModel>> = callbackFlow {
        val listener = lettersCollection
            .whereEqualTo("is_ask_anoba", true)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e(TAG, "Error listening answered letters", err)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toObject<LetterModel>() }
                    ?.filter { !it.anobaResponse.isNullOrBlank() }
                    ?.sortedByDescending { it.publishTimestamp?.time ?: it.createdAt?.time ?: 0L }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    suspend fun sendAnobaResponse(letterId: String, responseText: String, isContentHidden: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val docRef = lettersCollection.document(letterId)
            val letterSnapshot = docRef.get().await()
            val currentPrivacy = letterSnapshot.getString("privacy") ?: "PUBLIC"
            val targetOwnerId = letterSnapshot.getString("ownerId") ?: ""

            val updates = mutableMapOf<String, Any>(
                "anobaResponse" to responseText,
                "is_content_hidden" to isContentHidden
            )

            if (currentPrivacy == "PUBLIC") {
                updates["is_published"] = true
                updates["publish_timestamp"] = FieldValue.serverTimestamp()
            }

            docRef.update(updates).await()

            if (targetOwnerId.isNotBlank()) {
                val notificationData = mapOf(
                    "type" to "ANOBA_RESPONSE",
                    "targetId" to letterId,
                    "letterId" to letterId,
                    "title" to "Anoba Ablası 🌙",
                    "senderName" to "Anoba Ablası",
                    "message" to "Mektubun yanıtlandı! Cevabı okumak için dokun ✨",
                    "body" to "Mektubun yanıtlandı! Cevabı okumak için dokun ✨",
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(targetOwnerId)
                    .collection("notifications").add(notificationData)
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending Anoba response to letter: $letterId", e)
            false
        }
    }

    suspend fun markLetterNotificationsAsRead(userId: String, letterId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || letterId.isBlank()) return@withContext
        try {
            val querySnapshot = firestore.collection("users").document(userId)
                .collection("notifications")
                .whereEqualTo("letterId", letterId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            for (doc in querySnapshot.documents) {
                doc.reference.update("isRead", true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking letter notifications as read", e)
        }
    }

    suspend fun markAdminNotificationAsRead(letterId: String) = withContext(Dispatchers.IO) {
        if (letterId.isBlank()) return@withContext
        try {
            val querySnapshot = firestore.collection("admin_notifications")
                .whereEqualTo("letterId", letterId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            for (doc in querySnapshot.documents) {
                doc.reference.update("isRead", true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking admin notification as read", e)
        }
    }
}
