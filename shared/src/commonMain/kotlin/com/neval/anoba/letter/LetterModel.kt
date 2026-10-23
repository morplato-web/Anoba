package com.neval.anoba.letter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "letter_table")
data class LetterModel(
    @PrimaryKey var id: String = "",
    var ownerId: String = "",
    var username: String = "",
    var userProfileImageUrl: String? = null,
    var title: String? = null,
    var content: String = "",
    var commentsCount: Long = 0,
    var viewCount: Long = 0,
    var privacy: String = "PUBLIC",
    var unlockTimestampMillis: Long? = null,
    var audioUrl: String? = null,
    var audioDuration: Long? = null,
    var createdAtMillis: Long = 0L,
    var publishTimestampMillis: Long = 0L,

    @ColumnInfo(name = "is_published")
    var isPublished: Boolean = false,

    @ColumnInfo(name = "is_official")
    var isOfficial: Boolean = false,

    var userBadgeType: String? = "NONE",
    var topEmoji: String = "❤️",
    var reactionCount: Int = 0
)
