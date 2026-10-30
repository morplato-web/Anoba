package com.neval.anoba.mete

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Sağ paneldeki çizgi film / video kartlarının veri modeli (mete_hikayeler_tr)
 */
@Suppress("Unused", "unused")
@Keep
@IgnoreExtraProperties
data class MeteStoryItem(
    var id: Long = 0L,
    var title: String = "1",             // Hikaye / video bölüm adı (Örn: "1" veya "Çiftlik")
    var videoUrl: String? = null,
    var videoResId: Int? = null,
    var imageUrl: String? = null,        // Kapak görseli
)
