package com.neval.anoba.community.model

import androidx.annotation.Keep

@Keep
data class ServerModel(
    val id: String = "",
    val name: String = "",
    val iconUrl: String = "",
    val description: String = "",
    val ownerId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = true,
    val memberCount: Int = 0
)
