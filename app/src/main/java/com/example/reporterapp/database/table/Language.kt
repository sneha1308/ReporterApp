package com.example.reporterapp.database.table

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "language_table", indices = arrayOf(
    Index(value = ["languageName"],
        unique = true)
))
class Language (

    @PrimaryKey
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "languageName")
    val languageName: String ,

    @ColumnInfo(name = "languageDes")
    val languageDes: String ,

    @ColumnInfo(name = "locale")
    val locale: String

)