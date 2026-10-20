package com.neval.anoba.mycontent

import android.net.Uri

sealed interface TrimState {
    data object Idle : TrimState
    data class InProgress(val progress: Int) : TrimState
    data class Success(val newUri: Uri) : TrimState
    data object Saving : TrimState
    data object UploadComplete : TrimState
    data class Error(val message: String) : TrimState
}
