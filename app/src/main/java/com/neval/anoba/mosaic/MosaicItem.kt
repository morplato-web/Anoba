package com.neval.anoba.mosaic

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Mozaik (Mosaic) modülünde her bir kutunun veri modelini temsil eder.
 */
@Keep
@IgnoreExtraProperties
data class MosaicItem(
    @DocumentId
    var id: String = "",
    var index: Long = 0L,
    var type: String = "color",
    var contentUrl: String? = null,
    var textContent: String? = null,
    var viewCount: Long = 0,
    var clickCount: Long = 0,
    var colorHex: String? = null,
    var isAd: Boolean = false
)