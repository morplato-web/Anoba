package com.neval.anoba.chat.group

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.neval.anoba.chat.ui.Messageable
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
