package com.example.reporterapp.database.table

import android.arch.persistence.room.*

@Entity(tableName = "article_table", indices = arrayOf(Index(value = ["Id"],
    unique = true)))
data class Article(
    @ColumnInfo(name = "articleTitle")
    val articleTitle: String,

    @ColumnInfo(name = "articleDes")
    val articleDes: String,

    @ColumnInfo(name = "languageID")
    val languageName: String,

    @ColumnInfo(name = "catID")
    val catID: String,

    @ColumnInfo(name = "subCatID")
    val subCatID: String,

    @ColumnInfo(name = "location")
    val location: String,

    @ColumnInfo(name = "status")
    var status: String,

    @ColumnInfo(name = "time")
    val time: Long,

    @ColumnInfo(name = "keywords")
    val keywords: String,

    @ColumnInfo(name = "files")
    val files: List<String>,

    @ColumnInfo(name = "s3Files")
    val s3Files: List<String>,

    @ColumnInfo(name = "featured_image")
    val featuredImage: String,

    @ColumnInfo(name = "Id")
    var id: String,

    @ColumnInfo(name = "comment")
    var comment: String,

    @ColumnInfo(name = "modifiedAt")
    val commentDate: String,

    @ColumnInfo(name = "globalStatus")
    var globalStatus: String,

    @ColumnInfo(name = "modifiedTime")
    var modifiedTime: Long

) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_article_id")
    var localId: Int = 0
}





