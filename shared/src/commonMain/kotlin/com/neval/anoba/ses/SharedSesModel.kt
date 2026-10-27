package com.neval.anoba.ses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "shared_ses_table")
data class SharedSesModel(
    @PrimaryKey var id: String = "",
    var ownerId: String = "",
    var username: String = "",
    var userProfileImageUrl: String? = null,
    var audioUrl: String = "",
    var title: String = "",
    var personalNote: String = "",
    var durationMillis: Long = 0L,
    var createdAtMillis: Long = 0L,
    var reactionCount: Int = 0,
    var commentsCount: Int = 0,
    var viewCount: Int = 0,
    var shareCount: Long = 0,

    @ColumnInfo(name = "is_official")
    var isOfficial: Boolean = false,

    var userBadgeType: String? = "NONE",
    var isLikedByMe: Boolean = false,
    var topEmoji: String = "❤️",
    var durationText: String = ""
)
