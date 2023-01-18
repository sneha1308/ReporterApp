package com.example.reporterapp.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.example.reporterapp.ArticleRepository
import com.example.reporterapp.database.AppDatabase
import com.example.reporterapp.database.table.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ArticleViewModel(application: Application): AndroidViewModel(application){

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private val articleRepository: ArticleRepository

    var searchByLiveData: LiveData<Article>? = null
    private val filterLiveData = MutableLiveData<Int>()

    init {

        val articleDao = AppDatabase.getDatabase(application, scope).articleDao()
        articleRepository = ArticleRepository(articleDao)
        searchByLiveData = Transformations.switchMap(filterLiveData) {
                v -> articleRepository.getArticle(v)
        }
    }

    fun setData(id:Int){
        // Toast.makeText(getApplication(),"setData$id",Toast.LENGTH_SHORT).show();
        this.filterLiveData.value=id
    }

    fun insert(category: Article) = scope.launch(Dispatchers.IO) {
        articleRepository.insert(category)
    }

    fun  delete(articleId:Int) = scope.launch(Dispatchers.IO) {
        articleRepository.deleteById(articleId)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
