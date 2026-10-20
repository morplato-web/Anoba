package com.neval.anoba.ses.sesemoji

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class SesReaction(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val sesId: String = "",
    val emoji: String = "",
    val displayName: String = "",
    val userPhotoUrl: String? = null,
    val userBadgeType: String? = "NONE",
    @get:PropertyName("isOfficial")
    @set:PropertyName("isOfficial")
    var isOfficial: Boolean = false,
    @ServerTimestamp
    val timestamp: Date? = null
)
