package com.neval.anoba.common.repository

import com.neval.anoba.common.database.PinnedItemDao
import com.neval.anoba.common.database.PinnedItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class PinnedItemRepository(private val pinnedItemDao: PinnedItemDao) {

    fun getAllPinnedItems(userId: String): Flow<List<PinnedItemEntity>> {
        if (userId.isEmpty()) return emptyFlow()
        return pinnedItemDao.getAllPinnedItems(userId)
    }

    suspend fun togglePin(userId: String, contentId: String, contentType: String) {
        if (userId.isEmpty()) return
        val pinnedIds = pinnedItemDao.getAllPinnedIds(userId)
        if (pinnedIds.contains(contentId)) {
            pinnedItemDao.unpinItem(contentId, userId)
        } else {
            pinnedItemDao.pinItem(PinnedItemEntity(contentId, userId, contentType))
        }
    }

    @Suppress("unused")
    fun isItemPinned(userId: String, contentId: String): Flow<Boolean> {
        if (userId.isEmpty()) return emptyFlow()
        return pinnedItemDao.isItemPinned(contentId, userId)
    }
}
