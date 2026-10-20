package com.neval.anoba.photo.photocomment

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
@IgnoreExtraProperties
data class PhotoComment(
    @DocumentId
    val id: String = "",
    val photoId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileUrl: String? = null,
    val content: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val likedBy: List<String> = emptyList(),
    val replyToId: String? = null,

    @get:PropertyName("isOfficial")
    @set:PropertyName("isOfficial")
    var isOfficial: Boolean = false,

    var userBadgeType: String? = "NONE"
)
