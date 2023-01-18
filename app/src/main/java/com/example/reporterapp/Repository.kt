package com.example.reporterapp

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import android.util.Log
import com.example.reporterapp.api.RetrofitFactory
import com.example.reporterapp.database.Converters
import com.example.reporterapp.database.dao.ArticleDao
import com.example.reporterapp.database.dao.CategoryDao
import com.example.reporterapp.database.dao.LanguageDao
import com.example.reporterapp.database.dao.SubCategoryDao
import com.example.reporterapp.database.table.Article
import com.example.reporterapp.database.table.Category
import com.example.reporterapp.database.table.Language
import com.example.reporterapp.database.table.SubCategory

class Repository(private val categoryDao: CategoryDao,
                 private val languageDao: LanguageDao,
                 private val subCategoryDao: SubCategoryDao,
                 private val articleDao: ArticleDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allCategories: LiveData<List<Category>> = categoryDao.getAll()
    val allLanguages: LiveData<List<Language>> = languageDao.getAll()
    val allSubCategories: LiveData<List<SubCategory>> = subCategoryDao.getAll()

    fun getSubCat(int: Int):LiveData<List<SubCategory>>{
        return subCategoryDao.loadAllByCatIds(int)
    }

    fun getSubCategory(int: Int):List<SubCategory>{
        return subCategoryDao.getSubCategoryId(int)
    }

    init {
        Log.e("Reposi","Size of Langauge${allLanguages.value?.size}")
    }

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(language: Language) {
        languageDao.insert(language)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(category: Category) {
        categoryDao.insert(category)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(subCategory: SubCategory) {
        subCategoryDao.insert(subCategory)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(article: Article) {
        articleDao.insert(article)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(article: Article) {
        articleDao.delete(article)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteById(articleId: Int) {
        articleDao.deleteById(articleId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(articleId:String,languageId:String,categoryId:String,
                       subCatId:String,articleTitle: String,articleDes:String,location:String, keywords:String,
                       attachment:List<String>,s3AttachedFiles: List<String>,status:String, featuredImage:String,createdDate: Long, comment:String,
                       commentDate:String, globalStatus: String,modifiedDate: Long)
                       //,articleLocalId: Int
    {
        articleDao.updateArticle(articleId,articleTitle,articleDes,languageId,
            categoryId,subCatId,location,status,keywords,Converters.listToJson(attachment),Converters.listToJson(s3AttachedFiles),featuredImage,createdDate, comment,
            commentDate,globalStatus,modifiedDate)
        //,articleLocalId
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateDraft(articleId:Int,languageId:String,categoryId:String,
                       subCatId:String,articleTitle: String,articleDes:String,location:String, keywords:String,
                       attachment:List<String>,s3AttachedFiles: List<String>,status:String, featuredImage:String,createdDate: Long)
    //,articleLocalId: Int
    {
        articleDao.updateDraftArticle(articleId,articleTitle,articleDes,languageId,
            categoryId,subCatId,location,status,keywords,Converters.listToJson(attachment),Converters.listToJson(s3AttachedFiles),featuredImage,createdDate)
        //,articleLocalId
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAllCategory() {
        categoryDao.deleteAll()
        subCategoryDao.deleteAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAllArticle() {
        articleDao.deleteAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getArticleCount(id: String):Int {
        return articleDao.getArticleCount(id)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAllLanguage() {
        languageDao.deleteAll()
    }

    fun getCatId(name: String): Int {
        return categoryDao.getCatId(name)
    }

}