package com.neval.anoba.video.videocomment

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.neval.anoba.video.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class VideoCommentRepository(
    private val firestore: FirebaseFirestore,
    private val videoRepository: VideoRepository
) {

    companion object {
        private const val TAG = "VideoCommentRepository"
    }

    private fun getCommentsCollection(videoId: String): CollectionReference =
        firestore.collection("videos").document(videoId).collection("comments")

    fun getCommentsStream(videoId: String): Flow<List<VideoComment>> {
        return getCommentsCollection(videoId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .dataObjects<VideoComment>()
    }

    suspend fun addComment(videoId: String, comment: VideoComment): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.runTransaction { transaction ->
                val videoRef: DocumentReference = firestore.collection("videos").document(videoId)
                val newCommentRef: DocumentReference = getCommentsCollection(videoId).document()

                transaction.set(newCommentRef, comment.copy(id = newCommentRef.id))
                transaction.update(videoRef, "commentsCount", FieldValue.increment(1))
                null
            }.await()

            // 🚀 BİLDİRİM TETİKLE
            videoRepository.sendVideoNotification(
                videoId = videoId,
                senderUsername = comment.username.ifBlank { "Bir kullanıcı" },
                senderPhotoUrl = comment.userProfileUrl,
                notifType = "VIDEO_COMMENT",
                messageText = comment.content
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment to video: $videoId", e)
            throw e
        }
    }

    suspend fun deleteComment(videoId: String, commentId: String): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.runTransaction { transaction ->
                val videoRef: DocumentReference = firestore.collection("videos").document(videoId)
                val commentRef: DocumentReference = getCommentsCollection(videoId).document(commentId)

                transaction.delete(commentRef)
                transaction.update(videoRef, "commentsCount", FieldValue.increment(-1))
                null
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comment $commentId from video $videoId", e)
            throw e
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun toggleCommentLike(videoId: String, commentId: String, userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val commentRef: DocumentReference = getCommentsCollection(videoId).document(commentId)
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