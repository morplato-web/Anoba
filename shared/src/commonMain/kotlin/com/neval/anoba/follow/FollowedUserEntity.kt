package com.neval.anoba.follow

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followed_users")
data class FollowedUserEntity(
    @PrimaryKey
    val followingId: String, // Takip edilen kullanıcının UID'si
    val createdAt: Long = 0L
)
