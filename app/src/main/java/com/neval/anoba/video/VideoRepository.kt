@file:Suppress("RedundantExplicitType", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package com.neval.anoba.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
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
import com.google.firebase.storage.UploadTask
import com.neval.anoba.mycontent.VideoDownloadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class VideoRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val videoDao: VideoDao
) {
    private val videoCollection: CollectionReference = firestore.collection("videos")
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getVideos(): Flow<List<VideoModel>> {
        return videoCollection
            .whereEqualTo("is_published", true)
            .orderBy("publish_timestamp", Query.Direction.DESCENDING)
            .dataObjects<VideoModel>()
            .onEach { videos: List<VideoModel> ->
                videoDao.resetPublishedVideos()
                videoDao.insertAll(videos)
            }
            .flowOn(Dispatchers.IO)
    }

    fun getVideosOffline(): Flow<List<VideoModel>> = videoDao.getAllPublishedVideos()

    fun getQuarantineVideos(): Flow<List<VideoModel>> {
        return videoCollection
            .whereEqualTo("is_flagged", true)
            .dataObjects<VideoModel>()
            .catch { e ->
                Log.e("VideoRepository", "Error getting quarantine videos", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    fun getPopularVideos(): Flow<List<VideoModel>> {
        return videoCollection
            .whereEqualTo("is_popular", true)
            .dataObjects<VideoModel>()
            .catch { e ->
                Log.e("VideoRepository", "Error getting popular videos", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    fun getUserVideos(userId: String): Flow<List<VideoModel>> {
        return videoCollection
            .whereEqualTo("ownerId", userId)
            .dataObjects<VideoModel>()
            .onEach { videos: List<VideoModel> ->
                videoDao.insertAll(videos)
            }
            .flowOn(Dispatchers.IO)
    }

    fun getVideo(videoId: String): Flow<VideoModel?> {
        return videoCollection.document(videoId).dataObjects()
    }

    suspend fun getVideoById(videoId: String): VideoModel? = withContext(Dispatchers.IO) {
        try {
            val doc: DocumentSnapshot = videoCollection.document(videoId).get().await()
            doc.toObject<VideoModel>()
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error getting video by id", e)
            null
        }
    }

    suspend fun incrementViewCount(videoId: String) {
        withContext(Dispatchers.IO) {
            try {
                if (videoId.isNotBlank()) {
                    videoCollection.document(videoId).update("viewCount", FieldValue.increment(1)).await()
                    videoDao.incrementLocalViewCount(videoId)
                }
            } catch (e: Exception) {
                Log.e("VideoRepository", "Error incrementing view count for video $videoId", e)
            }
        }
    }

    suspend fun updateVideoTextDetails(videoId: String, title: String, personalNote: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates: Map<String, Any> = mapOf(
                "title" to title,
                "personalNote" to personalNote
            )
            videoCollection.document(videoId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error updating video text details", e)
            false
        }
    }

    suspend fun unflagVideo(videoId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            videoCollection.document(videoId).update(
                mapOf(
                    "is_flagged" to false,
                    "is_published" to true,
                    "flag_reason" to null,
                    "flagged_at" to null
                )
            ).await()

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
                            "type" to "REPORTER_UNFLAG",
                            "targetId" to videoId,
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
                    Log.e("VideoRepository", "Error sending unflag notifications to reporters", e)
                }
            }

            true
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error unflagging video: $videoId", e)
            false
        }
    }

    /**
     * Videoyu Media3 Transformer ile H.264 + AAC + Faststart ve ~3.5 Mbps hedef bitrate formatında sıkıştırır.
     */
    @OptIn(UnstableApi::class)
    suspend fun compressVideo(context: Context, sourceUri: Uri): File = withContext(Dispatchers.Main) {
        val cacheDir = File(context.cacheDir, "compressed_videos").apply { if (!exists()) mkdirs() }
        val outputFile = File(cacheDir, "cmp_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.mp4")

        suspendCancellableCoroutine { continuation ->
            val encoderFactory = DefaultEncoderFactory.Builder(context)
                .setRequestedVideoEncoderSettings(
                    VideoEncoderSettings.Builder()
                        .setBitrate(3_500_000)
                        .build()
                )
                .build()

            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .setEncoderFactory(encoderFactory)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        Log.d("VideoRepository", "Video sıkıştırma başarılı: ${outputFile.length() / 1024} KB")
                        continuation.resume(outputFile)
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        Log.e("VideoRepository", "Sıkıştırma hatası, kaynak video kullanılacak", exportException)
                        if (outputFile.exists()) outputFile.delete()
                        continuation.resumeWithException(exportException)
                    }
                })
                .build()

            // 1080p dikey çözünürlük ölçeklemesi
            val presentationEffect = Presentation.createForHeight(1920)
            val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(sourceUri))
                .setEffects(Effects(emptyList(), listOf(presentationEffect)))
                .build()

            try {
                transformer.start(editedItem, outputFile.absolutePath)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }

            continuation.invokeOnCancellation {
                transformer.cancel()
                if (outputFile.exists()) outputFile.delete()
            }
        }
    }

    suspend fun uploadVideo(
        context: Context,
        userId: String,
        username: String,
        userProfileImageUrl: String?,
        badgeType: String?,
        videoUri: Uri,
        title: String,
        duration: Long,
        isPublished: Boolean
    ): String? = withContext(Dispatchers.IO) {
        var compressedFile: File? = null
        try {
            // 🚀 Otomatik sıkıştırma adımı
            val fileToUploadUri: Uri = try {
                compressedFile = compressVideo(context, videoUri)
                Uri.fromFile(compressedFile)
            } catch (e: Exception) {
                Log.w("VideoRepository", "Otomatik sıkıştırma başarısız oldu, orijinal video yükleniyor", e)
                videoUri
            }

            val videoId: String = UUID.randomUUID().toString()
            val newDocRef: DocumentReference = videoCollection.document(videoId)
            val videoStorageRef: StorageReference = storage.reference.child("videos/$videoId")

            val uploadTask: UploadTask.TaskSnapshot = videoStorageRef.putFile(fileToUploadUri).await()
            val downloadUrl: String = uploadTask.storage.downloadUrl.await().toString()
            val thumbnailUrl: String? = createAndUploadThumbnail(downloadUrl, videoId)
            val countryCode: String = Locale.getDefault().country ?: ""

            val video = VideoModel(
                id = videoId,
                ownerId = userId,
                username = username,
                userProfileImageUrl = userProfileImageUrl,
                videoUrl = downloadUrl,
                thumbnailUrl = thumbnailUrl,
                title = title,
                duration = duration,
                isPublished = isPublished,
                isOfficial = username.contains("anoba", ignoreCase = true),
                countryCode = countryCode,
                userBadgeType = badgeType
            )
            newDocRef.set(video).await()
            val updates: MutableMap<String, Any> = mutableMapOf(
                "timestamp" to FieldValue.serverTimestamp()
            )
            if (isPublished) {
                updates["publish_timestamp"] = FieldValue.serverTimestamp()
            }
            newDocRef.update(updates).await()

            videoDao.insertVideo(video)
            videoId
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error uploading video", e)
            null
        } finally {
            // Cache'de oluşturulan geçici sıkıştırılmış dosyayı temizle
            try {
                if (compressedFile != null && compressedFile.exists()) {
                    compressedFile.delete()
                }
            } catch (_: Exception) {}
        }
    }

    @Suppress("unused")
    suspend fun sendVideoNotification(
        videoId: String,
        senderUsername: String,
        senderPhotoUrl: String?,
        notifType: String,
        messageText: String
    ): Unit = withContext(Dispatchers.IO) {
        try {
            val videoDoc = videoCollection.document(videoId).get().await()
            val ownerId = videoDoc.getString("ownerId")

            if (!ownerId.isNullOrBlank()) {
                val usernameClean = senderUsername.ifBlank { "Bir kullanıcı" }
                val notificationData = mapOf(
                    "type" to notifType,
                    "targetId" to videoId,
                    "videoId" to videoId,
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
            Log.e("VideoRepository", "Video bildirimi gönderilemedi: $videoId", e)
        }
    }

    private suspend fun createAndUploadThumbnail(videoUrl: String, videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoUrl, HashMap<String, String>())
            val bitmap: Bitmap? = retriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()

            if (bitmap != null) {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val data: ByteArray = baos.toByteArray()

                val thumbnailRef: StorageReference = storage.reference.child("videos/${videoId}_thumb.jpg")
                val uploadTask: UploadTask.TaskSnapshot = thumbnailRef.putBytes(data).await()
                uploadTask.storage.downloadUrl.await().toString()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error creating thumbnail for $videoId", e)
            null
        }
    }

    suspend fun togglePublishState(videoId: String) {
        withContext(Dispatchers.IO) {
            try {
                val docRef: DocumentReference = videoCollection.document(videoId)
                val snapshot: DocumentSnapshot = docRef.get().await()
                val currentState: Boolean = snapshot.getBoolean("is_published") ?: false

                if (!currentState) {
                    docRef.update(
                        mapOf(
                            "is_published" to true,
                            "publish_timestamp" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } else {
                    docRef.update("is_published", false).await()
                }
            } catch (e: Exception) {
                Log.e("VideoRepository", "Error toggling publish state for video: $videoId", e)
                throw e
            }
        }
    }

    suspend fun deleteVideo(videoId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val videoDoc: DocumentSnapshot = videoCollection.document(videoId).get().await()
            val videoUrl: String? = videoDoc.getString("videoUrl")
            val thumbnailUrl: String? = videoDoc.getString("thumbnailUrl")

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
                            "type" to "REPORTER_DELETED",
                            "targetId" to videoId,
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
                    Log.e("VideoRepository", "Error sending delete notifications to reporters", e)
                }
            }

            videoCollection.document(videoId).delete().await()
            videoDao.deleteVideoById(videoId)

            if (!videoUrl.isNullOrEmpty()) {
                try {
                    storage.getReferenceFromUrl(videoUrl).delete().await()
                } catch (e: Exception) {
                    if (e is com.google.firebase.storage.StorageException && e.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND) {
                        Log.d("VideoRepository", "Video file already deleted.")
                    } else {
                        Log.w("VideoRepository", "Storage delete fail for $videoUrl", e)
                    }
                }
            }
            if (!thumbnailUrl.isNullOrEmpty() && thumbnailUrl != videoUrl) {
                try {
                    storage.getReferenceFromUrl(thumbnailUrl).delete().await()
                } catch (e: Exception) {
                    if (!(e is com.google.firebase.storage.StorageException && e.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND)) {
                        Log.w("VideoRepository", "Storage thumb delete fail for $thumbnailUrl", e)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error deleting video: $videoId", e)
            false
        }
    }

    suspend fun deleteAllUserVideos(userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val snapshot: QuerySnapshot = videoCollection.whereEqualTo("ownerId", userId).get().await()
            for (doc: DocumentSnapshot in snapshot.documents) {
                val videoUrl: String? = doc.getString("videoUrl")
                val thumbnailUrl: String? = doc.getString("thumbnailUrl")
                val docId: String = doc.id
                if (!videoUrl.isNullOrEmpty()) {
                    try {
                        storage.getReferenceFromUrl(videoUrl).delete().await()
                    } catch (e: Exception) {
                        if (!(e is com.google.firebase.storage.StorageException && e.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND)) {
                            Log.e("VideoRepository", "Storage delete fail for user $userId", e)
                        }
                    }
                }
                if (!thumbnailUrl.isNullOrEmpty()) {
                    try {
                        storage.getReferenceFromUrl(thumbnailUrl).delete().await()
                    } catch (e: Exception) {
                        if (!(e is com.google.firebase.storage.StorageException && e.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND)) {
                            Log.e("VideoRepository", "Storage thumb delete fail for user $userId", e)
                        }
                    }
                }
                val ref: DocumentReference = doc.reference
                ref.delete().await()
                videoDao.deleteVideoById(docId)
            }
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error deleting all photos for user $userId", e)
        }
    }

    suspend fun downloadVideoToCache(context: Context, videoId: String, videoUrl: String): VideoDownloadState = withContext(Dispatchers.IO) {
        return@withContext try {
            val cacheDir = File(context.cacheDir, "editing_videos")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val localFile = File(cacheDir, "editing_$videoId.mp4")

            storage.getReferenceFromUrl(videoUrl).getFile(localFile).await()
            val packageName: String = context.packageName ?: ""
            val localUri: Uri = FileProvider.getUriForFile(context, "$packageName.provider", localFile)

            VideoDownloadState.Success(videoId, localUri)
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error downloading video to cache for videoId: $videoId", e)
            VideoDownloadState.Error("Video indirilemedi: ${e.message}")
        }
    }

    suspend fun getVideoUriForSharing(context: Context, videoUrl: String, videoId: String): Uri = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "shared_videos")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val localFile = File(cacheDir, "$videoId.mp4")

        if (!localFile.exists() || localFile.length() <= 0) {
            try {
                val storageRef: StorageReference = storage.getReferenceFromUrl(videoUrl)
                storageRef.getFile(localFile).await()
            } catch (e: Exception) {
                Log.e("VideoRepository", "Error downloading video for sharing", e)
                throw e
            }
        }

        incrementShareCount(videoId)

        val packageName: String = context.packageName ?: ""
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "$packageName.provider",
            localFile
        )
        return@withContext fileUri
    }

    suspend fun incrementShareCount(videoId: String): Unit = withContext(Dispatchers.IO) {
        try {
            if (videoId.isNotBlank()) {
                videoCollection.document(videoId)
                    .update("shareCount", FieldValue.increment(1))
                    .await()
            }
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error incrementing share count for video $videoId", e)
        }
    }

    suspend fun updateVideoFile(videoId: String, newVideoUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val docRef: DocumentReference = videoCollection.document(videoId)
            val oldVideoUrl: String? = docRef.get().await().getString("videoUrl")

            if (!oldVideoUrl.isNullOrEmpty()) {
                try {
                    storage.getReferenceFromUrl(oldVideoUrl).delete().await()
                } catch (e: Exception) {
                    Log.e("VideoRepository", "Error deleting old video file, but proceeding.", e)
                }
            }

            val newVideoRef: StorageReference = storage.reference.child("videos/$videoId")
            val uploadTask: UploadTask.TaskSnapshot = newVideoRef.putFile(newVideoUri).await()
            val newDownloadUrl: String = uploadTask.storage.downloadUrl.await().toString()

            docRef.update("videoUrl", newDownloadUrl).await()
            true
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error updating video file for videoId: $videoId", e)
            false
        }
    }
}