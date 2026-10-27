package com.neval.anoba.ses

data class SharedSesDetailModel(
    val id: String = "",
    val ownerId: String = "",
    val username: String = "",
    val userProfileImageUrl: String? = null,
    val audioUrl: String = "",
    val title: String = "",
    val personalNote: String = "",
    val createdAtMillis: Long = 0L,
    val viewCount: Int = 0,
    val commentsCount: Int = 0,
    val durationText: String = "00:00",
    val durationMillis: Long = 0L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val isOfficial: Boolean = false,
    val userBadgeType: String? = null,
    val isPopular: Boolean = false,
    val isFlagged: Boolean = false,
    val flagReason: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val timeAgoText: String = ""
)
