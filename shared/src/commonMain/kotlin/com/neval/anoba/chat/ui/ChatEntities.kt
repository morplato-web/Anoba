package com.neval.anoba.chat.ui

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "general_messages")
data class GeneralChatMessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val senderName: String,
    val senderPhotoUrl: String?,
    val content: String?,
    val imageUrl: String?,
    val videoUrl: String?,
    val audioUrl: String?,
    val audioDuration: Long?,
    val senderBadgeType: String? = null,
    val timestampMillis: Long,
    val replyToId: String? = null,
    val replyToName: String? = null,
    val replyToContent: String? = null
)

@Entity(tableName = "group_messages")
data class GroupMessageEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val senderId: String,
    val senderName: String,
    val senderPhotoUrl: String?,
    val content: String?,
    val imageUrl: String?,
    val videoUrl: String?,
    val audioUrl: String?,
    val audioDuration: Long?,
    val senderBadgeType: String? = null,
    val timestampMillis: Long,
    val readBy: List<String> = emptyList(),
    val replyToId: String? = null,
    val replyToName: String? = null,
    val replyToContent: String? = null
)

@Entity(tableName = "private_messages")
data class PrivateMessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val senderName: String,
    val senderPhotoUrl: String?,
    val receiverId: String,
    val receiverName: String,
    val content: String?,
    val imageUrl: String?,
    val videoUrl: String?,
    val audioUrl: String?,
    val audioDuration: Long?,
    val senderBadgeType: String? = null,
    val timestampMillis: Long,
    val status: String,
    val replyToId: String? = null,
    val replyToName: String? = null,
    val replyToContent: String? = null
)
