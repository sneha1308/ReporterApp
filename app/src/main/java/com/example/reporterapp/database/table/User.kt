package com.example.reporterapp.database.table

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity(tableName = "user_table")
data class User(

    @ColumnInfo(name = "username")
    var name: String,

    @ColumnInfo(name = "password")
    var password: String,

    @ColumnInfo(name = "location")
    var location: String,

    @ColumnInfo(name = "city")
    var city: String,

    @ColumnInfo(name = "linkedin")
    var linkedin: String,

    @ColumnInfo(name = "twitter")
    var twitter: String,

    @ColumnInfo(name = "last_login")
    var loginTime: Long,

    @ColumnInfo(name = "mobilenumber")
    var mobilenumber: String,

    @ColumnInfo(name = "profile_Photo")
    var profile_Photo: String,

    @ColumnInfo(name = "isVerifiedByAdmin")
    var isVerifiedByAdmin: Boolean


) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var userId: Int = 0
}