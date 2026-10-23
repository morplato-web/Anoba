package com.neval.anoba.home

import com.neval.anoba.market.MarketItemModel
import kotlinx.serialization.Serializable

@Serializable
enum class ContentType {
    PHOTO,
    LETTER,
    AUDIO,
    VIDEO,
    MARKET
}

@Serializable
data class SharedFeedItem(
    val id: String,
    val type: ContentType,
    val title: String,
    val description: String,
    val mediaUrl: String? = null,
    val ownerId: String,
    val username: String,
    val userProfileImageUrl: String? = null,
    val userBadgeType: String? = "NONE",
    val isOfficial: Boolean = false,
    val topEmoji: String = "❤️",
    val reactionCount: Int = 0,
    val viewCount: Long = 0L,
    val createdAtMillis: Long = 0L,
    val marketItem: MarketItemModel? = null
)
