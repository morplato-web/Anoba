package com.neval.anoba.di

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

object AppInitializer {
    fun initialize(
        context: Context,
        authInstance: FirebaseAuth,
        firestoreInstance: FirebaseFirestore,
        storageInstance: FirebaseStorage
    ) {
        try {
            // Eğer zaten bir Koin instance'ı varsa tekrar başlatma
            if (GlobalContext.getOrNull() == null) {
                startKoin {
                    androidLogger(Level.INFO)
                    androidContext(context)
                    modules(
                        createAppModuleDefinition(
                            authInstance = authInstance,
                            firestoreInstance = firestoreInstance,
                            storageInstance = storageInstance
                        )
                    )
                }
                Log.d("AppInitializer", "Koin başarıyla başlatıldı.")
            } else {
                Log.d("AppInitializer", "Koin zaten çalışıyor, atlanıyor.")
            }
        } catch (e: Exception) {
            Log.e("AppInitializer", "Koin başlatma hatası (Zaten başlamış olabilir): ${e.message}")
        }
    }
}
