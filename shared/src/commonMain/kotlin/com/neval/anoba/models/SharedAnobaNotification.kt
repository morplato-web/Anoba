package com.neval.anoba.models

import kotlinx.serialization.Serializable

@Serializable
data class SharedAnobaNotification(
    val id: String = "",
    val type: String = "PHOTO",
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val targetId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String? = null,
    val timestampMillis: Long = 0L,
    val groupId: String? = null,
    val isGroup: Boolean = false,
)
