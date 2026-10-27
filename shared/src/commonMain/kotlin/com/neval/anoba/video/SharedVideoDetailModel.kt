package com.neval.anoba.video

data class SharedVideoDetailModel(
    val id: String = "",
    val ownerId: String = "",
    val username: String = "",
    val userProfileImageUrl: String? = null,
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val title: String = "",
    val personalNote: String = "",
    val createdAtMillis: Long = 0L,
    val viewCount: Int = 0,
    val commentsCount: Int = 0,
    val isOfficial: Boolean = false,
    val userBadgeType: String? = null,
    val isPopular: Boolean = false,
    val isFlagged: Boolean = false,
    val flagReason: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val timeAgoText: String = ""
)
