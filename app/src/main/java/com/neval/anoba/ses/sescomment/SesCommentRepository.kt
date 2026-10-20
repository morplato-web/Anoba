package com.neval.anoba.ses.sescomment

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.neval.anoba.ses.SesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SesCommentRepository(
    private val firestore: FirebaseFirestore,
    private val sesRepository: SesRepository
) {

    companion object {
        private const val TAG = "SesCommentRepository"
    }

    // 🚀 DÜZELTME: "ses" yerine projede standart kullanılan "sesler" yapıldı
    private fun getCommentsCollection(sesId: String) =
        firestore.collection("sesler").document(sesId).collection("comments")

    fun getCommentsStream(sesId: String): Flow<List<SesComment>> {
        return getCommentsCollection(sesId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .dataObjects()
    }

    suspend fun addComment(sesId: String, comment: SesComment): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.runTransaction { transaction ->
                val newCommentRef = getCommentsCollection(sesId).document()
                val sesRef = firestore.collection("sesler").document(sesId)

                transaction.set(newCommentRef, comment.copy(id = newCommentRef.id))
                transaction.update(sesRef, "commentsCount", FieldValue.increment(1))
                null
            }.await()

            // 🚀 BİLDİRİM TETİKLE
            sesRepository.sendSesNotification(
                sesId = sesId,
                senderUsername = comment.username,
                senderPhotoUrl = comment.userProfileUrl,
                notifType = "SES_COMMENT",
                messageText = comment.content
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment to ses: $sesId", e)
            throw e
        }
    }

    suspend fun deleteComment(sesId: String, commentId: String): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.runTransaction { transaction ->
                val commentRef = getCommentsCollection(sesId).document(commentId)
                val sesRef = firestore.collection("sesler").document(sesId)

                transaction.delete(commentRef)
                transaction.update(sesRef, "commentsCount", FieldValue.increment(-1))
                null
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comment $commentId from ses $sesId", e)
            throw e
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun toggleCommentLike(sesId: String, commentId: String, userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val commentRef = getCommentsCollection(sesId).document(commentId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(commentRef)
                val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()

                if (likedBy.contains(userId)) {
                    transaction.update(commentRef, "likedBy", FieldValue.arrayRemove(userId))
                } else {
                    transaction.update(commentRef, "likedBy", FieldValue.arrayUnion(userId))
                }
                null
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like for ses comment $commentId", e)
            throw e
        }
    }
}