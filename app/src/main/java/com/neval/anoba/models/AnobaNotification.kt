package com.neval.anoba.models

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
data class AnobaNotification(
    @get:Exclude var id: String = "",
    val type: String = "PHOTO",
    val title: String = "",
    val message: String = "",

    @ServerTimestamp
    val timestamp: Date? = null,

    @get:PropertyName("isRead") @set:PropertyName("isRead")
    var isRead: Boolean = false,

    val targetId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String? = null,

    // 🚀 GRUP VE BİREBİR SOHBET AYRIŞTIRMA ALANLARI
    val groupId: String? = null,
    @get:PropertyName("isGroup") @set:PropertyName("isGroup")
    var isGroup: Boolean = false
) {
    @get:Exclude
    val timestampLong: Long
        get() = timestamp?.time ?: System.currentTimeMillis()
}