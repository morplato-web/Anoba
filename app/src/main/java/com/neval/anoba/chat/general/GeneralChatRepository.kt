package com.neval.anoba.chat.general

import android.net.Uri
import android.util.Log
import androidx.paging.PagingData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.neval.anoba.chat.ui.ChatDao
import com.neval.anoba.chat.ui.GeneralChatMessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GeneralChatRepository(
    firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val chatDao: ChatDao,
) {
    private val collection = firestore.collection("chatRooms")
    private val tag = "GeneralChatRepository"
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var activeListener: ListenerRegistration? = null
    private var activeRoomId: String? = null

    fun getMessagesPaging(roomId: String, searchQuery: String = ""): Flow<PagingData<GeneralChatMessage>> {
        if (activeRoomId != roomId) {
            stopRealtimeSync()
        }
        startRealtimeSync(roomId)

        val flow = if (searchQuery.isBlank()) {
            chatDao.getGeneralMessages()
        } else {
            chatDao.searchGeneralMessages(searchQuery)
        }
        return flow.map { list ->
            PagingData.from(list.map { it.toDomain() })
        }
    }

    private fun startRealtimeSync(roomId: String) {
        if (activeListener != null && activeRoomId == roomId) return

        stopRealtimeSync()
        activeRoomId = roomId

        activeListener = collection.document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.w(tag, "Realtime sync error: ${error?.message}")
                    return@addSnapshotListener
                }

                val messages = snapshot.toObjects(GeneralChatMessage::class.java)
                val entities = messages.map { it.toEntity() }

                repositoryScope.launch {
                    chatDao.insertGeneralMessages(entities)
                }
            }
    }

    fun stopRealtimeSync() {
        activeListener?.remove()
        activeListener = null
        activeRoomId = null
    }

    private fun GeneralChatMessage.toEntity() = GeneralChatMessageEntity(
        id = this.documentId.ifBlank { UUID.randomUUID().toString() },
        senderId = this.senderId ?: "",
        senderName = this.senderName ?: "",
        senderPhotoUrl = this.senderPhotoUrl,
        content = this.content,
        imageUrl = this.imageUrl,
        videoUrl = this.videoUrl,
        audioUrl = this.audioUrl,
        audioDuration = this.audioDuration,
        senderBadgeType = this.senderBadgeType,
        timestampMillis = this.timestamp?.time ?: System.currentTimeMillis(),
        replyToId = this.replyToId,
        replyToName = this.replyToName,
        replyToContent = this.replyToContent
    )

    private fun GeneralChatMessageEntity.toDomain() = GeneralChatMessage(
        documentId = this.id,
        senderId = this.senderId,
        senderName = this.senderName,
        senderPhotoUrl = this.senderPhotoUrl,
        content = this.content,
        imageUrl = this.imageUrl,
        videoUrl = this.videoUrl,
        audioUrl = this.audioUrl,
        audioDuration = this.audioDuration,
        senderBadgeType = this.senderBadgeType,
        timestamp = java.util.Date(this.timestampMillis),
        replyToId = this.replyToId,
        replyToName = this.replyToName,
        replyToContent = this.replyToContent
    )

    suspend fun addMessage(roomId: String, message: GeneralChatMessage) {
        try {
            collection.document(roomId)
                .collection("messages")
                .add(message).await()
        } catch (e: Exception) {
            Log.e(tag, "Message add error", e)
            throw e
        }
    }

    suspend fun uploadImage(roomId: String, imageUri: Uri): String? {
        return try {
            val imageId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("chat_images/$roomId/$imageId.jpg")
            storageRef.putFile(imageUri).await().storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Resim yükleme hatası", e)
            null
        }
    }

    suspend fun uploadVideo(roomId: String, videoUri: Uri): String? {
        return try {
            val videoId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("chat_videos/$roomId/$videoId.mp4")
            storageRef.putFile(videoUri).await().storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Video yükleme hatası", e)
            null
        }
    }

    suspend fun uploadAudio(roomId: String, audioUri: Uri): String? {
        return try {
            val audioId = "${UUID.randomUUID()}.m4a"
            val storageRef = storage.reference.child("chat_audio/$roomId/$audioId")
            storageRef.putFile(audioUri).await().storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Ses yükleme hatası", e)
            null
        }
    }

    private suspend fun deleteFileFromStorage(fileUrl: String) {
        if (fileUrl.isBlank()) return
        try {
            val storageRef = storage.getReferenceFromUrl(fileUrl)
            storageRef.delete().await()
        } catch (e: Exception) {
            Log.w(tag, "Error deleting file from storage: $fileUrl", e)
        }
    }

    suspend fun deleteMessage(roomId: String, message: GeneralChatMessage) {
        if (roomId.isBlank() || message.documentId.isBlank()) {
            Log.e(tag, "Cannot delete message with blank roomId or messageId")
            return
        }

        message.imageUrl?.let { deleteFileFromStorage(it) }
        message.videoUrl?.let { deleteFileFromStorage(it) }
        message.audioUrl?.let { deleteFileFromStorage(it) }

        collection.document(roomId)
            .collection("messages").document(message.documentId)
            .delete().await()

        chatDao.deleteGeneralMessageById(message.documentId)
    }

    suspend fun editMessage(roomId: String, messageId: String, newContent: String) {
        if (roomId.isBlank() || messageId.isBlank()) {
            Log.e(tag, "Cannot edit message with blank roomId or messageId")
            return
        }
        collection.document(roomId)
            .collection("messages").document(messageId)
            .update("content", newContent)
            .await()
    }
}