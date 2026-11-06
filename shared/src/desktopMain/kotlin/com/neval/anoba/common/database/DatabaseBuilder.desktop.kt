package com.neval.anoba.common.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<SharedAppDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".anoba/anoba_shared.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<SharedAppDatabase>(
        name = dbFile.absolutePath
    )
}
