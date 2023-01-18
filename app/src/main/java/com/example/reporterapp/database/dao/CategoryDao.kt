package com.example.reporterapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.example.reporterapp.database.table.Category

@Dao
interface CategoryDao {

    @Query("SELECT * FROM category_table")
    fun getAll(): LiveData<List<Category>>

    @Query("SELECT * FROM category_table WHERE id IN (:categoriesId)")
    fun loadAllByIds(categoriesId: Int): LiveData<List<Category>>

    /* @Query("SELECT * FROM category WHERE cat_name LIKE :first LIMIT 1")
     fun findByCategory(first: String): Model.Category*/

    @Query("SELECT COUNT(*) from category_table")
    fun countCategories(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg categories: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category: Category)

    @Delete
    fun delete(category: Category)

    @Query("DELETE FROM category_table")
    fun deleteAll()

    @Query("select id FROM category_table where catName= :name")
    fun getCatId(name: String): Int
}