package com.neval.anoba.photo.photocomment

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PhotoCommentRepository(private val firestore: FirebaseFirestore) {

    private fun getCommentsCollection(photoId: String): CollectionReference =
        firestore.collection("photos").document(photoId).collection("comments")

    fun getCommentsStream(photoId: String): Flow<List<PhotoComment>> {
        return getCommentsCollection(photoId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .dataObjects<PhotoComment>()
    }

    suspend fun addComment(photoId: String, comment: PhotoComment): Unit = withContext(Dispatchers.IO) {
        try {
            var targetOwnerId: String? = null

            firestore.runTransaction { transaction ->
                val photoRef: DocumentReference = firestore.collection("photos").document(photoId)
                val photoSnapshot: DocumentSnapshot = transaction.get(photoRef)
                targetOwnerId = photoSnapshot.getString("ownerId")

                val newCommentRef: DocumentReference = getCommentsCollection(photoId).document()
                transaction.set(newCommentRef, comment.copy(id = newCommentRef.id))
                transaction.update(photoRef, "commentsCount", FieldValue.increment(1))
                null
            }.await()

            // 🚀 BİLDİRİM TETİKLEYİCİ: senderPhotoUrl düzeltildi
            val ownerId = targetOwnerId
            if (!ownerId.isNullOrBlank() && ownerId != comment.userId) {
                val commenterUsername = comment.username.ifBlank { "Bir kullanıcı" }
                val notificationData = mapOf(
                    "type" to "PHOTO_COMMENT",
                    "targetId" to photoId,
                    "photoId" to photoId,
                    "title" to commenterUsername,
                    "senderName" to commenterUsername,
                    "senderPhotoUrl" to (comment.userProfileUrl ?: ""),
                    "message" to comment.content.ifBlank { "Fotoğrafınıza yorum yaptı." },
                    "body" to "$commenterUsername: ${comment.content}",
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(ownerId)
                    .collection("notifications").add(notificationData)
            }
        } catch (e: Exception) {
            Log.e("PhotoCommentRepository", "Error adding comment to photo: $photoId", e)
            throw e
        }
    }

    suspend fun deleteComment(photoId: String, commentId: String): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.runTransaction { transaction ->
                val photoRef: DocumentReference = firestore.collection("photos").document(photoId)
                val commentRef: DocumentReference = getCommentsCollection(photoId).document(commentId)

                transaction.delete(commentRef)
                transaction.update(photoRef, "commentsCount", FieldValue.increment(-1))
                null
            }.await()
        } catch (e: Exception) {
            Log.e("PhotoCommentRepository", "Error deleting comment $commentId", e)
            throw e
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun toggleCommentLike(photoId: String, commentId: String, userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val commentRef: DocumentReference = getCommentsCollection(photoId).document(commentId)
            firestore.runTransaction { transaction ->
                val snapshot: DocumentSnapshot = transaction.get(commentRef)
                val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()

                if (likedBy.contains(userId)) {
                    transaction.update(commentRef, "likedBy", FieldValue.arrayRemove(userId))
                } else {
                    transaction.update(commentRef, "likedBy", FieldValue.arrayUnion(userId))
                }
                null
            }.await()
        } catch (e: Exception) {
            Log.e("PhotoCommentRepository", "Error toggling like for comment $commentId", e)
            throw e
        }
    }
}