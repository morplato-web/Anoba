package com.neval.anoba.video

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Keep
@IgnoreExtraProperties
@Parcelize
@Entity(
    tableName = "video_table",
    indices = [Index(value = ["ownerId"])]
)
data class VideoModel(
    @PrimaryKey
    var id: String = "",
    var videoUrl: String = "",
    var ownerId: String = "",
    var username: String = "",
    var userProfileImageUrl: String? = null,
    
    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var createdAt: Date? = null,
    
    @ColumnInfo(name = "publish_timestamp")
    @get:PropertyName("publish_timestamp") @set:PropertyName("publish_timestamp")
    var publishTimestamp: Date? = null,
    
    var reactions: Map<String, Long> = emptyMap(),
    
    @Ignore
    var userReactions: Map<String, String> = emptyMap(),
    
    var commentsCount: Long = 0,
    var title: String? = null,
    var thumbnailUrl: String? = null,
    var duration: Long? = null,
    var viewCount: Long = 0,
    var shareCount: Long = 0,
    var personalNote: String = "",
    var countryCode: String? = null,
    
    @ColumnInfo(name = "is_published")
    @get:PropertyName("is_published") @set:PropertyName("is_published")
    var isPublished: Boolean = false,
    
    @ColumnInfo(name = "is_guide_content")
    @get:PropertyName("is_guide_content") @set:PropertyName("is_guide_content")
    var isGuideContent: Boolean = false,
    
    @ColumnInfo(name = "is_official")
    @get:PropertyName("is_official") @set:PropertyName("is_official")
    var isOfficial: Boolean = false,

    var userBadgeType: String? = "NONE",

    @ColumnInfo(name = "is_flagged")
    @get:PropertyName("is_flagged") @set:PropertyName("is_flagged")
    var isFlagged: Boolean = false,

    @ColumnInfo(name = "is_popular") // ⭐ 1000+ VİTRİN DURUMU
    @get:PropertyName("is_popular") @set:PropertyName("is_popular")
    var isPopular: Boolean = false,

    @get:PropertyName("flag_reason") @set:PropertyName("flag_reason")
    var flagReason: String? = null,

    @get:PropertyName("flagged_at") @set:PropertyName("flagged_at")
    var flaggedAt: Date? = null
) : Parcelable {
    constructor() : this("", "", "", "", null, null, null, emptyMap(), emptyMap(), 0, null, null, null, 0, 0, "", null, false, false, false, "NONE", false, false, null, null)
}
