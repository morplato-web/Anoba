package com.neval.anoba.market

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "market_table")
data class MarketItemModel(
    @PrimaryKey
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var currency: String = "₺",
    var category: String = "Genel",
    var condition: String = "İkinci El",
    var city: String = "",
    var photoUrl: String = "",
    var ownerId: String = "",
    var username: String = "",
    var userProfileImageUrl: String? = null,
    var createdAtMillis: Long = 0L,

    @ColumnInfo(name = "is_sold")
    var isSold: Boolean = false,

    @ColumnInfo(name = "is_official")
    var isOfficial: Boolean = false,

    var userBadgeType: String? = "NONE",
    var countryCode: String? = null
)
