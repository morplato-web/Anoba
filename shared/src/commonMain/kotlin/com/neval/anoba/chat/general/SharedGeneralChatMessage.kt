package com.neval.anoba.chat.general

import com.neval.anoba.chat.data.Messageable

data class SharedGeneralChatMessage(
    override val id: String = "",
    override val senderId: String? = null,
    override val senderName: String? = null,
    override val senderPhotoUrl: String? = null,
    override val content: String? = null,
    override val timestampMillis: Long? = null,
    override val imageUrl: String? = null,
    override val videoUrl: String? = null,
    override val audioUrl: String? = null,
    override val senderBadgeType: String? = null,
    override val audioDuration: Long? = null,
    override val replyToId: String? = null,
    override val replyToName: String? = null,
    override val replyToContent: String? = null,
    override val status: String? = null,
    override val readBy: List<String>? = null
) : Messageable