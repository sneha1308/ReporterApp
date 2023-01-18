package com.example.reporterapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.example.reporterapp.database.table.Attachment

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachment_table")
    fun getAll(): LiveData<List<Attachment>>

    /* @Query("SELECT * FROM category WHERE cat_name LIKE :first LIMIT 1")
     fun findByCategory(first: String): Model.Category*/

    @Query("SELECT COUNT(*) from attachment_table")
    fun countAttachment(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg arrayOfAttachment: Attachment)

    @Delete
    fun delete(attachment: Attachment)

    @Insert
    fun insert(attachment: Attachment)

    @Query("DELETE FROM attachment_table")
    fun deleteAll()
}