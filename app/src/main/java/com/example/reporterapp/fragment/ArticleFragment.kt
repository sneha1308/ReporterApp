package com.example.reporterapp.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.reporterapp.ARTICLE_ID
import com.example.reporterapp.ArticleStatus
import com.example.reporterapp.R
import com.example.reporterapp.activity.ArticleDetailActivity
import com.example.reporterapp.activity.MainActivity
import com.example.reporterapp.adapter.ARTICLE_STATUS
import com.example.reporterapp.adapter.ArticleAdapter
import com.example.reporterapp.app.PreferenceManager
import com.example.reporterapp.callback.CountCallback
import com.example.reporterapp.callback.QueryCallback
import com.example.reporterapp.callback.RecyclerViewListener
import com.example.reporterapp.localToUTCForArticles
import com.example.reporterapp.viewmodel.ArticleListViewModel
import com.example.reporterapp.viewmodel.CreateArticleViewModel


class ArticleFragment() : Fragment(),QueryCallback,CountCallback {


    private var articleStatus = ArticleStatus.NONE
    private lateinit var articleListViewModel: ArticleListViewModel
    var countCallback:CountCallback? = null
    lateinit var articleAdapter:ArticleAdapter
    private lateinit var createArticleViewModel: CreateArticleViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val articleStatusOrdinal = arguments?.getInt(ARTICLE_STATUS)
        if (articleStatusOrdinal != null) this.articleStatus = ArticleStatus.values()[articleStatusOrdinal]
        createArticleViewModel = ViewModelProviders.of(this).get(CreateArticleViewModel::class.java)



        articleAdapter = ArticleAdapter(object : RecyclerViewListener {
            override fun recyclerViewListClicked(v: View, position: Int, localId: Int) {
                startActivity(Intent(this@ArticleFragment.context, ArticleDetailActivity::class.java).apply {
                    putExtra(ARTICLE_ID,position)
                    if(localId != -1)
                        putExtra("artile_local_id",localId)
                })
            }
        })

        val view=inflater.inflate(R.layout.fragment_draft, container, false)
        val recycler=view.findViewById<RecyclerView>(R.id.recycler)

        val pullToRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        pullToRefresh.setOnRefreshListener {
            (activity as MainActivity).loader.show()
            pullToRefresh.isRefreshing = true
            createArticleViewModel.getCategories(this)
            createArticleViewModel.fetchData(this,if(context?.let { PreferenceManager.getInstance(it).refresh_date } == null) "" else
                PreferenceManager.getInstance(context!!).refresh_date)
            context?.let {
                PreferenceManager.getInstance(it)
                    .setRefreshDate(localToUTCForArticles())
            }
            refreshData()
            pullToRefresh.isRefreshing = false
        }

        recycler.adapter = articleAdapter
        recycler.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        refreshData()

        return view
    }

    fun refreshData() {

        articleListViewModel = ViewModelProviders.of(this).get(ArticleListViewModel::class.java)
        articleListViewModel.setData(this.articleStatus.caption)

        articleListViewModel.searchByLiveData?.observe(this, Observer { articles ->
            articles?.let {
                countCallback?.result(articleStatus.ordinal,it.count())

                articleAdapter.setData(it,this)
                (activity as MainActivity).loader.dismiss()
            }

        })

    }

    override fun filter(query: String) {
        articleAdapter.setFilter(query)
    }

    override fun result(position: Int, count: Int) {
    }
}