package com.neval.anoba.common.services

import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import com.neval.anoba.login.AuthResult
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.models.User
import kotlinx.coroutines.flow.StateFlow

interface AuthServiceInterface {
    val authStateFlow: StateFlow<AuthState>

    fun refreshAuthState()
    fun logout()
    fun reloadAuthState()
    fun getCurrentUserId(): String
    fun getCurrentUserDisplayName(): String?
    fun getCurrentUserPhotoUrl(): String?
    fun getCurrentFirebaseUser(): FirebaseUser?
    fun isLoggedInFirebase(): Boolean

    suspend fun login(email: String, password: String, rememberMe: Boolean): AuthResult
    suspend fun register(email: String, password: String, username: String): AuthResult
    suspend fun loginAnonymously(): AuthResult
    suspend fun loginWithGoogle(idToken: String): AuthResult

    suspend fun getCurrentUser(): User?
    suspend fun getUserRole(): String
    suspend fun isRememberMe(): Boolean
    suspend fun setRememberMe(rememberMe: Boolean)
    suspend fun getUserById(userId: String): User?
    suspend fun resetPassword(email: String): Pair<Boolean, String?>
    suspend fun updatePassword(newPassword: String): Pair<Boolean, String?>
    suspend fun updateUserName(newName: String): Pair<Boolean, String?>
    suspend fun reauthenticateAndDelete(email: String, password: String): Pair<Boolean, String?>
    suspend fun updateUserProfilePicture(imageUri: Uri): Pair<Boolean, String?>

    // Onboarding durumu için yeni metodlar
    suspend fun isTutorialCompleted(): Boolean
    suspend fun setTutorialCompleted(completed: Boolean)
}