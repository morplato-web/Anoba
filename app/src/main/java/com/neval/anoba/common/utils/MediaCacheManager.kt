package com.neval.anoba.common.utils

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import java.io.File

/**
 * Anoba Video ve Ses içerikleri için önbellekleme (Caching) yöneticisi.
 * Videoların donmasını engellemek ve internet kullanımını optimize etmek için kullanılır.
 */
@OptIn(UnstableApi::class)
object MediaCacheManager {
    private var simpleCache: SimpleCache? = null
    private const val CACHE_SIZE = 200 * 1024 * 1024L // 200MB Önbellek

    private fun getCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "media_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(CACHE_SIZE)
            val databaseProvider = StandaloneDatabaseProvider(context)
            simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
        }
        return simpleCache!!
    }

    fun createPlayer(context: Context): ExoPlayer {
        val cache = getCache(context)
        
        // Önbellek destekli veri kaynağı fabrikası
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
        
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                20_000, // Min buffer 20sn
                50_000, // Max buffer 50sn
                1_500,  // Playback start buffer 1.5sn
                3_000   // Rebuffer buffer 3sn
            ).build()

        return ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(cacheDataSourceFactory)
            )
            .build()
    }
}
