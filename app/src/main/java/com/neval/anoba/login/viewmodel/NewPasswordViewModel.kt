package com.neval.anoba.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neval.anoba.R
import com.neval.anoba.common.services.AuthServiceInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface NewPasswordUiState {
    data object Idle : NewPasswordUiState
    data object Loading : NewPasswordUiState
    data class Success(val message: String? = null, val messageRes: Int? = null) : NewPasswordUiState
    data class Error(val message: String? = null, val messageRes: Int? = null) : NewPasswordUiState
}

class NewPasswordViewModel(
    private val authService: AuthServiceInterface,
) : ViewModel() {

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _passwordError = MutableStateFlow<Int?>(null)
    val passwordError: StateFlow<Int?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<Int?>(null)
    val confirmPasswordError: StateFlow<Int?> = _confirmPasswordError.asStateFlow()

    private val _isFormValid = MutableStateFlow(false)
    val isFormValid: StateFlow<Boolean> = _isFormValid.asStateFlow()

    private val _uiState = MutableStateFlow<NewPasswordUiState>(NewPasswordUiState.Idle)
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    fun onPasswordChanged(new: String) {
        _password.value = new
        validateForm()
    }

    fun onConfirmPasswordChanged(new: String) {
        _confirmPassword.value = new
        validateForm()
    }

    private fun validateForm() {
        _passwordError.value = if (_password.value.isNotEmpty() && (_password.value.length < 6)) {
            R.string.error_password_too_short
        } else null

        _confirmPasswordError.value = if (_confirmPassword.value.isNotEmpty() && _password.value != _confirmPassword.value) {
            R.string.error_passwords_mismatch
        } else null

        _isFormValid.value = _passwordError.value == null &&
                _confirmPasswordError.value == null &&
                _password.value.length >= 6 &&
                _password.value == _confirmPassword.value
    }

    fun updatePassword() {
        if (!_isFormValid.value) return

        _uiState.value = NewPasswordUiState.Loading
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    authService.updatePassword(_password.value)
                }
                val success = result.first
                val message = result.second

                _uiState.value = if (success) {
                    NewPasswordUiState.Success(message = message)
                } else {
                    NewPasswordUiState.Error(
                        message = message,
                        messageRes = if (message == null) R.string.error_unknown else null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = NewPasswordUiState.Error(
                    message = e.localizedMessage,
                    messageRes = R.string.error_unknown
                )
            }
        }
    }

    fun resetState(){
        _uiState.value = NewPasswordUiState.Idle
    }
}
