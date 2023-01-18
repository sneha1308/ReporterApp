package com.example.reporterapp.adapter

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.reporterapp.NOTIFICATION_STATUS
import com.example.reporterapp.R
import com.example.reporterapp.fragment.NotificationFragment


class NotificationPagerAdapter(private val myContext: Context, fm: FragmentManager, private var totalTabs: Int) : FragmentPagerAdapter(fm) {

    private val tvTabTitles = arrayOf("UNREAD", "ALL")

    fun getTabView(position: Int): View {
        val tab = LayoutInflater.from(myContext).inflate(R.layout.tab_article_work, null)
        val notificationTabTitle = tab.findViewById<TextView>(R.id.tvTabTitle)
        val tvTabItemCount = tab.findViewById<TextView>(R.id.tvTabItemCount)
        notificationTabTitle.text = getPageTitle(position)

        if (getPageTitle(position) == tvTabTitles[1]) {
            tvTabItemCount.visibility = View.GONE
        } else {
            tvTabItemCount.visibility = View.VISIBLE
            tvTabItemCount.text = "0"
        }
        return tab
    }

    override fun getItem(p0: Int): Fragment {
        val bundle = Bundle()
        bundle.putInt(NOTIFICATION_STATUS, p0)
        return NotificationFragment()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tvTabTitles[position]
    }

    override fun getCount(): Int {
        return totalTabs
    }

}