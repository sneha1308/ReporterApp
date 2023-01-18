package com.example.reporterapp.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import android.util.Log
import android.widget.Toast
import com.apollographql.apollo.coroutines.toDeferred
import com.example.reporterapp.*
import com.example.reporterapp.api.ApolloClient
import com.example.reporterapp.database.AppDatabase
import com.example.reporterapp.database.table.Article
import com.example.reporterapp.database.table.Category
import com.example.reporterapp.database.table.Language
import com.example.reporterapp.database.table.SubCategory
import com.example.reporterapp.type.ArticleInput
import com.example.reporterapp.type.ArticleStatusGlobalStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


lateinit var languageList: List<Language>
lateinit var categoryList: List<Category>
lateinit var subCategoryList: List<SubCategory>

class CreateArticleViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    val apolloClient by lazy { ApolloClient.setupApollo() }
    private val repository: Repository
    private val remoteRepo: RemoteRepository

    val id =0
    val allCategories: LiveData<List<Category>>
    val allLanguage: LiveData<List<Language>>
    private val allSubCategories: LiveData<List<SubCategory>>
    var allSubCatCategories: List<SubCategory> = listOf()
    var count: Int = 0

    var searchByLiveData: LiveData<List<SubCategory>>? = null
    private val filterLiveData = MutableLiveData<String>()

    init {
        val categoryDao = AppDatabase.getDatabase(application, scope).categoryDao()
        val languageDao = AppDatabase.getDatabase(application, scope).languageDao()
        val subCatDao = AppDatabase.getDatabase(application, scope).subCategoryDa0()
        val articleDao = AppDatabase.getDatabase(application, scope).articleDao()
        repository = Repository(categoryDao, languageDao, subCatDao, articleDao)

        allCategories = repository.allCategories
        allLanguage = repository.allLanguages
        allSubCategories = repository.allSubCategories

        remoteRepo = RemoteRepository(this)

        searchByLiveData = Transformations.switchMap(filterLiveData) { v ->
            repository.getSubCat(v.toInt())
        }
    }

    fun getSub(int: Int) {
        allSubCatCategories = repository.getSubCategory(int)
    }

    fun setData(id: Int) {
        this.filterLiveData.value = id.toString()
    }

   /* fun getCatId(name: String):Int{
        run {
            return repository.getCatId(name)
        }
    }*/

    fun insert(language: Language) = scope.launch(Dispatchers.IO) {
        repository.insert(language)
    }

    fun deleteAllCategory() = scope.launch(Dispatchers.IO) {
        repository.deleteAllCategory()
    }

    fun deleteAllLanguage() = scope.launch(Dispatchers.IO) {
        repository.deleteAllLanguage()
    }

    fun insert(category: Category) = scope.launch(Dispatchers.IO) {
        repository.insert(category)
    }

    fun getArticleCount(id: String) = scope.launch(Dispatchers.IO) {
        count = repository.getArticleCount(id)
    }

    fun insert(subCategory: SubCategory) = scope.launch(Dispatchers.IO) {
        repository.insert(subCategory)
    }

    fun insert(article: Article) = scope.launch(Dispatchers.IO) {
        repository.insert(article)
    }

    fun delete(article: Article) = scope.launch(Dispatchers.IO) {
        repository.delete(article)
    }

    fun delete(articleId: Int) = scope.launch(Dispatchers.IO) {
        repository.deleteById(articleId)
    }

    fun createArticle(article: Article) {
        if (isOnline(getApplication())) {
            remoteRepo.postToServer(article)
        } else {
            showMsg(getApplication(), "Please check Internet Connection", Toast.LENGTH_SHORT)
            return
        }
    }

    fun updateArticle(article: Article,isInsert: Boolean) {
        if (isOnline(getApplication())) {
            remoteRepo.updateArticleToServer(article,isInsert)
        } else {
            showMsg(getApplication(), "Please check Internet Connection", Toast.LENGTH_SHORT)
            return
        }
    }

    fun deleteArticle(articleId: Int) {
        if (isOnline(getApplication())) {
            remoteRepo.deleteArticleFromServer(articleId)
        } else {
            showMsg(getApplication(), "Please check Internet Connection", Toast.LENGTH_SHORT)
            return
        }
    }

   /* fun postToServer(article: Article) = scope.launch(Dispatchers.IO) {
        if (isOnline(getApplication())) {

            val deferred = apolloClient.mutate(
                CreateArticleMutation.builder().articleData(
                    ArticleInput.builder().body(article.articleDes).title(article.articleTitle).category(article.catID).subcategory(
                        article.subCatID
                    ).language(article.languageName).location(article.location).keywords(article.keywords).status(
                        article.status
                    ).featuredimage(
                        ""
                    ).files(article.s3Files).build()
                ).token(com.example.reporterapp.app.PreferenceManager.getInstance(getApplication()).token ?: "").build()
            )
                .toDeferred()
            val response = deferred.await()
            article.id = response.data()?.createArticle()?.article()?.id() ?: ""
            insert(article)
            if(!response.hasErrors()){
                showMsg(getApplication(), "Article Saved Successfully", Toast.LENGTH_SHORT)
            }
            Log.e("article", response.data().toString())
        } else {
            showMsg(getApplication(),"Please check Internet Connection",Toast.LENGTH_SHORT)
        }
    }*/


    fun update(
        articleId: String, languageId: String, categoryId: String, subCatId: String,
        location: String, articleTitle: String, articleDes: String, keywords: String,
        attachment: List<String>, s3AttachedFiles: List<String>, status: String, featuredImage:String,createdDate: Long,
        comment:String, commentDate:String, globalStatus: String,modifiedDate: Long
    ) = scope.launch(Dispatchers.IO) {
        // ,articleLocalId:Int
        repository.update(
            articleId, languageId, categoryId, subCatId, articleTitle, articleDes,
            location, keywords, attachment, s3AttachedFiles, status,featuredImage, createdDate, comment, commentDate, globalStatus,
            modifiedDate
        )
        //,articleLocalId
    }

    fun updateDraft(
        articleId: Int, languageId: String, categoryId: String, subCatId: String,
        location: String, articleTitle: String, articleDes: String, keywords: String,
        attachment: List<String>, s3AttachedFiles: List<String>, status: String, featuredImage:String,createdDate: Long
    ) = scope.launch(Dispatchers.IO) {
        // ,articleLocalId:Int
        repository.updateDraft(
            articleId, languageId, categoryId, subCatId, articleTitle, articleDes,
            location, keywords, attachment, s3AttachedFiles, status,featuredImage, createdDate
        )
        //,articleLocalId
    }

    fun deleteAllArticle() = scope.launch(Dispatchers.IO) {
        repository.deleteAllArticle()
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

    fun getCategories(lifecycleOwner: LifecycleOwner){
        if (isOnline(getApplication())) {
            remoteRepo.getLanguagesFromApi()
            remoteRepo.getAllCategories()
        } else {
            showMsg(getApplication(), "Please check Internet Connection", Toast.LENGTH_SHORT)
            return
        }
        allCategories.observe(lifecycleOwner, Observer { categories ->
            categories?.let {
                categoryList = it
            }
        })
    }


    fun fetchData(lifecycleOwner: LifecycleOwner, localTime: String?) {
        if (isOnline(getApplication())) {
            remoteRepo.getAllArticlesListWithTimeStamp(localTime)
        } else {
            showMsg(getApplication(), "Please check Internet Connection", Toast.LENGTH_SHORT)
            return
        }
    }
}

