package com.neval.anoba.letter.lettercomment

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

class LetterCommentRepository(private val firestore: FirebaseFirestore) {

    companion object {
        private const val TAG = "LetterCommentRepository"
    }

    private fun getCommentsCollection(letterId: String): CollectionReference =
        firestore.collection("letters").document(letterId).collection("comments")

    fun getCommentsStream(letterId: String): Flow<List<LetterComment>> {
        return getCommentsCollection(letterId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .dataObjects<LetterComment>()
    }

    suspend fun addComment(letterId: String, comment: LetterComment): Unit = withContext(Dispatchers.IO) {
        try {
            var targetOwnerId: String? = null

            firestore.runTransaction { transaction ->
                val letterRef: DocumentReference = firestore.collection("letters").document(letterId)
                val letterSnapshot: DocumentSnapshot = transaction.get(letterRef)
                targetOwnerId = letterSnapshot.getString("ownerId")

                val newCommentRef: DocumentReference = getCommentsCollection(letterId).document()

                transaction.set(newCommentRef, comment.copy(id = newCommentRef.id))
                transaction.update(letterRef, "commentsCount", FieldValue.increment(1))
                null
            }.await()

            // 🚀 BİLDİRİM TETİKLEYİCİ: Profil fotoğrafı ve kullanıcı adı tam bağlandı
            val ownerId = targetOwnerId
            if (!ownerId.isNullOrBlank() && ownerId != comment.userId) {
                val commenterUsername = comment.username.ifBlank { "Bir kullanıcı" }
                val notificationData = mapOf(
                    "type" to "LETTER_COMMENT",
                    "targetId" to letterId,
                    "letterId" to letterId,
                    "title" to commenterUsername,
                    "senderName" to commenterUsername,
                    "senderPhotoUrl" to (comment.userProfileUrl ?: ""),
                    "message" to comment.content.ifBlank { "Mektubunuza yorum yaptı." },
                    "body" to "$commenterUsername: ${comment.content}",
                    "isRead" to false,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(ownerId)
                    .collection("notifications").add(notificationData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment to letter: $letterId", e)
            throw e
        }
    }

    suspend fun deleteComment(letterId: String, commentId: String): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.runTransaction { transaction ->
                val letterRef: DocumentReference = firestore.collection("letters").document(letterId)
                val commentRef: DocumentReference = getCommentsCollection(letterId).document(commentId)

                transaction.delete(commentRef)
                transaction.update(letterRef, "commentsCount", FieldValue.increment(-1))
                null
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comment $commentId from letter $letterId", e)
            throw e
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun toggleCommentLike(letterId: String, commentId: String, userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val commentRef: DocumentReference = getCommentsCollection(letterId).document(commentId)
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
            Log.e(TAG, "Error toggling like for comment $commentId", e)
            throw e
        }
    }
}