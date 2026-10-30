package com.neval.anoba.login

sealed class AuthResult {
    data object Idle : AuthResult()
    data object Loading : AuthResult()
    data class Success(
        val uid: String = "",
        val isAnonymous: Boolean = false,
    ) : AuthResult()

    data class Error(
        val message: String? = null,
    ) : AuthResult()
}
