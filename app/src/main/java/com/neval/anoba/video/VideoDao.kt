package com.neval.anoba.video

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM video_table WHERE is_published = 1 ORDER BY publish_timestamp DESC")
    fun getAllPublishedVideos(): Flow<List<VideoModel>>

    @Query("""
        SELECT * FROM video_table 
        WHERE is_published = 1 
        AND ownerId IN (SELECT followingId FROM followed_users) 
        ORDER BY publish_timestamp DESC
    """)
    fun getFollowingVideos(): Flow<List<VideoModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<VideoModel>)

    @Query("SELECT * FROM video_table WHERE id = :videoId LIMIT 1")
    suspend fun getVideoById(videoId: String): VideoModel?

    @Query("UPDATE video_table SET viewCount = viewCount + 1 WHERE id = :videoId")
    suspend fun incrementLocalViewCount(videoId: String)

    @Query("DELETE FROM video_table WHERE id = :videoId")
    suspend fun deleteVideoById(videoId: String)

    @Query("UPDATE video_table SET is_published = 0")
    suspend fun resetPublishedVideos()
}
