@file:Suppress("RedundantExplicitType", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package com.neval.anoba.ses

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

class SesRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val sesDao: SesDao
) {
      private val sesCollection: CollectionReference = firestore.collection("sesler")
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getSesler(): Flow<List<SesModel>> {
        return sesCollection
            .whereEqualTo("is_published", true)
            .orderBy("publish_timestamp", Query.Direction.DESCENDING)
            .dataObjects<SesModel>()
            .onEach { sesList: List<SesModel> ->
                sesDao.resetPublishedSes()
                sesDao.insertAll(sesList)
            }
            .flowOn(Dispatchers.IO)
    }

    fun getSeslerOffline(): Flow<List<SesModel>> = sesDao.getAllPublishedSes()

    fun getQuarantineSesler(): Flow<List<SesModel>> {
        return sesCollection
            .whereEqualTo("is_flagged", true)
            .dataObjects<SesModel>()
            .catch { e ->
                Log.e("SesRepository", "Error getting quarantine audios", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    fun getPopularSesler(): Flow<List<SesModel>> {
        return sesCollection
            .whereEqualTo("is_popular", true)
            .dataObjects<SesModel>()
            .catch { e ->
                Log.e("SesRepository", "Error getting popular audios", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    fun getUserAudios(userId: String): Flow<List<SesModel>> {
        return sesCollection
            .whereEqualTo("ownerId", userId)
            .dataObjects<SesModel>()
            .onEach { sesList: List<SesModel> ->
                sesDao.insertAll(sesList)
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getSes(sesId: String): SesModel? = withContext(Dispatchers.IO) {
        return@withContext try {
            val doc: DocumentSnapshot = sesCollection.document(sesId).get().await()
            doc.toObject<SesModel>()
        } catch (e: Exception) {
            Log.e("SesRepository", "Error fetching single ses: $sesId", e)
            null
        }
    }

    fun getSesFlow(sesId: String): Flow<SesModel?> {
        return sesCollection.document(sesId).dataObjects()
    }

    suspend fun incrementViewCount(sesId: String): Unit = withContext(Dispatchers.IO) {
        try {
            sesCollection.document(sesId).update("viewCount", FieldValue.increment(1)).await()
        } catch (e: Exception) {
            Log.e("SesRepository", "Error incrementing view count for ses $sesId", e)
        }
    }

    suspend fun uploadAudio(
        userId: String,
        username: String,
        userProfileImageUrl: String?,
        badgeType: String?,
        file: File,
        duration: Long,
        isPublished: Boolean
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val sesId: String = UUID.randomUUID().toString()
            val storageRef: StorageReference = storage.reference.child("sesler/$sesId.m4a")
            val audioUri: Uri = Uri.fromFile(file)

            storageRef.putFile(audioUri).await()
            val downloadUri: Uri = storageRef.downloadUrl.await()
            val downloadUrl: String = downloadUri.toString()
            val country: String = Locale.getDefault().country ?: ""

            val ses: SesModel = SesModel(
                id = sesId,
                ownerId = userId,
                username = username,
                userProfileImageUrl = userProfileImageUrl,
                senderName = username,
                audioUrl = downloadUrl,
                duration = duration,
                isPublished = isPublished,
                createdAt = null,
                anobaResponse = null,
                isOfficial = username.contains("anoba", ignoreCase = true),
                countryCode = country,
                userBadgeType = badgeType
            )

            val docRef: DocumentReference = sesCollection.document(sesId)
            docRef.set(ses).await()
            val updates: MutableMap<String, Any> = mutableMapOf(
                "timestamp" to FieldValue.serverTimestamp()
            )
            if (isPublished) {
                updates["publish_timestamp"] = FieldValue.serverTimestamp()
            }
            docRef.update(updates).await()

            sesDao.insertSes(ses)

            sesId
        } catch (e: Exception) {
            Log.e("SesRepository", "Error uploading audio", e)
            null
        }
    }

    suspend fun togglePublishState(sesId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val docRef: DocumentReference = sesCollection.document(sesId)
            val snapshot: DocumentSnapshot = docRef.get().await()
            val currentState: Boolean = snapshot.getBoolean("is_published") ?: false

            if (!currentState) {
                docRef.update(mapOf(
                    "is_published" to true,
                    "publish_timestamp" to FieldValue.serverTimestamp()
                )).await()
            } else {
                docRef.update("is_published", false).await()
            }
        } catch (e: Exception) {
            Log.e("SesRepository", "Error toggling publish state for audio: $sesId", e)
            throw e
        }
    }

    suspend fun deleteSes(sesId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val sesDoc: DocumentSnapshot = sesCollection.document(sesId).get().await()
            val audioUrl: String? = sesDoc.getString("audioUrl")

            // 🟢🛡️✅ BİLDİRİM: İşaretleyenlere "Silindi" bildirimi gönder
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
                            "type" to "REPORTER_DELETED",
                            "targetId" to sesId,
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
                    Log.e("SesRepository", "Error sending delete notifications to reporters", e)
                }
            }

            sesCollection.document(sesId).delete().await()
            sesDao.deleteSesById(sesId)

            if (!audioUrl.isNullOrEmpty()) {
                try {
                    val fileRef: StorageReference = storage.getReferenceFromUrl(audioUrl)
                    fileRef.delete().await()
                } catch (e: Exception) {
                    if (e is com.google.firebase.storage.StorageException && e.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND) {
                        Log.d("SesRepository", "Audio file already deleted from storage.")
                    } else {
                        Log.w("SesRepository", "Storage delete fail for $audioUrl", e)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e("SesRepository", "Error deleting ses: $sesId", e)
            false
        }
    }

    suspend fun deleteAllUserAudios(userId: String): Unit = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        try {
            val s1: QuerySnapshot = sesCollection.whereEqualTo("ownerId", userId).get().await()
            val s2: QuerySnapshot = sesCollection.whereEqualTo("senderId", userId).get().await()
            val snapshots: List<QuerySnapshot> = listOf(s1, s2)

            for (snapshot: QuerySnapshot in snapshots) {
                val docList: List<DocumentSnapshot> = snapshot.documents
                for (doc: DocumentSnapshot in docList) {
                    val audioUrl: String? = doc.getString("audioUrl")
                    val docId: String = doc.id
                    if (!audioUrl.isNullOrEmpty()) {
                        try {
                            val fileRef: StorageReference = storage.getReferenceFromUrl(audioUrl)
                            fileRef.delete().await()
                        } catch (e: Exception) {
                            if (!(e is com.google.firebase.storage.StorageException && e.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND)) {
                                Log.w("SesRepository", "Storage delete fail for user $userId", e)
                            }
                        }
                    }
                    val ref: DocumentReference = doc.reference
                    ref.delete().await()
                    sesDao.deleteSesById(docId)
                }
            }
            Log.d("SesRepository", "All audios for user $userId deleted.")
        } catch (e: Exception) {
            Log.e("SesRepository", "Error deleting all audios for user $userId", e)
        }
    }

    suspend fun getAudioUriForSharing(context: Context, audioUrl: String, sesId: String): Uri = withContext(Dispatchers.IO) {
        val storageRef: StorageReference = storage.getReferenceFromUrl(audioUrl)
        val localFile: File = File(context.cacheDir, "$sesId.m4a")

        storageRef.getFile(localFile).await()

        incrementShareCount(sesId)

        val packageName: String = context.packageName ?: ""
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "$packageName.provider",
            localFile
        )
        return@withContext fileUri
    }

    suspend fun incrementShareCount(sesId: String): Unit = withContext(Dispatchers.IO) {
        try {
            sesCollection.document(sesId)
                .update("shareCount", FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            Log.e("SesRepository", "Error incrementing share count for audio $sesId", e)
        }
    }

    suspend fun updateSes(sesId: String, updates: Map<String, Any?>): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            sesCollection.document(sesId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e("SesRepository", "Error updating ses: $sesId", e)
            false
        }
    }

    suspend fun unflagSes(sesId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            sesCollection.document(sesId).update(
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
                            "type" to "REPORTER_UNFLAG",
                            "targetId" to sesId,
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
                    Log.e("SesRepository", "Error sending unflag notifications to reporters", e)
                }
            }

            true
        } catch (e: Exception) {
            Log.e("SesRepository", "Error unflagging ses: $sesId", e)
            false
        }
    }

    @Suppress("unused")
    suspend fun sendSesNotification(
        sesId: String,
        senderUsername: String,
        senderPhotoUrl: String?,
        notifType: String,
        messageText: String
    ): Unit = withContext(Dispatchers.IO) {
        try {
            val sesDoc = sesCollection.document(sesId).get().await()
            val ownerId = sesDoc.getString("ownerId")

            if (!ownerId.isNullOrBlank()) {
                val usernameClean = senderUsername.ifBlank { "Bir kullanıcı" }
                val notificationData = mapOf(
                    "type" to notifType,
                    "targetId" to sesId,
                    "sesId" to sesId,
                    "title" to usernameClean,
                    "senderName" to usernameClean,
                    "senderPhotoUrl" to (senderPhotoUrl ?: ""),
                    "message" to messageText,
                    "body" to "$usernameClean: $messageText",
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(ownerId)
                    .collection("notifications").add(notificationData)
            }
        } catch (e: Exception) {
            Log.e("SesRepository", "Ses bildirimi gönderilemedi: $sesId", e)
        }
    }
}
