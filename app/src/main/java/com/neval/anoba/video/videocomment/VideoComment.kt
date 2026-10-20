package com.neval.anoba.video.videocomment

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
@IgnoreExtraProperties
data class VideoComment(
    @DocumentId
    val id: String = "",
    val videoId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileUrl: String? = null,
    val content: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val likedBy: List<String> = emptyList(),
    val replyToId: String? = null,

    // Firebase'in isOfficial ismini doğru tanıması için bu etiketler şarttır
    @get:PropertyName("isOfficial")
    @set:PropertyName("isOfficial")
    var isOfficial: Boolean = false,

    var userBadgeType: String? = "NONE"
)
