package com.neval.anoba.chat.group

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.neval.anoba.chat.data.Messageable
import com.neval.anoba.chat.general.SharedChatMessage
import java.util.Date

@Keep
@IgnoreExtraProperties
data class GroupMessage(
    @DocumentId
    val documentId: String = "",

    val groupId: String = "",
    override val senderId: String? = null,
    override val content: String? = null,
    override val senderName: String? = null,
    override val senderPhotoUrl: String? = null,

    override val imageUrl: String? = null,
    override val videoUrl: String? = null,
    override val audioUrl: String? = null,
    override val senderBadgeType: String? = null,
    override val audioDuration: Long? = null,

    @ServerTimestamp
    val timestamp: Date? = null,
    override val readBy: List<String> = emptyList(),
    override val status: String? = null,

    override val replyToId: String? = null,
    override val replyToName: String? = null,
    override val replyToContent: String? = null
) : Messageable {

    @get:Exclude
    override val id: String
        get() = documentId

    @get:Exclude
    override val timestampMillis: Long?
        get() = timestamp?.time
}

@Suppress("unused")
fun GroupMessage.toSharedModel(): SharedChatMessage = SharedChatMessage(
    id = this.id,
    senderId = this.senderId ?: "",
    senderName = this.senderName ?: "",
    senderPhotoUrl = this.senderPhotoUrl,
    senderBadgeType = this.senderBadgeType,
    content = this.content,
    imageUrl = this.imageUrl,
    videoUrl = this.videoUrl,
    audioUrl = this.audioUrl,
    audioDuration = this.audioDuration,
    timestampMillis = this.timestampMillis ?: 0L,
    replyToId = this.replyToId,
    replyToName = this.replyToName,
    replyToContent = this.replyToContent,
    status = this.status
)

@Suppress("unused")
fun SharedChatMessage.toAndroidGroupMessage(groupId: String = ""): GroupMessage = GroupMessage(
    documentId = this.id,
    groupId = groupId,
    senderId = this.senderId,
    senderName = this.senderName,
    senderPhotoUrl = this.senderPhotoUrl,
    senderBadgeType = this.senderBadgeType,
    content = this.content,
    imageUrl = this.imageUrl,
    videoUrl = this.videoUrl,
    audioUrl = this.audioUrl,
    audioDuration = this.audioDuration,
    timestamp = if (this.timestampMillis > 0L) Date(this.timestampMillis) else null,
    replyToId = this.replyToId,
    replyToName = this.replyToName,
    replyToContent = this.replyToContent,
    status = this.status
)
