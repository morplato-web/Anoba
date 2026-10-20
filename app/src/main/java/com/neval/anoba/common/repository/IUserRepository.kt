package com.neval.anoba.common.repository

import android.net.Uri
import com.neval.anoba.models.User
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    suspend fun getUserById(uid: String): User?
    suspend fun getAllUsers(): List<User>
    fun getAllUsersStream(): Flow<List<User>>
    suspend fun findUserByUsername(username: String): User?
    suspend fun getSonNumara(uid: String): Int?
    suspend fun isRememberMe(): Boolean
    suspend fun setRememberMe(rememberMe: Boolean)
    suspend fun uploadProfileImage(uid: String, imageUri: Uri): String?
    suspend fun deleteProfileImage(uid: String): Boolean
    suspend fun updateUserProfilePhotoUrl(uid: String, photoUrl: String): Boolean
    
    // İnteraktif Tur (Onboarding) için yeni metodlar
    suspend fun isTutorialCompleted(): Boolean
    suspend fun setTutorialCompleted(completed: Boolean)

    // FCM Bildirimleri
    suspend fun updateFcmToken(uid: String, token: String): Boolean
    suspend fun clearFcmToken(uid: String): Boolean

    suspend fun blockUser(currentUid: String, targetUid: String): Boolean
    suspend fun unblockUser(currentUid: String, targetUid: String): Boolean

    fun searchUsersStream(query: String): Flow<List<User>>

    suspend fun clearAllData()
}
