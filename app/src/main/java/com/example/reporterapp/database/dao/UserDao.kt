package com.example.reporterapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.example.reporterapp.database.table.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user_table")
    fun getAll(): LiveData<List<User>>

    @Query("SELECT * FROM user_table limit 1")
    fun get(): LiveData<User>

    @Query("SELECT * FROM user_table WHERE id LIKE :userId")
    fun findByStatus(userId: String): LiveData<List<User>>

    @Query("SELECT COUNT(*) from user_table")
    fun countUser(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg arrayOfArticles: User)

    @Delete
    fun delete(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Query("DELETE FROM user_table")
    fun deleteAll()

    @Query("UPDATE user_table SET username = :name WHERE id = :tid")
    fun updateTour(tid: Int, name: String): Int
}