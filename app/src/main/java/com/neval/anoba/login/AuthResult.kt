package com.neval.anoba.login

import com.google.firebase.auth.FirebaseUser

sealed class AuthResult {
    data object Idle : AuthResult()
    data object Loading : AuthResult()
    data class Success(val firebaseUser: FirebaseUser, val isAnonymous: Boolean = false) : AuthResult()
    // Mesajı opsiyonel yaptık, ResourceProvider ile ViewModel'de dile çevireceğiz
    data class Error(val message: String? = null) : AuthResult()
}