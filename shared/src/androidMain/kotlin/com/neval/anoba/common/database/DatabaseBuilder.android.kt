@file:Suppress("unused")

package com.neval.anoba.common.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getAndroidDatabaseBuilder(context: Context): RoomDatabase.Builder<SharedAppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("anoba_room.db")
    return Room.databaseBuilder<SharedAppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<SharedAppDatabase> {
    error("Android builds should use getAndroidDatabaseBuilder(context)")
}
