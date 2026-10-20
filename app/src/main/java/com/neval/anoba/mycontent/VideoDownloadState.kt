package com.neval.anoba.mycontent

import android.net.Uri

sealed interface VideoDownloadState {
    data object Idle : VideoDownloadState
    data object InProgress : VideoDownloadState
    data class Success(val videoId: String, val localUri: Uri) : VideoDownloadState
    data class Error(val message: String? = null, val messageResId: Int? = null) : VideoDownloadState
}
