package com.neval.anoba.video.videoemoji

data class VideoReactionUser(
    val userId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val emoji: String = "",
    val badgeType: String? = "NONE",
    val isOfficial: Boolean = false
)