package com.example.reporterapp.database.table

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "attachment_table")
data class Attachment (



    @ColumnInfo(name = "localPath")
    val localPath: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "userId")
    val userId: Int

){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var id: Int=0
}