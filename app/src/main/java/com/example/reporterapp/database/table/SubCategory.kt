package com.example.reporterapp.database.table

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "sub_cat_table", indices = arrayOf(
    Index(value = ["subCat","catId"],
        unique = true)
))
class SubCategory (

    @PrimaryKey
    @ColumnInfo(name = "Id")
    var id: Int ,

    @ColumnInfo(name = "subCat")
    var subCatName: String ,

    @ColumnInfo(name = "catId")
    var catId: Int



){
    override fun toString(): String {
        return "Subcategory With Id $id & name$subCatName And CatID $catId"
    }
}