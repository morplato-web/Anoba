package com.neval.anoba.video

sealed interface CapturedMedia {
    data class Photo(val uriOrPath: String) : CapturedMedia
    data class Video(val uriOrPath: String, val durationMillis: Long = 0L) : CapturedMedia
}
