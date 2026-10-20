package com.neval.anoba.letter

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LetterDao {
    @Query("SELECT * FROM letter_table WHERE is_published = 1 OR is_published = 'true' ORDER BY publish_timestamp DESC")
    fun getAllPublishedLetters(): Flow<List<LetterModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetter(letter: LetterModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(letters: List<LetterModel>)

    @Query("SELECT * FROM letter_table WHERE id = :letterId LIMIT 1")
    suspend fun getLetterById(letterId: String): LetterModel?

    @Query("UPDATE letter_table SET viewCount = viewCount + 1 WHERE id = :letterId")
    suspend fun incrementLocalViewCount(letterId: String)

    @Query("DELETE FROM letter_table WHERE id = :letterId")
    suspend fun deleteLetterById(letterId: String)

    @Query("UPDATE letter_table SET is_published = 0")
    suspend fun resetPublishedLetters()
}