package com.neval.anoba.chat.ui

interface Messageable {
    val id: String
    val senderId: String?
    val senderName: String?
    val senderPhotoUrl: String?
    val content: String?
    val timestampMillis: Long?
    val imageUrl: String?
    val videoUrl: String?
    val audioUrl: String?
    val audioDuration: Long?
    val senderBadgeType: String?
    val replyToId: String?
    val replyToName: String?
    val replyToContent: String?
    val readBy: List<String>? get() = null
    val status: String? get() = null
}
