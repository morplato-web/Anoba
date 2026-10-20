package com.neval.anoba.ses.sescomment

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName 
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
@IgnoreExtraProperties
data class SesComment(
    @DocumentId
    val id: String = "",
    val sesId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileUrl: String? = null,
    val content: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val likedBy: List<String> = emptyList(),
    val replyToId: String? = null,

    // Firebase'in isOfficial ismini tam olarak tanıması için bu etiketleri ekliyoruz
    @get:PropertyName("isOfficial")
    @set:PropertyName("isOfficial")
    var isOfficial: Boolean = false,

    var userBadgeType: String? = "NONE"
)
