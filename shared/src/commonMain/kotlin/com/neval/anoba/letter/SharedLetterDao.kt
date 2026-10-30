@file:Suppress("unused")

package com.neval.anoba.letter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedLetterDao {
    @Query("SELECT * FROM shared_letter_table WHERE is_published = 1 ORDER BY publishTimestampMillis DESC")
    fun getAllPublishedLetters(): Flow<List<SharedLetterModel>>

    @Query("SELECT * FROM shared_letter_table WHERE ownerId = :userId ORDER BY createdAtMillis DESC")
    fun getUserLetters(userId: String): Flow<List<SharedLetterModel>>

    @Query("SELECT * FROM shared_letter_table WHERE id = :id")
    suspend fun getLetterById(id: String): SharedLetterModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(letters: List<SharedLetterModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(letter: SharedLetterModel)

    @Query("DELETE FROM shared_letter_table WHERE id = :id")
    suspend fun deleteById(id: String)
}
