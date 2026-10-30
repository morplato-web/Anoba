package com.neval.anoba.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neval.anoba.R
import com.neval.anoba.common.repository.IUserRepository
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.utils.AnalyticsHelper
import com.neval.anoba.common.utils.EmailValidator
import com.neval.anoba.login.AuthResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val loginResult: LoginResult? = null,
) {
    sealed class LoginResult {
        data object Success : LoginResult()
        data class Error(val message: String? = null, val messageRes: Int? = null) : LoginResult()
    }
}

class LoginViewModel(
    private val authService: AuthServiceInterface,
    private val userRepository: IUserRepository,
    private val emailValidator: EmailValidator,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            val isValid = emailValidator.isValid(email)
            currentState.copy(
                email = email,
                emailError = if (isValid || email.isEmpty()) null else R.string.error_invalid_email,
                isFormValid = validateForm(email, currentState.password),
            )
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            val isValid = password.length >= 6
            currentState.copy(
                password = password,
                passwordError = if (isValid || password.isEmpty()) null else R.string.error_password_too_short,
                isFormValid = validateForm(currentState.email, password),
            )
        }
    }

    fun updateRememberMe(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        if (!_uiState.value.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = authService.login(
                email = _uiState.value.email,
                password = _uiState.value.password,
                rememberMe = _uiState.value.rememberMe,
            )

            val loginResultState = when (result) {
                is AuthResult.Success -> {
                    analyticsHelper.logEvent("login_success")
                    withContext(Dispatchers.IO) {
                        userRepository.setRememberMe(_uiState.value.rememberMe)
                    }
                    authService.reloadAuthState()
                    LoginUiState.LoginResult.Success
                }
                is AuthResult.Error -> {
                    analyticsHelper.logEvent("login_failed") {
                        putString("error", result.message ?: "Unknown")
                    }
                    LoginUiState.LoginResult.Error(
                        message = result.message,
                        messageRes = if (result.message == null) R.string.error_unknown else null,
                    )
                }
                else -> LoginUiState.LoginResult.Error(messageRes = R.string.error_unknown)
            }

            _uiState.update {
                it.copy(isLoading = false, loginResult = loginResultState)
            }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            withContext(Dispatchers.IO) {
                userRepository.setRememberMe(rememberMe = false)
            }

            val loginResultState = when (val result = authService.loginAnonymously()) {
                is AuthResult.Success -> {
                    analyticsHelper.logEvent("guest_login_success")
                    authService.reloadAuthState()
                    LoginUiState.LoginResult.Success
                }
                is AuthResult.Error -> {
                    analyticsHelper.logEvent("guest_login_failed") {
                        putString("error", result.message ?: "Unknown")
                    }
                    LoginUiState.LoginResult.Error(
                        message = result.message,
                        messageRes = if (result.message == null) R.string.error_unknown else null,
                    )
                }
                else -> LoginUiState.LoginResult.Error(messageRes = R.string.error_unknown)
            }

            _uiState.update { it.copy(isLoading = false, loginResult = loginResultState) }
        }
    }

    fun onLoginResultConsumed() {
        _uiState.update { it.copy(loginResult = null) }
    }

    private fun validateForm(email: String, pass: String): Boolean {
        return (emailValidator.isValid(email) && pass.length >= 6)
    }
}
