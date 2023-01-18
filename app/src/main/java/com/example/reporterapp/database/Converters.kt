package com.example.reporterapp.database

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson

class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun listToJson(value: List<String>?): String {
            return Gson().toJson(value)
        }

        @TypeConverter
        @JvmStatic
        fun jsonToList(value: String): List<String>? {
            val objects = Gson().fromJson(value, Array<String>::class.java) as Array<String>
            val list = objects.toList()
            return list
        }
    }

}