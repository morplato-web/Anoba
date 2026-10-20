package com.neval.anoba.follow

import com.neval.anoba.models.User
import kotlinx.coroutines.flow.Flow

interface FollowRepository {
    fun isFollowing(targetUserId: String): Flow<Boolean>
    fun getFollowedUserIdsFlow(): Flow<Set<String>>
    suspend fun getUsersDetails(userIds: List<String>): List<User>
    suspend fun getFollowerIds(userId: String): List<String>
    suspend fun getFollowingIds(): List<String>
    suspend fun followUser(targetUserId: String, senderUsername: String, senderPhotoUrl: String?)
    suspend fun unfollowUser(targetUserId: String)
    suspend fun syncFollowsFromFirebase()
}
