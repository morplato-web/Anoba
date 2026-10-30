package com.neval.anoba.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neval.anoba.R
import com.neval.anoba.common.services.AuthServiceInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authService: AuthServiceInterface,
) : ViewModel() {

    // Sonucu (Başarı, Hata Mesajı, Hata ID) taşıyan bir veri sınıfı
    data class ResetState(
        val isSuccess: Boolean = false,
        val errorMessage: String? = null,
        val errorResId: Int? = null,
    )

    private val _resetResult = MutableStateFlow<ResetState?>(null)
    val resetResult: StateFlow<ResetState?> = _resetResult.asStateFlow()

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun resetPassword(email: String) {
        if (email.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            // authService.resetPassword Pair<Boolean, String?> döndürüyor
            val result = authService.resetPassword(email)
            val isSuccess = result.first
            val errorMessage = result.second

            _resetResult.value = if (isSuccess) {
                ResetState(isSuccess = true)
            } else {
                ResetState(
                    isSuccess = false,
                    errorMessage = errorMessage,
                    errorResId = if (errorMessage == null) R.string.error_unknown else null,
                )
            }
            _isLoading.value = false
        }
    }

    fun resetState() {
        _resetResult.value = null
    }
}
