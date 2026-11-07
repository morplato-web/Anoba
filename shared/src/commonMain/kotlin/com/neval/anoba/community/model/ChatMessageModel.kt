package com.neval.anoba.community.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageModel(
    val id: String = "",
    val serverId: String = "",
    val channelId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)
