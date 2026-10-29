@file:Suppress("unused")

package com.neval.anoba.common.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Verilen milisaniyeyi "mm:ss" veya "m:ss" formatında bir String'e dönüştürür.
 * @param millis Toplam milisaniye
 * @param leadingZero Dakika kısmında başında sıfır olup olmayacağı (Örn: true -> 01:05, false -> 1:05)
 */
fun formatDuration(millis: Long, leadingZero: Boolean = true): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    val format = if (leadingZero) "%02d:%02d" else "%d:%02d"
    return String.format(Locale.US, format, minutes, seconds)
}

/**
 * Verilen Date objesini "HH:mm" formatında bir String'e dönüştürür.
 */
fun formatTimestamp(timestamp: Date): String {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(timestamp)
}
