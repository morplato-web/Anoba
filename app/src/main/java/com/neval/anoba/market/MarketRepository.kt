package com.neval.anoba.market

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Date

class MarketRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val marketDao: MarketDao
) {

    fun getMarketItemsStream(): Flow<List<MarketItemModel>> {
        return marketDao.getAllItems()
    }

    suspend fun syncMarketItemsFromRemote() = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("market_items")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(MarketItemModel::class.java)?.apply { id = doc.id }
            }

            marketDao.clearAll()
            marketDao.insertAll(items)
        } catch (e: Exception) {
            Log.e("MarketRepository", "Pazar yeri ilanlarını senkronize etme hatası: ${e.message}")
        }
    }

    suspend fun createMarketItem(
        context: Context,
        imageUri: Uri,
        title: String,
        description: String,
        price: Double,
        category: String,
        city: String,
        condition: String
    ): Result<MarketItemModel> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser ?: return@withContext Result.failure(Exception("Oturum açılmamış"))
            val compressedBytes = compressImage(context, imageUri)
                ?: return@withContext Result.failure(Exception("Görsel sıkıştırılamadı"))

            val storagePath = "market_photos/${currentUser.uid}_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child(storagePath)
            
            storageRef.putBytes(compressedBytes).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val docRef = firestore.collection("market_items").document()
            
            val newItem = MarketItemModel(
                id = docRef.id,
                title = title,
                description = description,
                price = price,
                currency = "₺",
                category = category,
                condition = condition,
                city = city,
                photoUrl = downloadUrl,
                ownerId = currentUser.uid,
                username = currentUser.displayName ?: "Kullanıcı",
                userProfileImageUrl = currentUser.photoUrl?.toString(),
                createdAt = Date(),
                isSold = false,
                isOfficial = false,
                userBadgeType = "NONE"
            )

            docRef.set(newItem).await()
            marketDao.insert(newItem)

            Result.success(newItem)
        } catch (e: Exception) {
            Log.e("MarketRepository", "İlan oluşturma hatası: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun markAsSold(itemId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val existing = marketDao.getItemById(itemId) ?: return@withContext false
            val updated = existing.copy(isSold = true)
            
            marketDao.insert(updated)
            firestore.collection("market_items").document(itemId)
                .update("is_sold", true)
                .await()
            true
        } catch (e: Exception) {
            Log.e("MarketRepository", "Satıldı olarak işaretleme hatası: ${e.message}")
            false
        }
    }

    suspend fun deleteItem(itemId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            marketDao.deleteById(itemId)
            firestore.collection("market_items").document(itemId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("MarketRepository", "İlan silme hatası: ${e.message}")
            false
        }
    }

    private suspend fun compressImage(context: Context, imageUri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val rotation = getImageRotation(context, imageUri)
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            if (origWidth <= 0 || origHeight <= 0) return@withContext null

            val maxDimension = 1080
            var sampleSize = 1
            val halfWidth = origWidth / 2
            val halfHeight = origHeight / 2
            while ((halfWidth / sampleSize) >= maxDimension && (halfHeight / sampleSize) >= maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val sampledBitmap = context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@withContext null

            val currentWidth = if (rotation == 90 || rotation == 270) sampledBitmap.height else sampledBitmap.width
            val currentHeight = if (rotation == 90 || rotation == 270) sampledBitmap.width else sampledBitmap.height

            val scale = if (currentWidth > maxDimension || currentHeight > maxDimension) {
                maxDimension.toFloat() / maxOf(currentWidth, currentHeight).toFloat()
            } else 1.0f

            val matrix = Matrix()
            if (scale != 1.0f) matrix.postScale(scale, scale)
            if (rotation != 0) matrix.postRotate(rotation.toFloat())

            val finalBitmap = Bitmap.createBitmap(
                sampledBitmap, 0, 0, sampledBitmap.width, sampledBitmap.height, matrix, true
            )

            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            
            if (sampledBitmap != finalBitmap) sampledBitmap.recycle()
            finalBitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("MarketRepository", "Sıkıştırma hatası: ${e.message}")
            null
        }
    }

    private fun getImageRotation(context: Context, uri: Uri): Int {
        var rotation = 0
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val orientation = exif.getAttributeInt(
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
            Log.e("MarketRepository", "EXIF Okuma Hatası: ${e.message}")
        }
        return rotation
    }
}
