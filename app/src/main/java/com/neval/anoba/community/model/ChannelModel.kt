package com.neval.anoba.community.model

import androidx.annotation.Keep

@Keep
enum class ChannelType {
    TEXT,
    VOICE,
    STAGE
}

@Keep
data class ChannelModel(
    val id: String = "",
    val serverId: String = "",
    val name: String = "",
    val type: ChannelType = ChannelType.TEXT,
    val topic: String = "",
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
