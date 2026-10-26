package com.neval.anoba.login.state

sealed class AuthState {
    data class Authenticated(
        val uid: String,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
        val registrationDate: String?,
        val isAnonymous: Boolean,
        val isOfficial: Boolean = false,
        val badgeType: String? = "NONE",
        val followerCount: Int = 0,
        val followingCount: Int = 0,
        val isBanned: Boolean = false
    ) : AuthState()

    data object Unauthenticated : AuthState()
    data object Idle : AuthState() // Başlangıç durumu eklendi
    data object Loading : AuthState()
    data class Error(val message: String?) : AuthState()
}