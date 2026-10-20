package com.neval.anoba.common.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedItemDao {
    @Query("SELECT * FROM pinned_items WHERE userId = :userId ORDER BY pinnedAt DESC")
    fun getAllPinnedItems(userId: String): Flow<List<PinnedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pinItem(item: PinnedItemEntity)

    @Query("DELETE FROM pinned_items WHERE contentId = :id AND userId = :userId")
    suspend fun unpinItem(id: String, userId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM pinned_items WHERE contentId = :id AND userId = :userId)")
    fun isItemPinned(id: String, userId: String): Flow<Boolean>

    @Query("SELECT contentId FROM pinned_items WHERE userId = :userId")
    suspend fun getAllPinnedIds(userId: String): List<String>
}
