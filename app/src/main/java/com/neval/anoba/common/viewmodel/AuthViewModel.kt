package com.neval.anoba.common.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.neval.anoba.R
import com.neval.anoba.common.repository.IUserRepository
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.state.UiState
import com.neval.anoba.common.utils.ResourceProvider
import com.neval.anoba.home.TutorialStep
import com.neval.anoba.login.AuthResult
import com.neval.anoba.login.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class AuthViewModel(
    private val authService: AuthServiceInterface,
    private val userRepository: IUserRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _currentUserRole.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showTutorial = MutableStateFlow(false)
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    private val _currentTutorialStep = MutableStateFlow(TutorialStep.WELCOME)
    val currentTutorialStep: StateFlow<TutorialStep> = _currentTutorialStep.asStateFlow()

    private val _sonNumara = MutableStateFlow<Int?>(null)
    val sonNumara: StateFlow<Int?> = _sonNumara.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteAccountState: StateFlow<UiState<Unit>> = _deleteAccountState.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<com.neval.anoba.chat.general.GeneralChatUser>>(emptyList())
    val blockedUsers: StateFlow<List<com.neval.anoba.chat.general.GeneralChatUser>> = _blockedUsers.asStateFlow()

    init {
        observeAuthState()
        loadTutorialState()
        listenForBlockedUsers()
    }

    fun loginWithGoogle(idToken: String, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authService.loginWithGoogle(idToken)
                onResult(result)
            } catch (e: Exception) {
                onResult(AuthResult.Error(e.localizedMessage ?: resourceProvider.getString(R.string.auth_generic_fail)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun listenForBlockedUsers() {
        viewModelScope.launch {
            userRepository.getAllUsersStream().collect { allUsers ->
                val state = _authState.value
                if (state is AuthState.Authenticated) {
                    val me = allUsers.find { it.uid == state.uid }
                    val blockedUids = me?.blockedUsers ?: emptyList()

                    val blockedProfiles = allUsers.filter { blockedUids.contains(it.uid) }.map { user ->
                        com.neval.anoba.chat.general.GeneralChatUser(
                            id = user.uid,
                            uid = user.uid,
                            displayName = user.displayName,
                            username = user.username,
                            email = user.email,
                            photoUrl = user.photoURL,
                            role = user.role.name,
                            badgeType = user.badgeType,
                            isOfficial = user.isOfficial
                        )
                    }
                    _blockedUsers.value = blockedProfiles
                }
            }
        }
    }

    fun unblockUser(targetUid: String) {
        val state = _authState.value
        if (state is AuthState.Authenticated) {
            viewModelScope.launch {
                userRepository.unblockUser(state.uid, targetUid)
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authService.authStateFlow.collectLatest { state ->
                _authState.value = state

                if (state is AuthState.Authenticated) {
                    _currentUserRole.value = authService.getUserRole()
                    loadUserProfile()

                    viewModelScope.launch {
                        try {
                            val token = FirebaseMessaging.getInstance().token.await()
                            userRepository.updateFcmToken(state.uid, token)
                        } catch (e: Exception) {
                            Log.e("AuthViewModel", "FCM Token hatası", e)
                        }
                    }
                } else {
                    _currentUserRole.value = null
                    _sonNumara.value = null
                }
            }
        }
    }

    private fun loadTutorialState() {
        viewModelScope.launch {
            _showTutorial.value = !authService.isTutorialCompleted()
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val uid = authService.getCurrentUserId()
            if (uid.isNotBlank()) {
                _sonNumara.value = userRepository.getSonNumara(uid)
            }
        }
    }

    fun refreshProfile() {
        authService.refreshAuthState()
        loadUserProfile()
    }

    fun logout() {
        viewModelScope.launch {
            val state = _authState.value
            if (state is AuthState.Authenticated) {
                try {
                    userRepository.clearFcmToken(state.uid)
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Logout sırasında FCM Token temizleme hatası", e)
                }
            }
            authService.logout()
            userRepository.setRememberMe(false)
        }
    }

    fun startTutorial() {
        _currentTutorialStep.value = TutorialStep.WELCOME
        _showTutorial.value = true
    }

    fun nextTutorialStep() {
        val current = _currentTutorialStep.value
        val steps = TutorialStep.entries
        val currentIndex = steps.indexOf(current)
        if (currentIndex < steps.size - 1) {
            _currentTutorialStep.value = steps[currentIndex + 1]
        } else {
            completeTutorial()
        }
    }

    fun completeTutorial() {
        _showTutorial.value = false
        viewModelScope.launch { userRepository.setTutorialCompleted(true) }
    }

    fun updateUserName(newName: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authService.updateUserName(newName)
                onResult(result.first, result.second)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Kullanıcı adı güncelleme hatası", e)
                onResult(false, resourceProvider.getString(R.string.auth_generic_fail))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reauthenticateAndDelete(email: String, pass: String) {
        viewModelScope.launch {
            _deleteAccountState.value = UiState.Loading
            try {
                val result = authService.reauthenticateAndDelete(email, pass)
                if (result.first) {
                    _deleteAccountState.value = UiState.Success(Unit)
                } else {
                    _deleteAccountState.value = UiState.Error(result.second ?: resourceProvider.getString(R.string.auth_generic_fail))
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Hesap silme hatası", e)
                _deleteAccountState.value = UiState.Error(resourceProvider.getString(R.string.auth_generic_fail))
            }
        }
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = UiState.Idle
    }

    fun updateUserProfilePicture(imageUri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withTimeoutOrNull(30000) {
                    authService.updateUserProfilePicture(imageUri)
                }
                if (result != null) {
                    if (result.first) {
                        onResult(true, null)
                    } else {
                        onResult(false, result.second)
                    }
                } else {
                    onResult(false, resourceProvider.getString(R.string.mycontent_error_timeout))
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Profil resmi güncelleme hatası", e)
                onResult(false, resourceProvider.getString(R.string.auth_generic_fail))
            } finally {
                _isLoading.value = false
            }
        }
    }
}