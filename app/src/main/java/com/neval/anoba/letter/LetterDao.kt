package com.neval.anoba.letter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LetterDao {
    @Query("SELECT * FROM letter_table WHERE is_published = 1 ORDER BY publish_timestamp DESC")
    fun getAllPublishedLetters(): Flow<List<LetterModel>>

    @Query("SELECT * FROM letter_table WHERE ownerId = :userId ORDER BY publish_timestamp DESC")
    fun getUserLetters(userId: String): Flow<List<LetterModel>>

    @Query("SELECT * FROM letter_table WHERE id = :id")
    suspend fun getLetterById(id: String): LetterModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(letters: List<LetterModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetter(letter: LetterModel)

    @Query("DELETE FROM letter_table WHERE id = :id")
    suspend fun deleteLetterById(id: String)

    @Query("DELETE FROM letter_table WHERE is_published = 1")
    suspend fun clearPublishedLetters()

    @Transaction
    suspend fun resetPublishedLetters() {
        clearPublishedLetters()
    }

    @Query("UPDATE letter_table SET viewCount = viewCount + 1 WHERE id = :id")
    suspend fun incrementLocalViewCount(id: String)
}
