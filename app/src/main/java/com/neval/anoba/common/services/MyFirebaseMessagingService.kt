package com.neval.anoba.common.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.neval.anoba.MainActivity
import com.neval.anoba.MyApplication
import com.neval.anoba.R
import com.neval.anoba.common.repository.IUserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val userRepository: IUserRepository by inject()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val type = data["type"] ?: "GENERAL"

        Log.d(TAG, "Bildirim geldi. Tip: $type")

        // 🚀 KRİTİK FİLTRE: Sadece CHAT ve CALL için push (sesli/görsel) bildirimi göster
        if (type == "CHAT" || type == "CALL") {
            if (data.isNotEmpty()) {
                handleDataMessage(data)
            }

            remoteMessage.notification?.let {
                sendNotification(
                    title = it.title ?: getString(R.string.notification_default_title),
                    messageBody = it.body ?: "",
                    data = data
                )
            }
        } else {
            // Sosyal etkileşimler kilit ekranında sessiz kalır. 
            // Firestore zaten bunları kaydediyor, biz sadece telefonun ötmesini engelledik.
            Log.d(TAG, "Sessiz bildirim: $type")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            scope.launch {
                userRepository.updateFcmToken(currentUserId, token)
            }
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        if (type == "CALL") {
            handleIncomingCall(data)
            return
        }

        val title = data["title"] ?: getString(R.string.notification_default_title)
        val body = data["body"] ?: getString(R.string.notification_new_content)

        sendNotification(title, body, data)
    }

    private fun handleIncomingCall(data: Map<String, String>) {
        val callerName = data["callerName"] ?: getString(R.string.notification_someone)
        val isVideo = data["isVideoCall"]?.toBoolean() ?: true

        val ringtoneIntent = Intent(this, RingtoneService::class.java).apply {
            action = RingtoneService.ACTION_START
        }
        try {
            startService(ringtoneIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Ringtone başlatılamadı: ${e.message}")
        }

        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isVideo) getString(R.string.call_video_incoming) else getString(R.string.call_voice_incoming)
        val body = getString(R.string.notification_calling_you, callerName)

        val notificationBuilder = NotificationCompat.Builder(this, MyApplication.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(fullScreenPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notificationBuilder.build())
    }

    private fun sendNotification(title: String, messageBody: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, MyApplication.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
