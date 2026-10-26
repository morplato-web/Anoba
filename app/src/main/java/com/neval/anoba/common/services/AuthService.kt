package com.neval.anoba.common.services

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.neval.anoba.R
import com.neval.anoba.common.database.AppDatabase
import com.neval.anoba.common.repository.IUserRepository
import com.neval.anoba.letter.LetterRepository
import com.neval.anoba.login.AuthResult
import com.neval.anoba.login.state.AuthState
import com.neval.anoba.models.User
import com.neval.anoba.models.UserRole
import com.neval.anoba.photo.PhotoRepository
import com.neval.anoba.ses.SesRepository
import com.neval.anoba.video.VideoRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthService(
    private val appUserRepository: IUserRepository,
    private val coroutineScope: CoroutineScope,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context,
    private val photoRepository: PhotoRepository,
    private val videoRepository: VideoRepository,
    private val sesRepository: SesRepository,
    private val letterRepository: LetterRepository,
    private val appDatabase: AppDatabase
) : AuthServiceInterface {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authStateFlow: StateFlow<AuthState> = _authState.asStateFlow()

    private var isRegistering = false
    private var userListenerRegistration: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        coroutineScope.launch {
            if (isRegistering) return@launch

            if (firebaseUser != null) {
                Log.d("AuthService", "Kullanıcı giriş yapmış: ${firebaseUser.uid}")
                try {
                    withTimeoutOrNull(5000) { firebaseUser.reload().await() }
                } catch (_: Exception) {}

                val regDateString = getRegistrationDateString(firebaseUser)
                val initialName = if (firebaseUser.isAnonymous) {
                    firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: context.getString(R.string.guest)
                } else {
                    firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: context.getString(R.string.state_loading)
                }

                val initialAuthState = AuthState.Authenticated(
                    uid = firebaseUser.uid,
                    displayName = initialName,
                    email = firebaseUser.email,
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    isAnonymous = firebaseUser.isAnonymous,
                    registrationDate = regDateString,
                    isOfficial = false
                )
                _authState.value = initialAuthState

                // 🚀 Canlı Dinleyici (Real-time Snapshot Listener):
                // Firebase Console'dan 'badgeType', 'displayName' vb. değiştirildiği an
                // uygulama yeniden başlatılmaya gerek kalmadan hafızadaki yetki durumunu anında günceller.
                userListenerRegistration?.remove()
                userListenerRegistration = firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { fsDoc, error ->
                        if (error != null || fsDoc == null || !fsDoc.exists()) return@addSnapshotListener
                        val currentName = fsDoc.getString("displayName") ?: fsDoc.getString("username") ?: initialName
                        val isOfficial = fsDoc.getBoolean("isOfficial") ?: false
                        val badge = fsDoc.getString("badgeType") ?: "NONE"
                        val followers = fsDoc.getLong("followerCount")?.toInt() ?: 0
                        val following = fsDoc.getLong("followingCount")?.toInt() ?: 0
                        val isBanned = fsDoc.getBoolean("isBanned") ?: fsDoc.getBoolean("is_banned") ?: false

                        val firestorePhoto = fsDoc.getString("photoURL") ?: fsDoc.getString("photoUrl")
                        val finalPhotoUrl = firestorePhoto ?: firebaseUser.photoUrl?.toString()

                        _authState.value = initialAuthState.copy(
                            displayName = currentName,
                            photoUrl = finalPhotoUrl,
                            isOfficial = isOfficial,
                            badgeType = badge,
                            followerCount = followers,
                            followingCount = following,
                            isBanned = isBanned
                        )
                    }
            } else {
                userListenerRegistration?.remove()
                userListenerRegistration = null
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun refreshAuthState() { reloadAuthState() }

    override fun logout() {
        coroutineScope.launch {
            try {
                val currentUid = auth.currentUser?.uid
                if (currentUid != null) {
                    try {
                        firestore.collection("users").document(currentUid)
                            .update("fcmToken", FieldValue.delete())
                            .await()
                    } catch (_: Exception) {}
                }

                userListenerRegistration?.remove()
                userListenerRegistration = null
                auth.signOut()
                _authState.value = AuthState.Unauthenticated

                withContext(Dispatchers.IO) {
                    appUserRepository.clearAllData()
                    appDatabase.chatDao().clearAllMessages()
                }
            } catch (_: Exception) { }
        }
    }

    private fun getErrorMessage(e: Exception): String {
        return if (e is FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> context.getString(R.string.error_invalid_email)
                "ERROR_WRONG_PASSWORD" -> context.getString(R.string.error_wrong_password)
                "ERROR_USER_NOT_FOUND" -> context.getString(R.string.error_user_not_found)
                "ERROR_USER_DISABLED" -> context.getString(R.string.error_user_disabled)
                "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.error_too_many_requests)
                "ERROR_EMAIL_ALREADY_IN_USE" -> context.getString(R.string.error_email_already_in_use)
                "ERROR_WEAK_PASSWORD" -> context.getString(R.string.error_weak_password)
                else -> e.localizedMessage ?: context.getString(R.string.auth_generic_fail)
            }
        } else {
            if (e.message?.contains("network", ignoreCase = true) == true) {
                context.getString(R.string.error_network_request_failed)
            } else {
                e.localizedMessage ?: context.getString(R.string.auth_generic_fail)
            }
        }
    }

    override suspend fun login(email: String, password: String, rememberMe: Boolean): AuthResult {
        _authState.value = AuthState.Loading
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return AuthResult.Error(message = context.getString(R.string.auth_user_object_null))
            appUserRepository.setRememberMe(rememberMe)
            reloadAuthState()
            AuthResult.Success(firebaseUser = firebaseUser, isAnonymous = false)
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            AuthResult.Error(message = getErrorMessage(e))
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResult {
        _authState.value = AuthState.Loading
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return AuthResult.Error(message = context.getString(R.string.auth_user_object_null))
            appUserRepository.setRememberMe(true)

            val baseName = firebaseUser.displayName?.ifBlank { "User" } ?: "User"
            val existingDoc = firestore.collection("users").document(firebaseUser.uid).get().await()

            if (!existingDoc.exists()) {
                val finalUsername = generateUniqueUsername(baseName)
                val newUser = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = finalUsername,
                    displayNameLowercase = finalUsername.lowercase(),
                    username = finalUsername,
                    photoURL = firebaseUser.photoUrl?.toString(),
                    roleValue = UserRole.MEMBER.name,
                    createdAtMillis = System.currentTimeMillis()
                )
                firestore.collection("users").document(firebaseUser.uid).set(newUser, SetOptions.merge()).await()
            }

            reloadAuthState()
            AuthResult.Success(firebaseUser = firebaseUser, isAnonymous = false)
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            AuthResult.Error(message = getErrorMessage(e))
        }
    }

    override suspend fun loginAnonymously(): AuthResult {
        _authState.value = AuthState.Loading
        return try {
            val result = auth.signInAnonymously().await()
            val firebaseUser = result.user ?: return AuthResult.Error(message = context.getString(R.string.auth_user_object_null))
            appUserRepository.setRememberMe(false)

            val baseGuestName = context.getString(R.string.guest)
            val finalGuestName = generateUniqueUsername(baseGuestName)

            val profileUpdate = UserProfileChangeRequest.Builder().setDisplayName(finalGuestName).build()
            firebaseUser.updateProfile(profileUpdate).await()

            val newUser = User(
                uid = firebaseUser.uid,
                email = "",
                displayName = finalGuestName,
                displayNameLowercase = finalGuestName.lowercase(),
                username = finalGuestName,
                roleValue = UserRole.GUEST.name,
                createdAtMillis = System.currentTimeMillis()
            )

            firestore.collection("users").document(firebaseUser.uid).set(newUser, SetOptions.merge()).await()

            reloadAuthState()
            AuthResult.Success(firebaseUser = firebaseUser, isAnonymous = true)
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            AuthResult.Error(message = getErrorMessage(e))
        }
    }

    private suspend fun generateUniqueUsername(baseUsername: String): String {
        val cleanedBase = baseUsername.trim().ifBlank { "User" }
        val usernamesCollection = firestore.collection("kullanici_adlari")
        val countersCollection = firestore.collection("ad_numaralari")

        return try {
            firestore.runTransaction { transaction ->
                val counterRef = countersCollection.document(cleanedBase.lowercase())
                val counterDoc = transaction.get(counterRef)

                var nextNumber = (counterDoc.getLong("son_numara") ?: 0L) + 1
                var potentialUsername: String

                var attempts = 0
                while (attempts < 20) {
                    potentialUsername = if (nextNumber == 1L) cleanedBase else "$cleanedBase$nextNumber"
                    val usernameRef = usernamesCollection.document(potentialUsername.lowercase())
                    val isTaken = transaction.get(usernameRef).exists()

                    if (!isTaken) {
                        transaction.set(usernameRef, mapOf("reserved" to true, "display" to potentialUsername))
                        transaction.set(counterRef, mapOf("son_numara" to nextNumber), SetOptions.merge())
                        return@runTransaction potentialUsername
                    }
                    nextNumber++
                    attempts++
                }
                "$cleanedBase${(1000..9999).random()}"
            }.await()
        } catch (e: Exception) {
            Log.e("AuthService", "Kullanıcı adı oluşturma hatası: ${e.message}")
            "$cleanedBase${(100..999).random()}"
        }
    }

    override suspend fun register(email: String, password: String, username: String): AuthResult {
        isRegistering = true
        _authState.value = AuthState.Loading
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("User creation failed")
            appUserRepository.setRememberMe(false)

            val finalUsername = generateUniqueUsername(username)

            val profileUpdate = UserProfileChangeRequest.Builder().setDisplayName(finalUsername).build()
            firebaseUser.updateProfile(profileUpdate).await()

            val newUser = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = finalUsername,
                displayNameLowercase = finalUsername.lowercase(),
                username = finalUsername,
                roleValue = UserRole.MEMBER.name,
                createdAtMillis = System.currentTimeMillis()
            )

            firestore.collection("users").document(firebaseUser.uid).set(newUser, SetOptions.merge()).await()

            firebaseUser.reload().await()
            val regDateString = getRegistrationDateString(firebaseUser)

            _authState.value = AuthState.Authenticated(
                uid = firebaseUser.uid,
                displayName = finalUsername,
                email = email,
                photoUrl = null,
                isAnonymous = false,
                registrationDate = regDateString,
                isOfficial = false
            )
            AuthResult.Success(firebaseUser = firebaseUser, isAnonymous = false)
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            AuthResult.Error(message = getErrorMessage(e))
        } finally {
            isRegistering = false
        }
    }

    private fun getRegistrationDateString(firebaseUser: FirebaseUser?): String? {
        val ts = firebaseUser?.metadata?.creationTimestamp ?: return null
        if (ts <= 0L) return null
        return try {
            SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(ts))
        } catch (_: Exception) { null }
    }

    override fun getCurrentUserId() = auth.currentUser?.uid ?: ""
    override fun getCurrentUserDisplayName() = auth.currentUser?.displayName
    override fun getCurrentUserPhotoUrl() = auth.currentUser?.photoUrl?.toString()
    override fun getCurrentFirebaseUser() = auth.currentUser
    override suspend fun getCurrentUser(): User? = try { appUserRepository.getUserById(auth.currentUser?.uid ?: "") } catch (_: Exception) { null }
    override suspend fun getUserRole(): String = determineUserRole(auth.currentUser)

    private suspend fun determineUserRole(firebaseUser: FirebaseUser?): String {
        if (firebaseUser == null || firebaseUser.isAnonymous) return UserRole.GUEST.name
        return try {
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            doc.getString("role") ?: UserRole.MEMBER.name
        } catch (_: Exception) {
            UserRole.MEMBER.name
        }
    }

    override suspend fun isRememberMe() = appUserRepository.isRememberMe()
    override suspend fun setRememberMe(rememberMe: Boolean) = appUserRepository.setRememberMe(rememberMe)
    override suspend fun getUserById(userId: String) = appUserRepository.getUserById(userId)

    override suspend fun resetPassword(email: String): Pair<Boolean, String?> {
        return try { auth.sendPasswordResetEmail(email).await(); Pair(true, context.getString(R.string.auth_password_reset_success)) } catch (e: Exception) { Pair(false, getErrorMessage(e)) }
    }

    override fun isLoggedInFirebase() = auth.currentUser != null

    override suspend fun updatePassword(newPassword: String): Pair<Boolean, String?> {
        val user = auth.currentUser ?: return Pair(false, context.getString(R.string.auth_not_logged_in))
        return try { user.updatePassword(newPassword).await(); Pair(true, context.getString(R.string.auth_password_update_success)) } catch (e: Exception) { Pair(false, getErrorMessage(e)) }
    }

    override suspend fun updateUserName(newName: String): Pair<Boolean, String?> {
        val user = auth.currentUser ?: return Pair(false, context.getString(R.string.auth_user_not_found))
        val uid = user.uid
        return try {
            val finalUsername = generateUniqueUsername(newName)
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(finalUsername).build()).await()
            try {
                firestore.collection("users").document(uid).update(
                    "displayName", finalUsername,
                    "displayNameLowercase", finalUsername.lowercase(),
                    "username", finalUsername
                ).await()

                // 🚀 OTOMATİK GÜNCELLEME: Eski içeriklerdeki kullanıcı adlarını yenile
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val collections = listOf("photos", "videos", "sesler", "letters")
                        for (coll in collections) {
                            val snapshot = firestore.collection(coll).whereEqualTo("ownerId", uid).get().await()
                            if (!snapshot.isEmpty) {
                                val batch = firestore.batch()
                                for (doc in snapshot.documents) {
                                    batch.update(doc.reference, "username", finalUsername)
                                }
                                batch.commit().await()
                            }
                        }
                        Log.d("AuthService", "Tüm eski içeriklerdeki kullanıcı adları güncellendi.")
                    } catch (e: Exception) {
                        Log.e("AuthService", "Eski içerikler güncellenirken isim hatası oluştu: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthService", "Kullanıcı adı güncelleme hatası: ${e.message}")
            }
            Pair(true, context.getString(R.string.auth_username_update_success))
        } catch (e: Exception) {
            Pair(false, getErrorMessage(e))
        }
    }

    override suspend fun reauthenticateAndDelete(email: String, password: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Pair(false, "Oturum bulunamadı.")
        val uid = user.uid
        val userEmail = user.email ?: email
        Log.d("AuthService", "Hesap silme başlatıldı. UID: $uid")

        return@withContext try {
            if (!user.isAnonymous) {
                val credential = EmailAuthProvider.getCredential(userEmail, password)
                user.reauthenticate(credential).await()
            }

            try {
                firestore.collection("users").document(uid).update("fcmToken", FieldValue.delete()).await()
            } catch (_: Exception) {}

            supervisorScope {
                val cleanupTasks = mutableListOf<Job>()

                cleanupTasks.add(launch { try { photoRepository.deleteAllUserPhotos(uid) } catch (e: Exception) { Log.e("AuthService", "Foto silme hatası", e) } })
                cleanupTasks.add(launch { try { videoRepository.deleteAllUserVideos(uid) } catch (e: Exception) { Log.e("AuthService", "Video silme hatası", e) } })
                cleanupTasks.add(launch { try { sesRepository.deleteAllUserAudios(uid) } catch (e: Exception) { Log.e("AuthService", "Ses silme hatası", e) } })
                cleanupTasks.add(launch { try { letterRepository.deleteAllUserLetters(uid) } catch (e: Exception) { Log.e("AuthService", "Mektup silme hatası", e) } })
                cleanupTasks.add(launch { try { appUserRepository.deleteProfileImage(uid) } catch (e: Exception) { Log.e("AuthService", "Profil resmi silme hatası", e) } })

                cleanupTasks.add(launch {
                    try {
                        val messages = firestore.collection("chatRooms").document("GeneralChat")
                            .collection("messages").whereEqualTo("senderId", uid).get().await()
                        for (doc in messages.documents) { doc.reference.delete() }
                    } catch (_: Exception) {}
                })

                cleanupTasks.add(launch {
                    try {
                        val privateChats = firestore.collection("private_chats")
                            .whereArrayContains("participants", uid).get().await()
                        for (doc in privateChats.documents) {
                            doc.reference.update("participants", FieldValue.arrayRemove(uid))
                        }
                    } catch (_: Exception) {}
                })

                withTimeoutOrNull(20000) { cleanupTasks.joinAll() }
            }

            val userDoc = try { firestore.collection("users").document(uid).get().await() } catch (_: Exception) { null }
            val username = userDoc?.getString("username") ?: userDoc?.getString("displayName")
            if (!username.isNullOrBlank()) {
                try {
                    firestore.collection("kullanici_adlari").document(username.lowercase()).delete().await()
                    val baseUsername = username.filter { !it.isDigit() }.lowercase()
                    if (baseUsername.isNotEmpty()) {
                        firestore.collection("ad_numaralari").document(baseUsername).delete().await()
                    }
                } catch (e: Exception) {
                    Log.e("AuthService", "İsim rezervasyon silme hatası", e)
                }
            }

            firestore.collection("users").document(uid).delete().await()
            firestore.collection("privateUserData").document(uid).delete().await()

            appDatabase.clearAllTables()
            appUserRepository.clearAllData()

            user.delete().await()

            _authState.value = AuthState.Unauthenticated
            Pair(true, context.getString(R.string.auth_account_deleted_success))
        } catch (e: Exception) {
            Log.e("AuthService", "HESAP SİLME HATASI!", e)
            Pair(false, getErrorMessage(e))
        }
    }

    override suspend fun updateUserProfilePicture(imageUri: Uri): Pair<Boolean, String?> {
        val user = auth.currentUser ?: return Pair(false, context.getString(R.string.auth_user_not_found))
        val uid = user.uid
        return try {
            val photoUrl = appUserRepository.uploadProfileImage(uid, imageUri) ?: return Pair(false, context.getString(R.string.auth_image_upload_fail))
            user.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(photoUrl.toUri()).build()).await()
            try { appUserRepository.updateUserProfilePhotoUrl(uid, photoUrl) } catch (_: Exception) {}

            // 🚀 OTOMATİK GÜNCELLEME: Eski içeriklerdeki profil resimlerini yenile
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val collections = listOf("photos", "videos", "sesler", "letters")
                    for (coll in collections) {
                        val snapshot = firestore.collection(coll).whereEqualTo("ownerId", uid).get().await()
                        if (!snapshot.isEmpty) {
                            val batch = firestore.batch()
                            for (doc in snapshot.documents) {
                                batch.update(doc.reference, "userProfileImageUrl", photoUrl)
                            }
                            batch.commit().await()
                        }
                    }
                    Log.d("AuthService", "Tüm eski içeriklerdeki profil resimleri güncellendi.")
                } catch (e: Exception) {
                    Log.e("AuthService", "Eski içerikler güncellenirken hata oluştu: ${e.message}")
                }
            }

            Pair(true, context.getString(R.string.auth_profile_photo_update_success))
        } catch (e: Exception) { Pair(false, getErrorMessage(e)) }
    }

    override fun reloadAuthState() {
        val user = auth.currentUser ?: return
        coroutineScope.launch {
            try {
                try { withTimeoutOrNull(5000) { user.reload().await() } } catch (_: Exception) {}

                val initialName = user.displayName?.takeIf { it.isNotBlank() } ?: context.getString(R.string.state_loading)
                val baseState = AuthState.Authenticated(
                    uid = user.uid,
                    displayName = initialName,
                    email = user.email,
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = user.isAnonymous,
                    registrationDate = getRegistrationDateString(user),
                    isOfficial = false
                )

                try {
                    val fsDoc = withTimeoutOrNull(3000) {
                        firestore.collection("users").document(user.uid).get().await()
                    }
                    if (fsDoc != null && fsDoc.exists()) {
                        val finalDisplayName = if (user.isAnonymous) {
                            user.displayName?.takeIf { it.isNotBlank() } ?: context.getString(R.string.guest)
                        } else {
                            fsDoc.getString("displayName") ?: fsDoc.getString("username") ?: initialName
                        }
                        val isOfficial = fsDoc.getBoolean("isOfficial") ?: false
                        val badge = fsDoc.getString("badgeType") ?: "NONE"
                        val followers = fsDoc.getLong("followerCount")?.toInt() ?: 0
                        val following = fsDoc.getLong("followingCount")?.toInt() ?: 0
                        val isBanned = fsDoc.getBoolean("isBanned") ?: fsDoc.getBoolean("is_banned") ?: false

                        val firestorePhoto = fsDoc.getString("photoURL") ?: fsDoc.getString("photoUrl")
                        val finalPhotoUrl = firestorePhoto ?: user.photoUrl?.toString()

                        _authState.value = baseState.copy(
                            displayName = finalDisplayName,
                            photoUrl = finalPhotoUrl,
                            isOfficial = isOfficial,
                            badgeType = badge,
                            followerCount = followers,
                            followingCount = following,
                            isBanned = isBanned
                        )
                    } else {
                        _authState.value = baseState
                    }
                } catch (_: Exception) {
                    _authState.value = baseState
                }
            } catch (_: Exception) { }
        }
    }

    override suspend fun isTutorialCompleted(): Boolean = try { appUserRepository.isTutorialCompleted() } catch (_: Exception) { true }
    override suspend fun setTutorialCompleted(completed: Boolean) { try { appUserRepository.setTutorialCompleted(completed) } catch (_: Exception) {} }
}