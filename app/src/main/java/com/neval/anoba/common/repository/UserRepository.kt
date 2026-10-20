package com.neval.anoba.common.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.neval.anoba.common.datastore.DataStoreKeys
import com.neval.anoba.common.datastore.dataStore
import com.neval.anoba.models.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : IUserRepository {

    companion object {
        private const val TAG = "UserRepository"
    }

    override suspend fun getUserById(uid: String): User? {
        if (uid.isEmpty()) return null
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by ID: $uid", e)
            null
        }
    }

    override suspend fun findUserByUsername(username: String): User? {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("displayNameLowercase", username.lowercase())
                .limit(1)
                .get()
                .await()
            if (snapshot.isEmpty) {
                null
            } else {
                snapshot.documents[0].toObject(User::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding user by username: $username", e)
            null
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all users", e)
            emptyList()
        }
    }

    override fun getAllUsersStream(): Flow<List<User>> = callbackFlow {
        val listenerRegistration = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening for user changes", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.toObjects(User::class.java)
                    trySend(users).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun getSonNumara(uid: String): Int? {
        if (uid.isEmpty()) return null
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.getLong("sonNumara")?.toInt() ?: document.getString("userNumber")?.toIntOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sonNumara for user: $uid", e)
            null
        }
    }

    override suspend fun isRememberMe(): Boolean {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[DataStoreKeys.REMEMBER_ME_KEY] ?: false
            }.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading isRememberMe. Returning false.", e)
            false
        }
    }

    override suspend fun setRememberMe(rememberMe: Boolean) {
        try {
            context.dataStore.edit {
                it[DataStoreKeys.REMEMBER_ME_KEY] = rememberMe
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting rememberMe", e)
        }
    }

    override suspend fun uploadProfileImage(uid: String, imageUri: Uri): String? {
        return try {
            val storageRef = storage.reference.child("profile_images").child(uid).child("profile.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile image", e)
            null
        }
    }

    override suspend fun deleteProfileImage(uid: String): Boolean {
        return try {
            val storageRef = storage.reference.child("profile_images").child(uid).child("profile.jpg")
            storageRef.delete().await()
            true
        } catch (e: Exception) {
            if (e is com.google.firebase.storage.StorageException && e.errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND) {
                Log.d(TAG, "Profile image already deleted or never existed.")
                true
            } else {
                Log.e(TAG, "Error deleting profile image for user: $uid", e)
                false
            }
        }
    }

    override suspend fun updateUserProfilePhotoUrl(uid: String, photoUrl: String): Boolean {
        return try {
            val updates = mapOf(
                "photoURL" to photoUrl,
                "photoUrl" to photoUrl
            )
            firestore.collection("users").document(uid).set(updates, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile photo URL", e)
            false
        }
    }

    override suspend fun isTutorialCompleted(): Boolean {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[DataStoreKeys.TUTORIAL_COMPLETED_KEY] ?: false
            }.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading isTutorialCompleted. Returning false.", e)
            false
        }
    }

    override suspend fun setTutorialCompleted(completed: Boolean) {
        try {
            context.dataStore.edit {
                it[DataStoreKeys.TUTORIAL_COMPLETED_KEY] = completed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting tutorialCompleted", e)
        }
    }

    override suspend fun updateFcmToken(uid: String, token: String): Boolean {
        if (uid.isEmpty()) return false
        return try {
            firestore.collection("users").document(uid)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating FCM token for user: $uid", e)
            false
        }
    }

    override suspend fun clearFcmToken(uid: String): Boolean {
        if (uid.isEmpty()) return false
        return try {
            firestore.collection("users").document(uid)
                .update("fcmToken", FieldValue.delete())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing FCM token for user: $uid", e)
            false
        }
    }

    override suspend fun blockUser(currentUid: String, targetUid: String): Boolean {
        if (currentUid.isEmpty() || targetUid.isEmpty()) return false
        return try {
            firestore.collection("users").document(currentUid)
                .update("blockedUsers", FieldValue.arrayUnion(targetUid))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error blocking user: $targetUid", e)
            false
        }
    }

    override suspend fun unblockUser(currentUid: String, targetUid: String): Boolean {
        if (currentUid.isEmpty() || targetUid.isEmpty()) return false
        return try {
            firestore.collection("users").document(currentUid)
                .update("blockedUsers", FieldValue.arrayRemove(targetUid))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unblocking user: $targetUid", e)
            false
        }
    }

    override fun searchUsersStream(query: String): Flow<List<User>> = kotlinx.coroutines.flow.flow {
        if (query.isBlank()) {
            emit(emptyList())
            return@flow
        }
        try {
            val snapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("displayName", query)
                .whereLessThanOrEqualTo("displayName", query + "\uf8ff")
                .limit(10)
                .get()
                .await()
            emit(snapshot.toObjects(User::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users with query: $query", e)
            emit(emptyList())
        }
    }

    override suspend fun clearAllData() {
        try {
            context.dataStore.edit {
                it.clear()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing dataStore", e)
        }
    }
}