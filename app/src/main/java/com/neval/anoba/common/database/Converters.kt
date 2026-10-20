package com.neval.anoba.common.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

@Suppress("unused")
object Converters {
    
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringMap(value: String?): Map<String, String> {
        val listType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value ?: "{}", listType)
    }

    @TypeConverter
    fun toStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromLongMap(value: String?): Map<String, Long> {
        val listType = object : TypeToken<Map<String, Long>>() {}.type
        return gson.fromJson(value ?: "{}", listType)
    }

    @TypeConverter
    fun toLongMap(map: Map<String, Long>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value ?: "[]", listType)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return gson.toJson(list)
    }
}
