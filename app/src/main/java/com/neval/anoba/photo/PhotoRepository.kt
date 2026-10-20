@file:Suppress("RedundantExplicitType", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package com.neval.anoba.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.dataObjects
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale

class PhotoRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val photoDao: PhotoDao
) {
    private val photoCollection: CollectionReference = firestore.collection("photos")
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getPhotos(): Flow<List<PhotoModel>> {
        return photoCollection
            .whereEqualTo("is_published", true)
            .orderBy("publish_timestamp", Query.Direction.DESCENDING)
            .dataObjects<PhotoModel>()
            .onEach { photos: List<PhotoModel> ->
                photoDao.resetPublishedPhotos()
                photoDao.insertAll(photos)
            }
            .catch { e: Throwable ->
                Log.e("PhotoRepository", "Error getting photos", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    fun getPhotosOffline(): Flow<List<PhotoModel>> = photoDao.getAllPublishedPhotos()

    fun getQuarantinePhotos(): Flow<List<PhotoModel>> {
        return photoCollection
            .whereEqualTo("is_flagged", true)
            .dataObjects<PhotoModel>()
            .catch { e ->
                Log.e("PhotoRepository", "Error getting quarantine photos", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    fun getPopularPhotos(): Flow<List<PhotoModel>> {
        return photoCollection
            .whereEqualTo("is_popular", true)
            .dataObjects<PhotoModel>()
            .catch { e ->
                Log.e("PhotoRepository", "Error getting popular photos", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    fun getUserPhotos(userId: String): Flow<List<PhotoModel>> {
        return photoCollection
            .whereEqualTo("ownerId", userId)
            .dataObjects<PhotoModel>()
            .onEach { photos: List<PhotoModel> ->
                photoDao.resetPublishedPhotos()
                photoDao.insertAll(photos)
            }
            .catch { e: Throwable ->
                Log.e("PhotoRepository", "Error getting user photos", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    fun getPhoto(photoId: String): Flow<PhotoModel?> {
        if (photoId.isBlank()) {
            return flowOf(null)
        }
        return photoCollection.document(photoId).dataObjects()
    }

    suspend fun incrementViewCount(photoId: String): Unit = withContext(Dispatchers.IO) {
        try {
            if (photoId.isNotBlank()) {
                // 1. Firebase'de artır
                photoCollection.document(photoId).update("viewCount", FieldValue.increment(1)).await()

                // 2. Yerel veritabanında anında artır
                photoDao.incrementLocalViewCount(photoId)
            }
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error incrementing view count for photo $photoId", e)
        }
    }

    suspend fun uploadPhoto(
        userId: String,
        username: String,
        userProfileImageUrl: String?,
        badgeType: String?,
        photoUri: Uri,
        title: String,
        isPublished: Boolean,
        isGuideContent: Boolean = false,
        context: Context
    ): String? = withContext(Dispatchers.IO) {
        try {
            val newDocRef: DocumentReference = photoCollection.document()
            val photoId: String = newDocRef.id

            val compressedData: ByteArray? = compressImage(context, photoUri)
            val storageRef: StorageReference = storage.reference.child("photos/$photoId")

            val uploadTask: UploadTask.TaskSnapshot = if (compressedData != null) {
                storageRef.putBytes(compressedData).await()
            } else {
                storageRef.putFile(photoUri).await()
            }

            val downloadUri: Uri = uploadTask.storage.downloadUrl.await()
            val downloadUrl: String = downloadUri.toString()
            val countryCode: String = Locale.getDefault().country ?: ""

            val photo = PhotoModel(
                id = photoId,
                ownerId = userId,
                username = username,
                userProfileImageUrl = userProfileImageUrl,
                photoUrl = downloadUrl,
                title = title,
                isPublished = isPublished,
                isGuideContent = isGuideContent,
                createdAt = null,
                isOfficial = username.contains("anoba", ignoreCase = true),
                countryCode = countryCode,
                userBadgeType = badgeType
            )

            newDocRef.set(photo).await()
            val updates: MutableMap<String, Any> = mutableMapOf(
                "timestamp" to FieldValue.serverTimestamp()
            )
            if (isPublished) {
                updates["publish_timestamp"] = FieldValue.serverTimestamp()
            }
            newDocRef.update(updates).await()

            photoDao.insertPhoto(photo)
            photoId
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error uploading photo", e)
            null
        }
    }

    // 🚀 BİLDİRİM TETİKLEYİCİ: Fotoğraf Beğenisi / Yorumu (Gizlilik ve targetId uyumlu)
    @Suppress("unused")
    suspend fun sendPhotoNotification(
        photoId: String,
        senderUsername: String,
        senderPhotoUrl: String?,
        notifType: String, // "PHOTO_LIKE", "PHOTO_COMMENT"
        messageText: String
    ): Unit = withContext(Dispatchers.IO) {
        try {
            val photoDoc = photoCollection.document(photoId).get().await()
            val ownerId = photoDoc.getString("ownerId")

            if (!ownerId.isNullOrBlank()) {
                val usernameClean = senderUsername.ifBlank { "Bir kullanıcı" }
                val notificationData = mapOf(
                    "type" to notifType,
                    "targetId" to photoId,
                    "photoId" to photoId,
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
            Log.e("PhotoRepository", "Fotoğraf bildirimi gönderilemedi: $photoId", e)
        }
    }

    suspend fun togglePublishState(photoId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val docRef: DocumentReference = photoCollection.document(photoId)
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
            Log.e("PhotoRepository", "Error toggling publish state for photo: $photoId", e)
            throw e
        }
    }

    suspend fun deletePhoto(photoId: String): Boolean = withContext(Dispatchers.IO) {
        if (photoId.isBlank()) return@withContext false

        val docRef: DocumentReference = photoCollection.document(photoId)
        try {
            val snapshot: DocumentSnapshot = docRef.get().await()
            val photoUrl: String? = snapshot.getString("photoUrl")

            if (!photoUrl.isNullOrEmpty()) {
                try {
                    val fileRef: StorageReference = storage.getReferenceFromUrl(photoUrl)
                    fileRef.delete().await()
                } catch (storageException: Exception) {
                    if (storageException is com.google.firebase.storage.StorageException && storageException.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND) {
                        Log.d("PhotoRepository", "Photo file already deleted from storage.")
                    } else {
                        Log.e("PhotoRepository", "Error deleting from Storage", storageException)
                    }
                }
            }

            // 🟢🛡️✅ BİLDİRİM: İşaretleyenlere "Silindi" bildirimi gönder
            scope.launch {
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
                            "type" to "REPORTER_DELETED",
                            "targetId" to photoId,
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
                    Log.e("PhotoRepository", "Error sending delete notifications to reporters", e)
                }
            }

            docRef.delete().await()
            photoDao.deletePhotoById(photoId)
            true
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error deleting photo: $photoId", e)
            false
        }
    }

    suspend fun deleteAllUserPhotos(userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val snapshot: QuerySnapshot = photoCollection.whereEqualTo("ownerId", userId).get().await()
            for (doc: DocumentSnapshot in snapshot.documents) {
                val photoUrl: String? = doc.getString("photoUrl")
                val docId: String = doc.id
                if (!photoUrl.isNullOrEmpty()) {
                    try {
                        val fileRef: StorageReference = storage.getReferenceFromUrl(photoUrl)
                        fileRef.delete().await()
                    } catch (e: Exception) {
                        if (!(e is com.google.firebase.storage.StorageException && e.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND)) {
                            Log.e("PhotoRepository", "Storage delete fail for user $userId", e)
                        }
                    }
                }
                val ref: DocumentReference = doc.reference
                ref.delete().await()
                photoDao.deletePhotoById(docId)
            }
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error deleting all photos for user $userId", e)
        }
    }

    suspend fun getPhotoUriForSharing(context: Context, photoUrl: String, photoId: String): Uri = withContext(Dispatchers.IO) {
        val storageRef: StorageReference = storage.getReferenceFromUrl(photoUrl)
        val localFile: File = File(context.cacheDir, "$photoId.jpg")

        storageRef.getFile(localFile).await()
        incrementShareCount(photoId)

        val packageName: String = context.packageName ?: ""
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "$packageName.provider",
            localFile
        )
        return@withContext fileUri
    }

    suspend fun incrementShareCount(photoId: String): Unit = withContext(Dispatchers.IO) {
        try {
            if (photoId.isNotBlank()) {
                photoCollection.document(photoId)
                    .update("shareCount", FieldValue.increment(1))
                    .await()
            }
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error incrementing share count for photo $photoId", e)
        }
    }

    suspend fun updatePhotoDetails(
        photoId: String,
        title: String,
        personalNote: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates: Map<String, Any> = mapOf(
                "title" to title,
                "personalNote" to personalNote
            )
            photoCollection.document(photoId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error updating photo details: $photoId", e)
            false
        }
    }

    suspend fun unflagPhoto(photoId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            photoCollection.document(photoId).update(
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
                            "type" to "REPORTER_UNFLAG",
                            "targetId" to photoId,
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
                    Log.e("PhotoRepository", "Error sending unflag notifications", e)
                }
            }

            true
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error unflagging photo: $photoId", e)
            false
        }
    }

    private suspend fun compressImage(context: Context, imageUri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        try {
            // 1. EXIF Yön Bilgisini Oku (AndroidX ExifInterface ile)
            val rotation = getImageRotation(context, imageUri)

            // 2. Boyutları Oku (Belleğe resim yüklemeden - OutOfMemory Önlemi)
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            if (origWidth <= 0 || origHeight <= 0) return@withContext null

            // 3. Uygun inSampleSize hesapla
            val maxDimension = 1080
            var sampleSize = 1
            val halfWidth = origWidth / 2
            val halfHeight = origHeight / 2
            while ((halfWidth / sampleSize) >= maxDimension && (halfHeight / sampleSize) >= maxDimension) {
                sampleSize *= 2
            }

            // 4. Optimize edilmiş boyutta Bitmap'i yükle
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Bellek kullanımını %50 daha düşürür
            }

            val sampledBitmap = context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@withContext null

            // 5. Hem Döndürmeyi hem de 1080px Ölçeklemeyi TEK MATRIX ile yap
            val currentWidth = if (rotation == 90 || rotation == 270) sampledBitmap.height else sampledBitmap.width
            val currentHeight = if (rotation == 90 || rotation == 270) sampledBitmap.width else sampledBitmap.height

            val scale = if (currentWidth > maxDimension || currentHeight > maxDimension) {
                maxDimension.toFloat() / maxOf(currentWidth, currentHeight).toFloat()
            } else {
                1.0f
            }

            val matrix = Matrix().apply {
                if (rotation != 0) postRotate(rotation.toFloat())
                postScale(scale, scale)
            }

            val finalBitmap = Bitmap.createBitmap(
                sampledBitmap, 0, 0, sampledBitmap.width, sampledBitmap.height, matrix, true
            )

            // sampledBitmap artık gerekmiyorsa bellekten temizle
            if (finalBitmap != sampledBitmap) {
                sampledBitmap.recycle()
            }

            // 6. Sıkıştır ve Byte Dizisine Çevir
            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            finalBitmap.recycle()

            val result: ByteArray = outputStream.toByteArray()
            outputStream.close()

            Log.d("PhotoRepository", "Optimizasyon başarılı: Rotation: $rotation, Boyut: ${result.size / 1024} KB")
            result
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Resim işleme hatası", e)
            null
        }
    }

    private fun getImageRotation(context: Context, uri: Uri): Int {
        var rotation = 0
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exifInterface = ExifInterface(input)
                val orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                rotation = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
        } catch (e: Exception) {
            Log.e("PhotoRepository", "EXIF okuma hatası", e)
        }
        return rotation
    }
}
