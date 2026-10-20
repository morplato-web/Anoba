package com.neval.anoba.common.media

interface VideoPlayerController {
    fun play(url: String)
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
}
