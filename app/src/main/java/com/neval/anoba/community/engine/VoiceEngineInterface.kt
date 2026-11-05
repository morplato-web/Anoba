package com.neval.anoba.community.engine

import kotlinx.coroutines.flow.StateFlow

interface VoiceEngineInterface {
    val speakingUserIds: StateFlow<Set<Int>>
    fun initEngineIfNeeded()
    fun joinVoiceChannel(channelName: String, userIdInt: Int)
    fun leaveVoiceChannel()
    fun setMuted(isMuted: Boolean)
    fun setDeafened(isDeafened: Boolean)
    fun destroy()
}
