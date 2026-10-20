package com.neval.anoba.common.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.neval.anoba.MyApplication
import com.neval.anoba.R

class RingtoneService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_START) {
            startRingtone()
        } else if (action == ACTION_STOP) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startRingtone() {
        if (mediaPlayer == null) {
            try {
                // Foreground Service için bildirim zorunludur (Android 8+)
                val notification = NotificationCompat.Builder(this, MyApplication.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.call_incoming))
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // Mevcut ikonlardan biri
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .build()
                
                startForeground(1001, notification)

                mediaPlayer = MediaPlayer.create(this, R.raw.ahizeli_telefon).apply {
                    isLooping = true
                    start()
                }
                Log.d("RingtoneService", "Zil sesi başlatıldı")
            } catch (e: Exception) {
                Log.e("RingtoneService", "Zil sesi başlatılamadı: ${e.message}")
                stopSelf()
            }
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            } catch (e: Exception) {
                Log.e("RingtoneService", "Durdurma hatası: ${e.message}")
            }
        }
        mediaPlayer = null
        Log.d("RingtoneService", "Zil sesi durduruldu ve bellek temizlendi")
    }

    override fun onDestroy() {
        stopRingtone()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.neval.anoba.ACTION_START_RINGTONE"
        const val ACTION_STOP = "com.neval.anoba.ACTION_STOP_RINGTONE"
    }
}
