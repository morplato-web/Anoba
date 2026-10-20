package com.neval.anoba.common.utils

import android.content.Context
import com.neval.anoba.R
import java.text.SimpleDateFormat
import java.util.Date

object DateTimeUtils {

    /**
     * Anoba Standart Zaman Formatlayıcı.
     * Cihazın diline göre "Az önce", "5 dakika önce", "Dün" veya "25 May 2024" döner.
     */
    fun formatRelativeTime(context: Context, dateMillis: Long?): String {
        if (dateMillis == null) return context.getString(R.string.common_unknown)

        val now = Date().time
        val diff = now - dateMillis

        // Android 13+ Per-app language ve yerelleştirme uyumluluğu için
        val locale = context.resources.configuration.locales[0]

        // GELECEK TARİH KONTROLÜ (Zaman Kapsülü için)
        if (diff < 0) {
            val futureDiff = -diff
            val fMinutes = futureDiff / (60 * 1000)
            val fHours = futureDiff / (60 * 60 * 1000)
            val fDays = futureDiff / (24 * 60 * 60 * 1000)

            return when {
                fMinutes < 60 -> context.getString(R.string.common_minutes_later, fMinutes.toInt())
                fHours < 24 -> context.getString(R.string.common_hours_later, fHours.toInt())
                fDays < 30 -> context.getString(R.string.common_days_later, fDays.toInt())
                else -> SimpleDateFormat("dd MMM yyyy", locale).format(Date(dateMillis))
            }
        }

        val minutes = diff / (60 * 1000)
        val hours = diff / (60 * 60 * 1000)
        val days = diff / (24 * 60 * 60 * 1000)

        return when {
            minutes < 1 -> context.getString(R.string.common_just_now)
            minutes < 60 -> context.getString(R.string.common_minutes_ago, minutes.toInt())
            hours < 24 -> context.getString(R.string.common_hours_ago, hours.toInt())
            days < 7 -> context.getString(R.string.common_days_ago, days.toInt())
            else -> {
                val actualDate = Date(dateMillis)
                SimpleDateFormat("dd MMM yyyy", locale).format(actualDate)
            }
        }
    }
}
