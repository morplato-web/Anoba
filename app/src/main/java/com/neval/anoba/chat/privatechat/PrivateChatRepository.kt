package com.neval.anoba.chat.privatechat

import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.ListenerRegistration
import com.neval.anoba.chat.data.ChatDao
import com.neval.anoba.chat.data.PrivateMessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PrivateChatRepository(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val chatDao: ChatDao,
) {
    private val tag = "PrivateChatRepository"
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val historyMutex = Mutex()
    private val activeListeners = mutableMapOf<String, ListenerRegistration>()

    private fun getChatRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_$userId2" else "${userId2}_$userId1"
    }

    fun getMessagesPaging(currentUserId: String, otherUserId: String): Flow<PagingData<PrivateMessage>> {
        startRealtimeSync(currentUserId, otherUserId)

        return chatDao.getPrivateMessages(currentUserId, otherUserId).map { list ->
            PagingData.from(list.map { it.toDomain() })
        }
    }

    suspend fun loadOlderMessages(currentUserId: String, otherUserId: String, anchorTimestamp: Long?) {
        if (historyMutex.isLocked) return

        historyMutex.withLock {
            val referenceTimestamp = anchorTimestamp ?: chatDao.getOldestPrivateMessageTimestamp(currentUserId, otherUserId) ?: return@withLock
            val chatRoomId = getChatRoomId(currentUserId, otherUserId)

            try {
                val snapshot = db.collection("private_chats").document(chatRoomId)
                    .collection("messages")
                    .whereLessThan("timestamp", java.util.Date(referenceTimestamp))
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val messages = snapshot.toObjects(PrivateMessage::class.java)
                    val entities = messages.map { it.toEntity() }
                    chatDao.insertPrivateMessages(entities)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading older messages for $chatRoomId", e)
            }
        }
    }

    private fun startRealtimeSync(currentUserId: String, otherUserId: String) {
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        if (activeListeners.containsKey(chatRoomId)) return

        val listener = db.collection("private_chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Snapshot listener error for room $chatRoomId", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.toObjects(PrivateMessage::class.java)
                    val entities = messages.map { it.toEntity() }

                    repositoryScope.launch {
                        try {
                            chatDao.insertPrivateMessages(entities)
                        } catch (e: Exception) {
                            Log.e(tag, "Error inserting private messages to DAO", e)
                        }
                    }
                }
            }

        activeListeners[chatRoomId] = listener
    }

    fun stopRealtimeSync(currentUserId: String, otherUserId: String) {
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        activeListeners[chatRoomId]?.remove()
        activeListeners.remove(chatRoomId)
    }

    private fun PrivateMessage.toEntity() = PrivateMessageEntity(
        id = this.id.ifBlank { UUID.randomUUID().toString() },
        senderId = this.senderId ?: "",
        senderName = this.senderName ?: "",
        senderPhotoUrl = this.senderPhotoUrl,
        receiverId = this.receiverId ?: "",
        receiverName = this.receiverName ?: "",
        content = this.content,
        imageUrl = this.imageUrl,
        videoUrl = this.videoUrl,
        audioUrl = this.audioUrl,
        audioDuration = this.audioDuration,
        senderBadgeType = this.senderBadgeType,
        timestampMillis = this.timestamp?.time ?: System.currentTimeMillis(),
        status = this.status ?: "SENT",
        replyToId = this.replyToId,
        replyToName = this.replyToName,
        replyToContent = this.replyToContent
    )

    private fun PrivateMessageEntity.toDomain() = PrivateMessage(
        id = this.id,
        senderId = this.senderId,
        senderName = this.senderName,
        senderPhotoUrl = this.senderPhotoUrl,
        receiverId = this.receiverId,
        receiverName = this.receiverName,
        content = this.content,
        imageUrl = this.imageUrl,
        videoUrl = this.videoUrl,
        audioUrl = this.audioUrl,
        audioDuration = this.audioDuration,
        senderBadgeType = this.senderBadgeType,
        timestamp = java.util.Date(this.timestampMillis),
        status = this.status,
        replyToId = this.replyToId,
        replyToName = this.replyToName,
        replyToContent = this.replyToContent
    )

    suspend fun uploadImage(chatRoomId: String, imageUri: Uri): String? {
        return try {
            val imageId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("chat_images/$chatRoomId/$imageId")
            val uploadTask = storageRef.putFile(imageUri).await()
            uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Error uploading image to chat room $chatRoomId", e)
            null
        }
    }

    suspend fun uploadVideo(chatRoomId: String, videoUri: Uri): String? {
        return try {
            val videoId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("chat_videos/$chatRoomId/$videoId")
            val uploadTask = storageRef.putFile(videoUri).await()
            uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Error uploading video to chat room $chatRoomId", e)
            null
        }
    }

    suspend fun uploadAudio(chatRoomId: String, audioUri: Uri): String? {
        return try {
            val audioId = "${UUID.randomUUID()}.m4a"
            val storageRef = storage.reference.child("chat_audio/$chatRoomId/$audioId")
            val uploadTask = storageRef.putFile(audioUri).await()
            uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Error uploading audio to chat room $chatRoomId", e)
            null
        }
    }

    suspend fun sendPrivateMessage(message: PrivateMessage, mediaTypeString: String? = null) {
        val senderId = message.senderId
        val receiverId = message.receiverId
        if ((senderId == null) || (receiverId == null)) {
            Log.e(tag, "Cannot send message with null senderId or receiverId")
            return
        }
        val chatRoomId = getChatRoomId(senderId, receiverId)

        val chatRoomRef = db.collection("private_chats").document(chatRoomId)
        val messageRef = chatRoomRef.collection("messages").document()

        val lastMessageContent = message.content?.takeIf { it.isNotBlank() } ?: mediaTypeString ?: ""

        try {
            db.runTransaction { transaction ->
                val roomSnapshot = transaction.get(chatRoomRef)

                if (!roomSnapshot.exists()) {
                    val participantsList = listOf(senderId, receiverId)
                    val newRoomData = mapOf(
                        "participants" to participantsList,
                        "lastMessage" to lastMessageContent,
                        "lastActivity" to FieldValue.serverTimestamp(),
                        "status" to "PENDING",
                        "requestedBy" to senderId
                    )
                    transaction.set(chatRoomRef, newRoomData)
                } else {
                    val roomUpdates = mapOf(
                        "lastMessage" to lastMessageContent,
                        "lastActivity" to FieldValue.serverTimestamp()
                    )
                    transaction.update(chatRoomRef, roomUpdates)
                }

                transaction.set(messageRef, message)
                null
            }.await()

            // 🚀 BİLDİRİM: !! uyarısı temizlendi, targetId (senderId) eklendi
            val senderUsername = message.senderName?.ifBlank { "Bir kullanıcı" } ?: "Bir kullanıcı"
            val msgContent = message.content
            val notifBody = when {
                !msgContent.isNullOrBlank() -> msgContent
                message.imageUrl != null -> "📷 Fotoğraf"
                message.videoUrl != null -> "🎥 Video"
                message.audioUrl != null -> "🎙️ Sesli Mesaj"
                else -> "Yeni bir mesajınız var."
            }

            val notificationData = mapOf(
                "type" to "CHAT",
                "targetId" to senderId,
                "isGroup" to false, // 🚀 ÖZEL SOHBET OLDUĞUNU BELİRTTİK
                "senderId" to senderId,
                "title" to senderUsername,
                "senderName" to senderUsername,
                "senderPhotoUrl" to (message.senderPhotoUrl ?: ""),
                "message" to notifBody,
                "body" to notifBody,
                "isRead" to false,
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(receiverId)
                .collection("notifications").add(notificationData)

            Log.i(tag, "Transaction and notification successful for message to $chatRoomId")
        } catch (e: Exception) {
            Log.e(tag, "Error in sendPrivateMessage transaction for $chatRoomId", e)
            throw e
        }
    }

    private suspend fun deleteFileFromStorage(fileUrl: String?) {
        if (fileUrl.isNullOrBlank()) return
        try {
            val storageRef = storage.getReferenceFromUrl(fileUrl)
            storageRef.delete().await()
        } catch (e: Exception) {
            Log.w(tag, "Error deleting file from storage: $fileUrl", e)
        }
    }

    suspend fun deletePrivateMessage(currentUserId: String, partnerUserId: String, message: PrivateMessage) {
        message.imageUrl?.let { deleteFileFromStorage(it) }
        message.videoUrl?.let { deleteFileFromStorage(it) }
        message.audioUrl?.let { deleteFileFromStorage(it) }

        val chatRoomId = getChatRoomId(currentUserId, partnerUserId)
        db.collection("private_chats")
            .document(chatRoomId)
            .collection("messages")
            .document(message.id)
            .delete()
            .await()

        chatDao.deletePrivateMessageById(message.id)
    }

    suspend fun updatePrivateMessageContent(
        senderId: String,
        receiverId: String,
        messageId: String,
        newContent: String
    ) {
        val chatRoomId = getChatRoomId(senderId, receiverId)
        val messageRef = db.collection("private_chats")
            .document(chatRoomId)
            .collection("messages")
            .document(messageId)
        messageRef.update("content", newContent).await()
    }

    suspend fun addPrivateMessages(currentUserId: String, otherUserId: String, messages: List<PrivateMessage>) {
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        val batch = db.batch()
        val messagesCollection = db.collection("private_chats").document(chatRoomId).collection("messages")

        messages.forEach { message ->
            val docRef = messagesCollection.document(message.id)
            batch.set(docRef, message)
        }
        batch.commit().await()
    }

    suspend fun acceptChatRequest(currentUserId: String, otherUserId: String) {
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        db.collection("private_chats").document(chatRoomId)
            .update("status", "ACCEPTED")
            .await()
    }

    suspend fun markMessagesAsRead(currentUserId: String, otherUserId: String, messageIds: List<String>) {
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        val batch = db.batch()
        val messagesCollection = db.collection("private_chats").document(chatRoomId).collection("messages")

        messageIds.forEach { id ->
            batch.update(messagesCollection.document(id), "status", "READ")
        }
        batch.commit().await()
    }

    fun getChatRoomStatusStream(currentUserId: String, otherUserId: String): Flow<Pair<String?, String?>> {
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        return db.collection("private_chats").document(chatRoomId)
            .snapshots()
            .map { it.getString("status") to it.getString("requestedBy") }
    }

    fun getTypingStatusStream(currentUserId: String, otherUserId: String): Flow<Boolean> {
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        return db.collection("private_chats").document(chatRoomId)
            .snapshots()
            .map { snapshot ->
                val typingMap = snapshot.get("typing") as? Map<*, *>
                (typingMap?.get(otherUserId) ?: false) as Boolean
            }
    }

    suspend fun updateTypingStatus(currentUserId: String, otherUserId: String, isTyping: Boolean) {
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        db.collection("private_chats").document(chatRoomId)
            .update("typing.$currentUserId", isTyping)
            .await()
    }
}