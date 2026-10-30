package com.neval.anoba.mete

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Sol paneldeki kelime ve ses kartlarının veri modeli (mete_kelimeler_tr)
 */
@Suppress("Unused", "unused")
@Keep
@IgnoreExtraProperties
data class MeteWordItem(
    var id: Long = 0L,
    var sectionName: String = "1",       // Bölüm adı / numarası (Örn: "1" veya "Çiftlik")
    var audioUrl: String? = null,
    var imageUrl: String? = null,
    var soundResName: String? = null,
    var imageResId: Int? = null,
    var animationUrl: String? = null,
    var animationResId: Int? = null,
    var videoUrl: String? = null,
    var videoResId: Int? = null,
) {
    @get:Exclude @set:Exclude var boxColor: Color? = null

    @get:Exclude
    val cleanAnimationUrl: String?
        get() = animationUrl?.takeIf { it.isNotBlank() && it != "null" }

    @get:Exclude
    val cleanVideoUrl: String?
        get() = videoUrl?.takeIf { it.isNotBlank() && it != "null" }

    @get:Exclude
    val cleanAudioUrl: String?
        get() = audioUrl?.takeIf { it.isNotBlank() && it != "null" }

    @get:Exclude
    val cleanImageUrl: String?
        get() = imageUrl?.takeIf { it.isNotBlank() && it != "null" }
}
