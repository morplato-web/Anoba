package com.neval.anoba.video.videoemoji

import com.neval.anoba.common.utils.StandardEmojis

object VideoEmojis {
    // Merkezi StandardEmojis listesini kullanıyoruz
    val list = StandardEmojis.list

    val emojiColors = StandardEmojis.emojiColors

    /**
     * Eski ID'lerle (HEART vb.) yanlışlıkla kaydedilmiş veriler varsa 
     * bunları doğru emojilere çevirerek UI'ı kurtarıyoruz.
     */
    fun getDisplayEmoji(key: String): String {
        return when (key.uppercase()) {
            "HEART" -> "❤️"
            "HAPPY" -> "😂"
            "CLAP" -> "👍"
            "SAD" -> "😢"
            "SURPRISED" -> "😲"
            "THINKING" -> "🤔"
            "ANGRY" -> "😠"
            "REPORT" -> "🚫"
            "DELETE" -> "❌"
            else -> key
        }
    }
}
