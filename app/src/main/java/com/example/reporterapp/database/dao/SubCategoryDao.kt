package com.example.reporterapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.example.reporterapp.database.table.Category
import com.example.reporterapp.database.table.SubCategory

@Dao
interface SubCategoryDao {

    @Query("SELECT * FROM sub_cat_table")
    fun getAll(): LiveData<List<SubCategory>>

    @Query("SELECT * FROM sub_cat_table WHERE catId IN (:categoriesIds)")
    fun loadAllByCatIds(categoriesIds: Int): LiveData<List<SubCategory>>

    @Query("SELECT * FROM sub_cat_table WHERE catId IN (:categoriesIds)")
    fun getSubCategoryId(categoriesIds: Int): List<SubCategory>

    @Query("SELECT * FROM sub_cat_table WHERE id LIKE :id")
    fun findById(id: Int): LiveData<List<SubCategory>>

    @Query("SELECT COUNT(*) from sub_cat_table")
    fun countSubCategories(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg categories: SubCategory)

    @Delete
    fun delete(subcategory: SubCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subcategory: SubCategory)

    @Query("DELETE FROM sub_cat_table")
    fun deleteAll()
}