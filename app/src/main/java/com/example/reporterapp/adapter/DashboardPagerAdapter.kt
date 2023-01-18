package com.example.reporterapp.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.reporterapp.fragment.*
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import com.example.reporterapp.R
import android.os.Bundle
import com.example.reporterapp.ArticleStatus
import com.example.reporterapp.callback.CountCallback
import com.example.reporterapp.callback.QueryCallback

const val ARTICLE_STATUS = "article_status"

class DashboardPagerAdapter(
    private val myContext: Context,
    fm: FragmentManager,
    private var totalTabs: Int
) : FragmentPagerAdapter(fm), CountCallback {

    var list = arrayOfNulls<TextView>(totalTabs)
    var queryCallback = arrayOfNulls<QueryCallback>(totalTabs)

    fun getTabView(position: Int): View {
        val tab = LayoutInflater.from(myContext).inflate(R.layout.tab_article_work, null)
        tab.findViewById<TextView>(R.id.tvTabTitle).text = getPageTitle(position)
        tab.findViewById<TextView>(R.id.tvTabItemCount).text = "0"
        list[position] = tab.findViewById(R.id.tvTabItemCount)
        return tab
    }
    override fun result(position: Int, count: Int) {
        list[position]?.visibility = View.VISIBLE
        list[position]?.text = "$count"
    }
    override fun getItem(position: Int): Fragment? {
        val bundle = Bundle()
        bundle.putInt(ARTICLE_STATUS, position)
        val article = ArticleFragment()
        queryCallback[position] = article
        article.countCallback = this
        article.arguments = bundle
        return article
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ArticleStatus.values()[position].caption
    }

    override fun getCount(): Int {
        return totalTabs
    }
}