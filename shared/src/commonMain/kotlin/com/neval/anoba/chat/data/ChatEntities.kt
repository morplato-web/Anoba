package com.neval.anoba.chat.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "general_messages",
    indices = [Index(value = ["timestampMillis"])],
)
data class GeneralChatMessageEntity(
    @PrimaryKey override val id: String,
    override val senderId: String,
    override val senderName: String,
    override val senderPhotoUrl: String? = null,
    override val content: String? = null,
    override val imageUrl: String? = null,
    override val videoUrl: String? = null,
    override val audioUrl: String? = null,
    override val audioDuration: Long? = null,
    override val senderBadgeType: String? = null,
    override val timestampMillis: Long = 0L,
    override val replyToId: String? = null,
    override val replyToName: String? = null,
    override val replyToContent: String? = null,
    override val status: String? = null
) : Messageable

@Entity(
    tableName = "group_messages",
    indices = [
        Index(value = ["groupId", "timestampMillis"]),
        Index(value = ["timestampMillis"])
    ]
)
data class GroupMessageEntity(
    @PrimaryKey override val id: String,
    val groupId: String,
    override val senderId: String,
    override val senderName: String,
    override val senderPhotoUrl: String? = null,
    override val content: String? = null,
    override val imageUrl: String? = null,
    override val videoUrl: String? = null,
    override val audioUrl: String? = null,
    override val audioDuration: Long? = null,
    override val senderBadgeType: String? = null,
    override val timestampMillis: Long = 0L,
    override val readBy: List<String> = emptyList(),
    override val replyToId: String? = null,
    override val replyToName: String? = null,
    override val replyToContent: String? = null,
    override val status: String? = null
) : Messageable

@Entity(
    tableName = "private_messages",
    indices = [
        Index(value = ["senderId", "receiverId", "timestampMillis"]),
        Index(value = ["receiverId", "senderId", "timestampMillis"]),
        Index(value = ["timestampMillis"])
    ]
)
data class PrivateMessageEntity(
    @PrimaryKey override val id: String,
    override val senderId: String,
    override val senderName: String,
    override val senderPhotoUrl: String? = null,
    val receiverId: String,
    val receiverName: String,
    override val content: String? = null,
    override val imageUrl: String? = null,
    override val videoUrl: String? = null,
    override val audioUrl: String? = null,
    override val audioDuration: Long? = null,
    override val senderBadgeType: String? = null,
    override val timestampMillis: Long = 0L,
    override val status: String = "SENT",
    override val replyToId: String? = null,
    override val replyToName: String? = null,
    override val replyToContent: String? = null
) : Messageable
