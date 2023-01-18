package com.example.reporterapp

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import android.text.TextUtils
import android.widget.Toast
import com.example.reporterapp.database.dao.ArticleDao
import com.example.reporterapp.database.dao.CategoryDao
import com.example.reporterapp.database.dao.LanguageDao
import com.example.reporterapp.database.dao.SubCategoryDao
import com.example.reporterapp.database.table.Article
import com.example.reporterapp.database.table.Category
import com.example.reporterapp.database.table.Language
import com.example.reporterapp.database.table.SubCategory

class ArticleRepository(private val articleDao: ArticleDao) {

    fun getCat(articleStatus: String): LiveData<List<Article>> {
        return articleDao.findByStatus(articleStatus)
    }

    fun getArticle(id:Int):LiveData<Article>{
        return articleDao.get(id)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(category: Article) {
        articleDao.insert(category)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteById(articleId: Int) {
        articleDao.deleteById(articleId)
    }


}