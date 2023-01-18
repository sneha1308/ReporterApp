package com.example.reporterapp.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.example.reporterapp.ArticleRepository
import com.example.reporterapp.ArticleStatus
import com.example.reporterapp.database.AppDatabase
import com.example.reporterapp.database.table.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ArticleListViewModel(application: Application): AndroidViewModel(application){

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private val articleRepository: ArticleRepository

    var searchByLiveData: LiveData<List<Article>>? = null
    private val filterLiveData = MutableLiveData<String>()

    init {
        val articleDao = AppDatabase.getDatabase(application, scope).articleDao()
        articleRepository = ArticleRepository(articleDao)

        searchByLiveData = Transformations.switchMap(filterLiveData) {
                v -> articleRepository.getCat(if (v.equals("RE-WORK", ignoreCase = true)) "Rework"
        else if (v.equals("DRAFTS", ignoreCase = true)) "Draft" else v)
        }
    }

    fun setData(id:String){
        // Toast.makeText(getApplication(),"setData$id",Toast.LENGTH_SHORT).show();
        this.filterLiveData.value=id
    }

    fun insert(category: Article) = scope.launch(Dispatchers.IO) {
        articleRepository.insert(category)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
