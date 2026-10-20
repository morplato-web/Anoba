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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpViewModel(
    private val authService: AuthServiceInterface,
    private val userRepository: IUserRepository,
    private val emailValidator: EmailValidator,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _confirmPasswordVisible = MutableStateFlow(false)
    val confirmPasswordVisible: StateFlow<Boolean> = _confirmPasswordVisible.asStateFlow()

    private val _usernameError = MutableStateFlow<Int?>(null)
    val usernameError: StateFlow<Int?> = _usernameError.asStateFlow()

    private val _emailError = MutableStateFlow<Int?>(null)
    val emailError: StateFlow<Int?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<Int?>(null)
    val passwordError: StateFlow<Int?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<Int?>(null)
    val confirmPasswordError: StateFlow<Int?> = _confirmPasswordError.asStateFlow()

    private val _formValid = MutableStateFlow(false)
    val formValid: StateFlow<Boolean> = _formValid.asStateFlow()

    private val _signUpState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    fun updateUsername(newUsername: String) {
        _username.update { newUsername } // trim() kaldırıldı, böylece yazarken boşluk bırakabilirsin
        validateForm()
    }

    fun updateEmail(newEmail: String) {
        _email.update { newEmail.trim() }
        validateForm()
    }

    fun updatePassword(newPassword: String) {
        _password.update { newPassword } // Şifrede de trim olması sorun yaratabilir, kaldırdık
        validateForm()
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        _confirmPassword.update { newConfirmPassword } // Kaldırıldı
        validateForm()
    }

    fun togglePasswordVisibility() { _passwordVisible.update { !it } }

    fun toggleConfirmPasswordVisibility() { _confirmPasswordVisible.update { !it } }

    private fun validateForm() {
        // Kullanıcı adı hatası: 3 karakterden azsa
        _usernameError.value = if (_username.value.isNotEmpty() && _username.value.length < 3) {
            R.string.error_username_short
        } else null

        // E-posta hatası: Format geçersizse
        _emailError.value = if (_email.value.isNotEmpty() && !emailValidator.isValid(_email.value)) {
            R.string.error_invalid_email
        } else null

        // Şifre hatası: 6 karakterden azsa
        _passwordError.value = if (_password.value.isNotEmpty() && _password.value.length < 6) {
            R.string.error_password_too_short
        } else null

        // Şifre eşleşme hatası
        _confirmPasswordError.value = if (_confirmPassword.value.isNotEmpty() && _password.value != _confirmPassword.value) {
            R.string.error_passwords_mismatch
        } else null

        // Formun genel geçerliliği
        _formValid.value = _usernameError.value == null &&
                _emailError.value == null &&
                _passwordError.value == null &&
                _confirmPasswordError.value == null &&
                _username.value.isNotEmpty() &&
                _email.value.isNotEmpty() &&
                _password.value.isNotEmpty() &&
                _confirmPassword.value.isNotEmpty() &&
                _password.value == _confirmPassword.value
    }

    fun performRegistration() {
        validateForm()
        if (!_formValid.value) return

        _signUpState.value = SignUpUiState.Loading
        viewModelScope.launch {
            val result = authService.register(
                email = _email.value,
                password = _password.value,
                username = _username.value
            )
            _signUpState.value = when (result) {
                is AuthResult.Success -> {
                    analyticsHelper.logEvent("sign_up_success")
                    withContext(Dispatchers.IO) {
                        userRepository.setRememberMe(true)
                    }
                    SignUpUiState.Success
                }
                is AuthResult.Error -> {
                    analyticsHelper.logEvent("sign_up_failed") {
                        putString("error", result.message ?: "Unknown")
                    }
                    // AuthResult içindeki message'ı kullanıyoruz
                    SignUpUiState.Error(result.message)
                }
                else -> SignUpUiState.Idle
            }
        }
    }

    fun onSignUpResultConsumed() {
        _signUpState.value = SignUpUiState.Idle
    }

    sealed interface SignUpUiState {
        data object Idle : SignUpUiState
        data object Loading : SignUpUiState
        data object Success : SignUpUiState
        data class Error(val message: String?) : SignUpUiState
    }
}