package com.neval.anoba.common.database

import androidx.room.Entity

/**
 * Kullanıcının "Pinle" (İğnele) özelliğini kullanarak favorilerine eklediği
 * içeriklerin ID'lerini yerel veritabanında saklayan tablo.
 */
@Entity(tableName = "pinned_items", primaryKeys = ["contentId", "userId"])
data class PinnedItemEntity(
    val contentId: String, // İçeriğin ID'si
    val userId: String,    // İğneleyen kullanıcının ID'si
    val contentType: String,
    val pinnedAt: Long = 0L
)
