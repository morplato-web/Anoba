package com.neval.anoba.market

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Suppress("Unused", "unused")
@Dao
interface MarketDao {
    @Query("SELECT * FROM market_table WHERE is_sold = 0 ORDER BY id DESC")
    fun getAllActiveItems(): Flow<List<MarketItemModel>>

    @Query("SELECT * FROM market_table ORDER BY id DESC")
    fun getAllItems(): Flow<List<MarketItemModel>>

    @Query("SELECT * FROM market_table WHERE id = :id")
    suspend fun getItemById(id: String): MarketItemModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MarketItemModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MarketItemModel)

    @Query("DELETE FROM market_table WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM market_table")
    suspend fun clearAll()
}
