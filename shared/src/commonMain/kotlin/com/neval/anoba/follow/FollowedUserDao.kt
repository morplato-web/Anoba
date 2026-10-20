package com.neval.anoba.follow

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowedUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(followedUser: FollowedUserEntity)

    @Delete
    suspend fun deleteFollow(followedUser: FollowedUserEntity)

    @Query("SELECT * FROM followed_users")
    fun getAllFollowedUsers(): Flow<List<FollowedUserEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM followed_users WHERE followingId = :userId)")
    fun isFollowing(userId: String): Flow<Boolean>

    @Query("SELECT followingId FROM followed_users")
    suspend fun getFollowedUserIds(): List<String>

    @Query("DELETE FROM followed_users")
    suspend fun clearAll()
}
