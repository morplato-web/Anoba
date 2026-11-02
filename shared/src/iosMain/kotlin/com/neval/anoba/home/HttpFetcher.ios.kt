package com.neval.anoba.home

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.timeIntervalSince1970

@OptIn(ExperimentalForeignApi::class)
actual fun httpGet(urlString: String): String {
    return try {
        val url = NSURL.URLWithString(urlString) ?: return ""
        NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null) ?: ""
    } catch (_: Exception) {
        ""
    }
}

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
