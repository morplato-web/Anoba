package com.neval.anoba.photo.photoemoji

data class PhotoReactionUser(
    val userId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val emoji: String = "",
    val badgeType: String? = "NONE",
    val isOfficial: Boolean = false
)