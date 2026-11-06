package com.neval.anoba.community.model

import kotlinx.serialization.Serializable

@Serializable
data class VoiceMemberModel(
    val userId: String = "",
    val serverId: String = "",
    val channelId: String = "",
    val username: String = "",
    val userAvatar: String = "",
    val isMuted: Boolean = false,
    val isDeafened: Boolean = false,
    val isSpeaking: Boolean = false,
    val isStreaming: Boolean = false,
    val joinedAt: Long = 0L
)
