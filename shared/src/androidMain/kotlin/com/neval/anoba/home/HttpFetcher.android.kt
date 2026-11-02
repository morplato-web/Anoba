package com.neval.anoba.home

import java.net.HttpURLConnection
import java.net.URL

actual fun httpGet(urlString: String): String {
    return try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.inputStream.bufferedReader().use { it.readText() }
    } catch (_: Exception) {
        ""
    }
}
