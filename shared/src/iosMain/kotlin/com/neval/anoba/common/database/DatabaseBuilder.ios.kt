@file:Suppress("unused")

package com.neval.anoba.common.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

actual fun getDatabaseBuilder(): RoomDatabase.Builder<SharedAppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/Documents/anoba_room.db"
    return Room.databaseBuilder<SharedAppDatabase>(
        name = dbFilePath,
    )
}
