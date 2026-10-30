package com.neval.anoba.market

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Suppress("Unused", "unused")
class StringListTypeConverter {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("|||").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.asSequence()?.filter { it.isNotBlank() }?.joinToString("|||") ?: ""
    }
}

@Keep
@IgnoreExtraProperties
@Parcelize
@Entity(tableName = "market_table")
@TypeConverters(StringListTypeConverter::class)
data class MarketItemModel(
    @DocumentId
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
    var photoUrls: List<String> = emptyList(),
    var ownerId: String = "",
    var username: String = "",
    var userProfileImageUrl: String? = null,

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var createdAt: Date? = null,

    @ColumnInfo(name = "is_sold")
    @get:PropertyName("is_sold") @set:PropertyName("is_sold")
    var isSold: Boolean = false,

    @ColumnInfo(name = "is_official")
    @get:PropertyName("is_official") @set:PropertyName("is_official")
    var isOfficial: Boolean = false,

    var userBadgeType: String? = "NONE",
    var countryCode: String? = null,
) : Parcelable {
    constructor() : this("", "", "", 0.0, "₺", "Genel", "İkinci El", "", "", emptyList(), "", "", null, null, false, false, "NONE", null)
}
