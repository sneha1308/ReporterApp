package com.example.reporterapp.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.content.Context
import com.example.reporterapp.ArticleStatus
import com.example.reporterapp.database.dao.*
import com.example.reporterapp.database.table.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

const val DATABASE_NAME="reporter_app.db"

@TypeConverters(com.example.reporterapp.database.Converters::class)
@Database(entities = [Category::class, SubCategory::class, Language::class,Article::class,User::class], version = 1)

abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun languageDao(): LanguageDao
    abstract fun subCategoryDa0(): SubCategoryDao
    abstract fun articleDao():ArticleDao
    abstract fun userDao():UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context,scope:CoroutineScope): AppDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ):RoomDatabase.Callback(){

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.categoryDao())
                        populateDatabase(database.languageDao())
                        populateDatabase(database.subCategoryDa0())
                        //populateDatabase(database.articleDao())
                        populateDatabase(database.userDao())
                    }
                }
            }
        }
        fun populateDatabase(languageDao: LanguageDao) {
            languageDao.deleteAll()
            /*  var language = Language(0,"English","english language","en")
              languageDao.insert(language)
              language = Language(1,"Telugu","ap and telugu language","tn")
              languageDao.insert(language)*/
        }

        fun populateDatabase(categoryDao: CategoryDao) {
            categoryDao.deleteAll()
            /*var category = Category(0,"Politics")
            categoryDao.insert(category)
            category = Category(1,"Crime")
            categoryDao.insert(category)
            category = Category(2,"Sports")
            categoryDao.insert(category)*/
        }

        fun populateDatabase(subcategoryDao: SubCategoryDao) {
            subcategoryDao.deleteAll()
        }

        fun populateDatabase(userDao: UserDao) {
            userDao.deleteAll()
/*
            val user= User("Sneha Reddy",
                "1234",
                "Kakinada",
                "Hyderabad",
                "https://linkedin.com/abc",
                "https://twitter.com/xyz",
                System.currentTimeMillis().toString(),)*/

          //  userDao.insert(user)
        }

       /* fun populateDatabase(articleDao: ArticleDao){
            articleDao.deleteAll()
        }*/
    }

}