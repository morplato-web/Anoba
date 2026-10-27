package com.neval.anoba.ses

import kotlinx.serialization.Serializable

@Serializable
data class SharedSesCommentModel(
    val id: String = "",
    val sesId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileUrl: String? = null,
    val content: String = "",
    val timestampMillis: Long = 0L,
    val likedBy: List<String> = emptyList(),
    val replyToId: String? = null,
    val isOfficial: Boolean = false,
    val userBadgeType: String? = "NONE"
)
