package com.neval.anoba.community.engine

import android.content.Context
import com.neval.anoba.common.utils.Constants
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants.AUDIO_PROFILE_SPEECH_STANDARD
import io.agora.rtc2.Constants.AUDIO_SCENARIO_GAME_STREAMING
import io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommunityVoiceEngine(
    private val context: Context
) : VoiceEngineInterface {
    private var rtcEngine: RtcEngine? = null

    private val _speakingUserIds = MutableStateFlow<Set<Int>>(emptySet())
    override val speakingUserIds: StateFlow<Set<Int>> = _speakingUserIds.asStateFlow()

    private val eventHandler = object : IRtcEngineEventHandler() {
        override fun onAudioVolumeIndication(
            speakers: Array<out AudioVolumeInfo>?,
            totalVolume: Int
        ) {
            val currentlySpeaking = mutableSetOf<Int>()
            speakers?.forEach { speaker ->
                // Volume 15 üstündeyse konuşuyor say
                if (speaker.volume > 15) {
                    currentlySpeaking.add(speaker.uid)
                }
            }
            _speakingUserIds.value = currentlySpeaking
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            // Kullanıcı ses kanalına katıldı
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            // Kullanıcı ayrıldı
        }
    }

    override fun initEngineIfNeeded() {
        if (rtcEngine != null) return
        try {
            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = Constants.AGORA_APP_ID
                mEventHandler = eventHandler
            }
            rtcEngine = RtcEngine.create(config).apply {
                setChannelProfile(io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                setClientRole(CLIENT_ROLE_BROADCASTER)
                // Oyun & Sesli Sohbet Profili (Oyun seslerini boğmaz, yankı engeller)
                setAudioProfile(AUDIO_PROFILE_SPEECH_STANDARD, AUDIO_SCENARIO_GAME_STREAMING)
                // Her 200ms'de bir ses hacmini bildir (Yeşil halka animasyonu için)
                enableAudioVolumeIndication(200, 3, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun joinVoiceChannel(channelName: String, userIdInt: Int) {
        initEngineIfNeeded()
        val options = ChannelMediaOptions().apply {
            clientRoleType = CLIENT_ROLE_BROADCASTER
            channelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            publishMicrophoneTrack = true
            autoSubscribeAudio = true
        }
        rtcEngine?.joinChannel(null, channelName, userIdInt, options)
    }

    override fun leaveVoiceChannel() {
        try {
            rtcEngine?.leaveChannel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setMuted(isMuted: Boolean) {
        try {
            rtcEngine?.muteLocalAudioStream(isMuted)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setDeafened(isDeafened: Boolean) {
        try {
            // Kulaklık kapatıldığında hem karşı sesleri kapat hem de kendi mikrofonunu sustur
            rtcEngine?.muteAllRemoteAudioStreams(isDeafened)
            rtcEngine?.muteLocalAudioStream(isDeafened)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun destroy() {
        try {
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
            rtcEngine = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
