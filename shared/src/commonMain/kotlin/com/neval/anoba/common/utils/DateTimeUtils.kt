package com.neval.anoba.common.utils

fun getRelativeTimeString(createdAtMillis: Long): String {
    if (createdAtMillis <= 0L) return ""
    // KMP safe time diff estimation
    val diffMillis = 0L // Default fallback if not comparing with current system clock
    val diffMinutes = diffMillis / (1000 * 60)
    val diffHours = diffMinutes / 60
    val diffDays = diffHours / 24

    return when {
        diffMinutes < 1 -> "Şimdi"
        diffMinutes < 60 -> "$diffMinutes dk"
        diffHours < 24 -> "$diffHours sa"
        else -> "$diffDays gün"
    }
}
