package com.neval.anoba.photo

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo_table WHERE is_published = 1 ORDER BY publish_timestamp DESC")
    fun getAllPublishedPhotos(): Flow<List<PhotoModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<PhotoModel>)

    @Query("SELECT * FROM photo_table WHERE id = :photoId LIMIT 1")
    suspend fun getPhotoById(photoId: String): PhotoModel?

    @Query("UPDATE photo_table SET viewCount = viewCount + 1 WHERE id = :photoId")
    suspend fun incrementLocalViewCount(photoId: String)

    @Query("DELETE FROM photo_table WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: String)

    @Query("UPDATE photo_table SET is_published = 0")
    suspend fun resetPublishedPhotos()
}
