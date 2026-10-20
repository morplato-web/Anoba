package com.neval.anoba.ses

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SesDao {
    @Query("SELECT * FROM ses_table WHERE is_published = 1 ORDER BY publish_timestamp DESC")
    fun getAllPublishedSes(): Flow<List<SesModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSes(ses: SesModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sesList: List<SesModel>)

    @Query("SELECT * FROM ses_table WHERE id = :sesId LIMIT 1")
    suspend fun getSesById(sesId: String): SesModel?

    @Query("DELETE FROM ses_table WHERE id = :sesId")
    suspend fun deleteSesById(sesId: String)

    @Query("UPDATE ses_table SET is_published = 0")
    suspend fun resetPublishedSes()
}