package com.example.reporterapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.example.reporterapp.database.table.Category
import com.example.reporterapp.database.table.Language

@Dao
interface LanguageDao {

    @Query("SELECT * FROM language_table")
    fun getAll(): LiveData<List<Language>>

    /* @Query("SELECT * FROM category WHERE cat_name LIKE :first LIMIT 1")
     fun findByCategory(first: String): Model.Category*/

    @Query("SELECT COUNT(*) from language_table")
    fun countLanguage(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg categories: Language)

    @Delete
    fun delete(language: Language)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(language: Language)

    @Query("DELETE FROM language_table")
    fun deleteAll()
}