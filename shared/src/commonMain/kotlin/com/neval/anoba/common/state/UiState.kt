package com.neval.anoba.common.state

/**
 * Uygulama genelinde UI durumlarını yönetmek için kullanılan temel sınıf.
 * Success: Veri başarıyla yüklendi.
 * Error: Bir hata oluştu (mesaj ve opsiyonel hata kodu ile).
 * Loading: Veri şu an yükleniyor.
 * Idle: Henüz bir işlem başlatılmadı.
 */
sealed class UiState<out T> {
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val code: String? = null) : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data object Idle : UiState<Nothing>()
}
