package com.neval.anoba.common.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun isSameDay(millis1: Long?, millis2: Long?): Boolean {
    if (millis1 == null || millis2 == null) return false
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] && cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
}

fun getFormattedDate(millis: Long?): String? {
    if (millis == null) return null
    val calendar = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply { timeInMillis = millis }
    return when {
        calendar[Calendar.YEAR] == messageDate[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == messageDate[Calendar.DAY_OF_YEAR] -> "Bugün"
        calendar[Calendar.YEAR] == messageDate[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] - 1 == messageDate[Calendar.DAY_OF_YEAR] -> "Dün"
        else -> SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(millis))
    }
}
