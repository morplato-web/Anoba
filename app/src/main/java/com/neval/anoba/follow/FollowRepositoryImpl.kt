package com.neval.anoba.follow

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.neval.anoba.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FollowRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val followedUserDao: FollowedUserDao
) : FollowRepository {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun isFollowing(targetUserId: String): Flow<Boolean> {
        return followedUserDao.isFollowing(targetUserId)
    }

    override fun getFollowedUserIdsFlow(): Flow<Set<String>> {
        return followedUserDao.getAllFollowedUsers().map { list -> list.map { it.followingId }.toSet() }
    }

    override suspend fun getUsersDetails(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()
        
        return userIds.chunked(30).flatMap { chunk ->
            try {
                firestore.collection("users")
                    .whereIn("uid", chunk)
                    .get()
                    .await()
                    .toObjects(User::class.java)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getFollowerIds(userId: String): List<String> {
        return try {
            firestore.collection("follows")
                .whereEqualTo("followingId", userId)
                .get()
                .await()
                .documents.mapNotNull { it.getString("followerId") }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getFollowingIds(): List<String> {
        return followedUserDao.getFollowedUserIds()
    }

    override suspend fun followUser(targetUserId: String, senderUsername: String, senderPhotoUrl: String?) {
        val uid = currentUserId ?: return
        if (uid == targetUserId) return

        val followRef = firestore.collection("follows").document("${uid}_${targetUserId}")
        val currentUserRef = firestore.collection("users").document(uid)
        val targetUserRef = firestore.collection("users").document(targetUserId)

        try {
            firestore.runTransaction { transaction ->
                transaction.set(followRef, mapOf(
                    "followerId" to uid,
                    "followingId" to targetUserId,
                    "createdAt" to FieldValue.serverTimestamp()
                ))
                
                transaction.update(currentUserRef, "followingCount", FieldValue.increment(1))
                transaction.update(targetUserRef, "followerCount", FieldValue.increment(1))
            }.await()
        } catch (_: Exception) {
            followRef.set(mapOf(
                "followerId" to uid,
                "followingId" to targetUserId,
                "createdAt" to FieldValue.serverTimestamp()
            )).await()
        }

        try {
            followedUserDao.insertFollow(FollowedUserEntity(targetUserId))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val usernameClean = senderUsername.ifBlank { "Bir kullanıcı" }
            val notificationData = mapOf(
                "type" to "FOLLOW",
                "targetId" to uid,
                "title" to usernameClean,
                "senderName" to usernameClean,
                "senderPhotoUrl" to (senderPhotoUrl ?: ""),
                "message" to "seni takip etmeye başladı.",
                "body" to "$usernameClean seni takip etmeye başladı.",
                "isRead" to false,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(targetUserId)
                .collection("notifications").add(notificationData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun unfollowUser(targetUserId: String) {
        val uid = currentUserId ?: return

        val followRef = firestore.collection("follows").document("${uid}_${targetUserId}")
        val currentUserRef = firestore.collection("users").document(uid)
        val targetUserRef = firestore.collection("users").document(targetUserId)

        try {
            firestore.runTransaction { transaction ->
                transaction.delete(followRef)
                transaction.update(currentUserRef, "followingCount", FieldValue.increment(-1))
                transaction.update(targetUserRef, "followerCount", FieldValue.increment(-1))
            }.await()
        } catch (_: Exception) {
            followRef.delete().await()
        }

        try {
            followedUserDao.deleteFollow(FollowedUserEntity(targetUserId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun syncFollowsFromFirebase() {
        val uid = currentUserId ?: return
        try {
            val snapshot = firestore.collection("follows")
                .whereEqualTo("followerId", uid)
                .get()
                .await()

            val followedEntities = snapshot.documents.mapNotNull { doc ->
                val targetId = doc.getString("followingId")
                if (targetId != null) FollowedUserEntity(targetId) else null
            }

            followedUserDao.clearAll()
            followedEntities.forEach { followedUserDao.insertFollow(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
