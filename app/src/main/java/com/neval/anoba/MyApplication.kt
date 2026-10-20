package com.neval.anoba

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.neval.anoba.di.AppInitializer

class MyApplication : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "anoba_messages"
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        FirebaseApp.initializeApp(this)

        val isDebuggable = (0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE))

        if (isDebuggable) {
            try {
                val debugFactoryClass = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                val getInstanceMethod = debugFactoryClass.getMethod("getInstance")
                val factory = getInstanceMethod.invoke(null) as AppCheckProviderFactory

                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(factory)
                Log.d("MyApplication", "App Check: Debug Token Aktif")
            } catch (e: Exception) {
                Log.e("MyApplication", "DebugAppCheck başlatılamadı", e)
            }
        } else {
            try {
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Log.d("MyApplication", "App Check: Play Integrity Aktif")
            } catch (e: Exception) {
                Log.e("MyApplication", "Play Integrity başlatılamadı", e)
            }
        }

        AppInitializer.initialize(
            context = this@MyApplication,
            authInstance = FirebaseAuth.getInstance(),
            firestoreInstance = FirebaseFirestore.getInstance(),
            storageInstance = FirebaseStorage.getInstance()
        )

        coil3.SingletonImageLoader.setSafe {
            org.koin.mp.KoinPlatformTools.defaultContext().get().get<coil3.ImageLoader>()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = "Anoba mesaj ve mektup bildirimleri"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}