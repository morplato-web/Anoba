package com.neval.anoba.letter.letteremoji

data class LetterReactionUser(
    val userId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val emoji: String = "",
    val badgeType: String? = "NONE",
    val isOfficial: Boolean = false
)
