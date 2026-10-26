package com.neval.anoba.market

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedMarketDao {
    @Query("SELECT * FROM shared_market_table WHERE is_sold = 0 ORDER BY createdAtMillis DESC")
    fun getAllActiveItems(): Flow<List<SharedMarketItemModel>>

    @Query("SELECT * FROM shared_market_table ORDER BY createdAtMillis DESC")
    fun getAllItems(): Flow<List<SharedMarketItemModel>>

    @Query("SELECT * FROM shared_market_table WHERE id = :id")
    suspend fun getItemById(id: String): SharedMarketItemModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SharedMarketItemModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SharedMarketItemModel)

    @Query("DELETE FROM shared_market_table WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM shared_market_table")
    suspend fun clearAll()
}
