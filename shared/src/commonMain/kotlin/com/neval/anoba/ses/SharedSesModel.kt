package com.neval.anoba.ses

data class SharedSesModel(
    val id: String = "",
    val ownerId: String = "",
    val username: String = "",
    val userProfileImageUrl: String? = null,
    val audioUrl: String = "",
    val title: String = "",
    val personalNote: String = "",
    val createdAtMillis: Long = 0L,
    val reactionCount: Int = 0,
    val commentsCount: Int = 0,
    val viewCount: Int = 0,
    val durationText: String = "00:00",
    val durationMillis: Long = 0L,
    val isOfficial: Boolean = false,
    val userBadgeType: String? = null,
    val isLikedByMe: Boolean = false
)
