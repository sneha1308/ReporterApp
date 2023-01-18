package com.example.reporterapp.database.table

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "category_table", indices = arrayOf(Index(value = ["catName"],
    unique = true)))
data class Category (

    @PrimaryKey
    @ColumnInfo(name = "Id")
    val id: Int ,

    @ColumnInfo(name = "catName")
    val catName: String

)