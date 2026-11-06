package com.neval.anoba.home

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun httpGet(urlString: String): String {
    return try {
        java.net.URL(urlString).readText()
    } catch (e: Exception) {
        ""
    }
}
