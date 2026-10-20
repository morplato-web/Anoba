package com.neval.anoba.chat.general

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.neval.anoba.chat.ui.Messageable
import java.util.Date

@Keep
@IgnoreExtraProperties
data class GeneralChatMessage(
    @DocumentId
    val documentId: String = "",

    override val senderId: String? = null,
    override val content: String? = null,
    override val senderName: String? = null,
    override val senderPhotoUrl: String? = null,
    @ServerTimestamp val timestamp: Date? = null,
    override val imageUrl: String? = null,
    override val videoUrl: String? = null,
    override val audioUrl: String? = null,
    override val senderBadgeType: String? = null,
    override val audioDuration: Long? = null,
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
