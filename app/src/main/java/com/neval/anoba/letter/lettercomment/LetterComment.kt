package com.neval.anoba.letter.lettercomment

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Anoba - Mektup Yorum Modeli
 * [id] Firestore doküman kimliği
 * [isOfficial] Kullanıcının Heyet olup olmadığını belirler (0xFFFFC107 Altın sarısı renk tetikleyici)
 */
@Keep
@IgnoreExtraProperties
data class LetterComment(
    @DocumentId
    val id: String = "",

    val letterId: String = "",

    val userId: String = "",

    val username: String = "",

    val userProfileUrl: String? = null,

    val content: String = "",

    @ServerTimestamp
    val timestamp: Date? = null,

    val likedBy: List<String> = emptyList(),

    val replyToId: String? = null,

    // Firebase'in isOfficial ismini (is takısından dolayı) doğru haritalaması için zorunludur:
    @get:PropertyName("isOfficial")
    @set:PropertyName("isOfficial")
    var isOfficial: Boolean = false,

    var userBadgeType: String? = "NONE"
) {
    // UI tarafında crash almamak için yardımcı bir özellik (Safe Date)
    val safeTimestamp: Long get() = timestamp?.time ?: System.currentTimeMillis()
}