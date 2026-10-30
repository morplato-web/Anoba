package com.neval.anoba.letter

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Keep
@IgnoreExtraProperties
@Parcelize
@Entity(tableName = "letter_table")
data class LetterModel(
    @PrimaryKey
    var id: String = "",
    var ownerId: String = "",
    var username: String = "",
    var userProfileImageUrl: String? = null,
    var title: String? = null,
    var content: String = "",
    var commentsCount: Long = 0,

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var createdAt: Date? = null,

    @ColumnInfo(name = "publish_timestamp")
    @get:PropertyName("publish_timestamp") @set:PropertyName("publish_timestamp")
    var publishTimestamp: Date? = null,

    var reactions: Map<String, Long> = mapOf(),
    @Ignore
    var userReactions: Map<String, String> = mapOf(),
    
    var viewCount: Long = 0,
    var shareCount: Long = 0,
    var privacy: String = "PUBLIC",
    var recipientId: String? = null,
    var recipientUsername: String? = null,
    var personalNote: String = "",
    var unlockTimestamp: Long? = null, 
    var countryCode: String? = null,
    var anobaResponse: String? = null,

    var audioUrl: String? = null,
    var audioDuration: Long? = null,

    var pseudonym: String? = null, 
    
    @ColumnInfo(name = "is_ask_anoba")
    @get:PropertyName("is_ask_anoba") @set:PropertyName("is_ask_anoba")
    var isAskAnoba: Boolean = false, 

    @ColumnInfo(name = "is_content_hidden")
    @get:PropertyName("is_content_hidden") @set:PropertyName("is_content_hidden")
    var isContentHidden: Boolean = false,

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
    constructor() : this(
        id = "", ownerId = "", username = "", userProfileImageUrl = null, title = null,
        content = "", commentsCount = 0, createdAt = null, publishTimestamp = null,
        reactions = mapOf(), userReactions = mapOf(), viewCount = 0, shareCount = 0,
        privacy = "PUBLIC", recipientId = null, recipientUsername = null,
        personalNote = "", unlockTimestamp = null, countryCode = null,
        anobaResponse = null, audioUrl = null, audioDuration = null,
        pseudonym = null, isAskAnoba = false, isContentHidden = false, isPublished = false,
        isGuideContent = false, isOfficial = false, userBadgeType = "NONE",
        isFlagged = false, isPopular = false, flagReason = null, flaggedAt = null
    )
}

fun LetterModel.toSharedModel(): SharedLetterModel {
    return SharedLetterModel(
        id = this.id,
        ownerId = this.ownerId,
        username = this.username,
        userProfileImageUrl = this.userProfileImageUrl,
        title = this.title,
        content = this.content,
        commentsCount = this.commentsCount,
        viewCount = this.viewCount,
        privacy = this.privacy,
        unlockTimestampMillis = this.unlockTimestamp,
        audioUrl = this.audioUrl,
        audioDuration = this.audioDuration,
        createdAtMillis = this.createdAt?.time ?: 0L,
        publishTimestampMillis = this.publishTimestamp?.time ?: 0L,
        isPublished = this.isPublished,
        isOfficial = this.isOfficial,
        userBadgeType = this.userBadgeType ?: "NONE",
        topEmoji = this.reactions.maxByOrNull { it.value }?.key ?: "❤️",
        reactionCount = this.reactions.values.sum().toInt()
    )
}
