package com.neval.anoba.community.desktop

import com.neval.anoba.community.engine.VoiceEngineInterface

class DesktopVoiceListener(
    private val voiceEngine: VoiceEngineInterface
) {
    private var isPushToTalkActive: Boolean = false
    private var pushToTalkKeyCode: Int = 58 // Varsayılan Caps Lock tuşu

    fun setPushToTalkKey(keyCode: Int) {
        this.pushToTalkKeyCode = keyCode
    }

    // Windows / Desktop Küresel Tuş Basıldı
    fun onGlobalKeyPressed(keyCode: Int) {
        if (!isPushToTalkActive && keyCode == pushToTalkKeyCode) {
            isPushToTalkActive = true
            voiceEngine.setMuted(false)
        }
    }

    // Windows / Desktop Küresel Tuş Bırakıldı
    fun onGlobalKeyReleased(keyCode: Int) {
        if (isPushToTalkActive && keyCode == pushToTalkKeyCode) {
            isPushToTalkActive = false
            voiceEngine.setMuted(true)
        }
    }
}
