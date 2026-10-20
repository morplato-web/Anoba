package com.neval.anoba.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val documentId: String = "",
    val uid: String = "",
    val displayName: String = "",
    val displayNameLowercase: String = "",
    val username: String? = null,
    val email: String = "",
    val photoURL: String? = null,
    val bio: String? = null,
    var roleValue: String? = "MEMBER",
    val createdAtMillis: Long? = null,
    val userNumber: Int? = null,
    val fcmToken: String? = null,
    val badgeType: String? = "NONE",
    var isOfficial: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val blockedUsers: List<String> = emptyList()
) {
    val role: UserRole
        get() = try {
            UserRole.valueOf(roleValue ?: "MEMBER")
        } catch (_: Exception) {
            UserRole.MEMBER
        }
}
