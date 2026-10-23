package com.neval.anoba.letter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LetterDao {
    @Query("SELECT * FROM letter_table WHERE is_published = 1 ORDER BY publishTimestampMillis DESC")
    fun getAllPublishedLetters(): Flow<List<LetterModel>>

    @Query("SELECT * FROM letter_table WHERE ownerId = :userId ORDER BY createdAtMillis DESC")
    fun getUserLetters(userId: String): Flow<List<LetterModel>>

    @Query("SELECT * FROM letter_table WHERE id = :id")
    suspend fun getLetterById(id: String): LetterModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(letters: List<LetterModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(letter: LetterModel)

    @Query("DELETE FROM letter_table WHERE id = :id")
    suspend fun deleteById(id: String)
}
