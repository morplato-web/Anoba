package com.neval.anoba.video

data class SharedVideoCommentModel(
    val id: String = "",
    val videoId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileUrl: String? = null,
    val userBadgeType: String? = null,
    val isOfficial: Boolean = false,
    val content: String = "",
    val timestampMillis: Long = 0L,
    val timeAgoText: String = "",
    val replyToId: String? = null,
    val likedBy: List<String> = emptyList()
)
