package com.neval.anoba.chat.group

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
@IgnoreExtraProperties
data class ChatGroup(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var ownerId: String = "",
    @get:PropertyName("isPrivate")
    var isPrivate: Boolean = false,
    var members: List<String> = emptyList(),
    var imageUrl: String? = null,
    var typingUsers: Map<String, String> = emptyMap(),
    var lastMessage: String = "",
    @ServerTimestamp
    var createdAt: Date? = null
) {
    @Suppress("unused")
    constructor() : this("", "", "", "", false, emptyList(), null, emptyMap(), "", null)
}

@Suppress("unused")
fun ChatGroup.toSharedModel(): SharedChatGroup = SharedChatGroup(
    id = this.id,
    name = this.name,
    description = this.description,
    ownerId = this.ownerId,
    isPrivate = this.isPrivate,
    members = this.members,
    imageUrl = this.imageUrl,
    typingUsers = this.typingUsers,
    lastMessage = this.lastMessage,
    createdAtMillis = this.createdAt?.time ?: 0L
)

@Suppress("unused")
fun SharedChatGroup.toAndroidModel(): ChatGroup = ChatGroup(
    id = this.id,
    name = this.name,
    description = this.description,
    ownerId = this.ownerId,
    isPrivate = this.isPrivate,
    members = this.members,
    imageUrl = this.imageUrl,
    typingUsers = this.typingUsers,
    lastMessage = this.lastMessage,
    createdAt = if (this.createdAtMillis > 0L) Date(this.createdAtMillis) else null
)
