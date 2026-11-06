package com.neval.anoba.community.model

import kotlinx.serialization.Serializable

@Serializable
data class ServerModel(
    val id: String = "",
    val name: String = "",
    val iconUrl: String = "",
    val description: String = "",
    val ownerId: String = "",
    val createdAt: Long = 0L,
    val isPublic: Boolean = true,
    val memberCount: Int = 0
)
