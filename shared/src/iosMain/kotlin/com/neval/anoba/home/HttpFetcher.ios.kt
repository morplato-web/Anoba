package com.neval.anoba.home

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfURL

@OptIn(ExperimentalForeignApi::class)
actual fun httpGet(urlString: String): String {
    return try {
        val url = NSURL.URLWithString(urlString) ?: return ""
        NSString.stringWithContentsOfURL(url, encoding = NSUTF8StringEncoding, error = null) ?: ""
    } catch (_: Exception) {
        ""
    }
}
