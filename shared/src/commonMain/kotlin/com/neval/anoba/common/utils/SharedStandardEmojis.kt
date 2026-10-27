package com.neval.anoba.common.utils

import androidx.compose.ui.graphics.Color

object SharedStandardEmojis {
    val list = listOf(
        "❤️", "😂", "👍", "🔥", "🥰", "😢", "😲", "🤔", "🫤", "😠", "🚫", "❌"
    )

    val emojiColors = mapOf(
        "❤️" to Color(0xFFFDECEC),
        "😂" to Color(0xFFFFF9E6),
        "👍" to Color(0xFFE8F5E9),
        "🔥" to Color(0xFFFFF3E0),
        "🥰" to Color(0xFFFCE4EC),
        "😢" to Color(0xFFE3F2FD),
        "😲" to Color(0xFFF3E5F5),
        "🤔" to Color(0xFFECEFF1),
        "🫤" to Color(0xFFF5F5F5),
        "😠" to Color(0xFFFFEBEE),
        "🚫" to Color(0xFFE0E0E0),
        "❌" to Color(0xFFFFEBEE)
    )
}
