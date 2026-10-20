package com.neval.anoba.common.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getAndroidDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("anoba_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    error("Android builds should use getAndroidDatabaseBuilder(context)")
}
