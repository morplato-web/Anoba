package com.neval.anoba.chat.group

import android.net.Uri
import android.util.Log
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.ListenerRegistration
import com.neval.anoba.chat.general.GeneralChatUser
import com.neval.anoba.chat.data.ChatDao
import com.neval.anoba.chat.data.GroupMessageEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GroupChatRepository(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val chatDao: ChatDao
) {
    private val tag = "GroupChatRepository"
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val historyMutex = Mutex()
    private val activeListeners = mutableMapOf<String, ListenerRegistration>()

    fun getGroupsStreamForUser(userId: String): Flow<List<ChatGroup>> {
        val memberGroupsFlow = db.collection("groups")
            .whereArrayContains("members", userId)
            .dataObjects<ChatGroup>()

        val publicGroupsFlow = db.collection("groups")
            .whereEqualTo("isPrivate", false)
            .dataObjects<ChatGroup>()

        return combine(memberGroupsFlow, publicGroupsFlow) { memberGroups, publicGroups ->
            (memberGroups + publicGroups)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt }
        }
    }

    fun getMessagesPaging(groupId: String): Flow<PagingData<GroupMessage>> {
        startRealtimeSync(groupId)

        return chatDao.getGroupMessages(groupId).map { list ->
            PagingData.from(list.map { it.toDomain() })
        }
    }

    suspend fun loadOlderMessages(groupId: String, anchorTimestamp: Long?) {
        if (historyMutex.isLocked) return

        historyMutex.withLock {
            val referenceTimestamp = anchorTimestamp ?: chatDao.getOldestGroupMessageTimestamp(groupId) ?: return@withLock

            try {
                val snapshot = db.collection("groups").document(groupId)
                    .collection("messages")
                    .whereLessThan("timestamp", java.util.Date(referenceTimestamp))
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val messages = snapshot.toObjects(GroupMessage::class.java)
                    val entities = messages.map { it.toEntity(groupId) }
                    chatDao.insertGroupMessages(entities)
                    Log.d(tag, "Loaded ${entities.size} older messages for $groupId")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading older messages for $groupId", e)
            }
        }
    }

    private fun startRealtimeSync(groupId: String) {
        if (activeListeners.containsKey(groupId)) return

        val listener = db.collection("groups").document(groupId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Snapshot listener error for group $groupId", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.toObjects(GroupMessage::class.java)
                    val entities = messages.map { it.toEntity(groupId) }

                    repositoryScope.launch {
                        try {
                            chatDao.insertGroupMessages(entities)
                        } catch (e: Exception) {
                            Log.e(tag, "Error inserting group messages to DAO", e)
                        }
                    }
                }
            }

        activeListeners[groupId] = listener
    }

    fun stopRealtimeSync(groupId: String) {
        activeListeners[groupId]?.remove()
        activeListeners.remove(groupId)
    }

    private fun GroupMessage.toEntity(groupId: String) = GroupMessageEntity(
        id = this.id.ifBlank { UUID.randomUUID().toString() },
        groupId = groupId,
        senderId = this.senderId ?: "",
        senderName = this.senderName ?: "",
        senderPhotoUrl = this.senderPhotoUrl,
        content = this.content,
        imageUrl = this.imageUrl,
        videoUrl = this.videoUrl,
        audioUrl = this.audioUrl,
        audioDuration = this.audioDuration,
        senderBadgeType = this.senderBadgeType,
        timestampMillis = this.timestampMillis ?: System.currentTimeMillis(),
        readBy = this.readBy,
        replyToId = this.replyToId,
        replyToName = this.replyToName,
        replyToContent = this.replyToContent
    )

    private fun GroupMessageEntity.toDomain() = GroupMessage(
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
        readBy = this.readBy,
        replyToId = this.replyToId,
        replyToName = this.replyToName,
        replyToContent = this.replyToContent
    )

    suspend fun createGroup(group: ChatGroup): ChatGroup {
        val docRef = db.collection("groups").document()
        val newGroup = group.copy(id = docRef.id)
        docRef.set(newGroup).await()
        return newGroup
    }

    suspend fun addMessageToGroup(groupId: String, message: GroupMessage) {
        if (!message.senderId.isNullOrBlank()) {
            try {
                db.collection("groups").document(groupId)
                    .update("members", FieldValue.arrayUnion(message.senderId))
                    .await()
            } catch (e: Exception) {
                Log.w(tag, "Failed to update group members array", e)
            }
        }

        db.collection("groups").document(groupId)
            .collection("messages").add(message).await()

        repositoryScope.launch {
            try {
                val groupDoc = db.collection("groups").document(groupId).get().await()
                val groupName = groupDoc.getString("name") ?: "Grup"
                @Suppress("UNCHECKED_CAST")
                val members = groupDoc.get("members") as? List<String> ?: emptyList()

                val senderUsername = message.senderName?.ifBlank { "Bir üye" } ?: "Bir üye"
                val notifBody = when {
                    !message.content.isNullOrBlank() -> "$senderUsername: ${message.content}"
                    message.imageUrl != null -> "$senderUsername: 📷 Fotoğraf"
                    message.videoUrl != null -> "$senderUsername: 🎥 Video"
                    message.audioUrl != null -> "$senderUsername: 🎙️ Sesli Mesaj"
                    else -> "$senderUsername yeni bir mesaj gönderdi."
                }

                // 🚀 BİLDİRİM: type=CHAT korundu, isGroup=true ve groupId eklendi
                members.filter { it != message.senderId }.forEach { memberId ->
                    val notificationData = mapOf(
                        "type" to "CHAT",
                        "targetId" to groupId,
                        "groupId" to groupId,
                        "isGroup" to true,
                        "title" to groupName,
                        "senderName" to senderUsername,
                        "senderPhotoUrl" to (message.senderPhotoUrl ?: ""),
                        "message" to notifBody,
                        "body" to notifBody,
                        "isRead" to false,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(memberId)
                        .collection("notifications").add(notificationData)
                }
            } catch (e: Exception) {
                Log.w(tag, "Grup bildirimleri oluşturulurken hata", e)
            }
        }
    }

    suspend fun uploadImage(groupId: String, imageUri: Uri): String? {
        return try {
            val imageId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("chat_images/$groupId/$imageId")
            val uploadTask = storageRef.putFile(imageUri).await()
            uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Error uploading image to group $groupId", e)
            null
        }
    }

    suspend fun uploadVideo(groupId: String, videoUri: Uri): String? {
        return try {
            val videoId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("chat_videos/$groupId/$videoId")
            val uploadTask = storageRef.putFile(videoUri).await()
            uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Error uploading video to group $groupId", e)
            null
        }
    }

    suspend fun uploadAudio(groupId: String, audioUri: Uri): String? {
        return try {
            val audioId = "${UUID.randomUUID()}.m4a"
            val storageRef = storage.reference.child("chat_audio/$groupId/$audioId")
            val uploadTask = storageRef.putFile(audioUri).await()
            uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Error uploading audio to group $groupId", e)
            null
        }
    }

    suspend fun editMessageContent(groupId: String, messageId: String, newContent: String) {
        db.collection("groups").document(groupId)
            .collection("messages").document(messageId)
            .update("content", newContent).await()
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

    suspend fun deleteMessageFromGroup(groupId: String, message: GroupMessage) {
        message.imageUrl?.let { deleteFileFromStorage(it) }
        message.videoUrl?.let { deleteFileFromStorage(it) }
        message.audioUrl?.let { deleteFileFromStorage(it) }

        db.collection("groups").document(groupId)
            .collection("messages").document(message.id).delete().await()

        chatDao.deleteGroupMessageById(message.id)
    }

    suspend fun deleteGroup(groupId: String) {
        db.collection("groups").document(groupId).delete().await()
    }

    suspend fun getGroupDetails(groupId: String): ChatGroup? {
        return try {
            db.collection("groups").document(groupId).get().await().toObject<ChatGroup>()
        } catch (e: Exception) {
            Log.e(tag, "Error getting group details for $groupId", e)
            null
        }
    }

    suspend fun getGroupMemberDetails(memberIds: List<String>): List<GeneralChatUser> {
        if (memberIds.isEmpty()) return emptyList()
        val users = mutableListOf<GeneralChatUser>()
        try {
            memberIds.chunked(10).forEach { chunk ->
                val snapshot = db.collection("users").whereIn("uid", chunk).get().await()
                users.addAll(snapshot.toObjects(GeneralChatUser::class.java))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching user details for members", e)
        }
        return users
    }

    suspend fun addUserToGroup(groupId: String, userId: String) {
        db.collection("groups").document(groupId).update("members", FieldValue.arrayUnion(userId)).await()
    }

    suspend fun removeUserFromGroup(groupId: String, userId: String) {
        db.collection("groups").document(groupId).update("members", FieldValue.arrayRemove(userId)).await()
    }

    suspend fun setGroupPrivacy(groupId: String, isPrivate: Boolean) {
        db.collection("groups").document(groupId).update("isPrivate", isPrivate).await()
    }

    suspend fun updateGroupInfo(groupId: String, newName: String, newDescription: String) {
        val updates = mapOf(
            "name" to newName,
            "description" to newDescription
        )
        db.collection("groups").document(groupId).update(updates).await()
    }

    suspend fun uploadGroupImage(groupId: String, imageUri: Uri): String {
        val storageRef = storage.reference.child("group_images/$groupId/${UUID.randomUUID()}")
        val uploadTask = storageRef.putFile(imageUri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    suspend fun updateGroupImageUrl(groupId: String, imageUrl: String) {
        db.collection("groups").document(groupId).update("imageUrl", imageUrl).await()
    }

    suspend fun updateTypingStatus(groupId: String, userId: String, userName: String, isTyping: Boolean) {
        val typingUpdate = if (isTyping) {
            mapOf("typingUsers.$userId" to userName)
        } else {
            mapOf("typingUsers.$userId" to FieldValue.delete())
        }
        db.collection("groups").document(groupId).update(typingUpdate).await()
    }

    suspend fun markMessagesAsReadBatch(groupId: String, messageIds: List<String>, userId: String) {
        if (messageIds.isEmpty()) return
        try {
            messageIds.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { messageId ->
                    val messageRef = db.collection("groups").document(groupId)
                        .collection("messages").document(messageId)
                    batch.update(messageRef, "readBy", FieldValue.arrayUnion(userId))
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            Log.e(tag, "Toplu okundu işaretleme hatası", e)
        }
    }

    suspend fun addMessagesToGroup(groupId: String, messages: List<GroupMessage>) {
        val batch = db.batch()
        val messagesCollection = db.collection("groups").document(groupId).collection("messages")

        messages.forEach { message ->
            val docRef = messagesCollection.document(message.id)
            batch.set(docRef, message)
        }
        batch.commit().await()
    }

    @Suppress("UNUSED_PARAMETER")
    fun muteGroup(groupId: String, userId: String) { }

    @Suppress("UNUSED_PARAMETER")
    fun unmuteGroup(groupId: String, userId: String) { }
}