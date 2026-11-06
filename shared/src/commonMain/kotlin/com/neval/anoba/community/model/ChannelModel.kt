package com.neval.anoba.community.model

import kotlinx.serialization.Serializable

@Serializable
enum class ChannelType {
    TEXT,
    VOICE,
    STAGE
}

@Serializable
data class ChannelModel(
    val id: String = "",
    val serverId: String = "",
    val name: String = "",
    val type: ChannelType = ChannelType.TEXT,
    val topic: String = "",
    val position: Int = 0,
    val createdAt: Long = 0L
)
