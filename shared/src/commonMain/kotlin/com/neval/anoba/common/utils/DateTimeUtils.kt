package com.neval.anoba.common.utils

expect fun getCurrentTimeMillis(): Long

fun getRelativeTimeString(createdAtMillis: Long): String {
    if (createdAtMillis <= 0L) return ""

    val currentMillis = getCurrentTimeMillis()
    val diffMillis = currentMillis - createdAtMillis
    if (diffMillis < 0L) return "Şimdi"

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
