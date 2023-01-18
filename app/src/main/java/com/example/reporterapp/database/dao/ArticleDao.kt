package com.example.reporterapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.example.reporterapp.database.table.Article

@Dao
interface ArticleDao {

    @Query("SELECT * FROM article_table order by time desc")
    fun getAll(): LiveData<List<Article>>

    @Query("SELECT * FROM article_table WHERE status LIKE :status order by time desc")
    fun findByStatus(status: String): LiveData<List<Article>>

    @Query("SELECT * FROM article_table WHERE local_article_id LIKE :id")
    fun get(id: Int): LiveData<Article>

    @Query("SELECT COUNT(*) from article_table")
    fun countArticle(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg arrayOfArticles: Article)

    @Delete
    fun delete(article: Article)

    @Query("DELETE FROM article_table WHERE id = :article")
    fun deleteById(article: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(article: Article)


    @Query("UPDATE article_table SET articleTitle = :articleTitle,articleDes = :articleDes,languageID=:languageId,catID=:categoryId,subCatID=:subCatId,location=:location,status = :status,keywords=:keywords,files=:files, s3Files=:s3Files, featured_image=:featuredImage, time=:createdDate, comment=:comment, modifiedAt=:commentDate, globalStatus=:globalStatus, modifiedTime=:modifiedDate WHERE id = :aid ")
    fun updateArticle(
        aid: String,
        articleTitle: String,
        articleDes: String,
        languageId: String,
        categoryId: String,
        subCatId: String,
        location: String,
        status: String,
        keywords: String,
        files: String,
        s3Files:String,
        featuredImage:String,
        createdDate:Long,
        comment:String,
        commentDate:String,
        globalStatus: String,
        modifiedDate: Long
    ): Int
//    articleLocalId: Int
    @Query("DELETE FROM article_table")
    fun deleteAll()

    @Query("SELECT COUNT(*) from article_table where id = :id")
    fun getArticleCount(id: String): Int

    @Query("UPDATE article_table SET articleTitle = :articleTitle,articleDes = :articleDes,languageID=:languageId,catID=:categoryId,subCatID=:subCatId,location=:location,status = :status,keywords=:keywords,files=:files, s3Files=:s3Files, featured_image=:featuredImage, time=:createdDate WHERE local_article_id = :aid")
    fun updateDraftArticle(
        aid: Int,
        articleTitle: String,
        articleDes: String,
        languageId: String,
        categoryId: String,
        subCatId: String,
        location: String,
        status: String,
        keywords: String,
        files: String,
        s3Files:String,
        featuredImage:String,
        createdDate:Long
    ): Int
}