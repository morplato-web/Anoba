@file:Suppress("unused")

package com.neval.anoba.common.media

class IosVideoPlayerController : VideoPlayerController {
    override fun play(url: String) {}
    override fun pause() {}
    override fun stop() {}
    override fun release() {}
    override fun isPlaying(): Boolean = false
}
