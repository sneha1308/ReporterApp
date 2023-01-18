package com.example.reporterapp

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.example.reporterapp.database.dao.UserDao
import com.example.reporterapp.database.table.Article
import com.example.reporterapp.database.table.User

class UserRepository(private val userDao: UserDao) {


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(category: User) {
        userDao.insert(category)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        userDao.deleteAll()
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(int : Int,name: String) {
        userDao.updateTour(int,name)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun getUser(): LiveData<User> {
        return userDao.get()
    }

}