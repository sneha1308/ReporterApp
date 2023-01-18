package com.example.reporterapp.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import com.example.reporterapp.R
import com.example.reporterapp.adapter.NotificationPagerAdapter
import kotlinx.android.synthetic.main.activity_notification.*

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        supportActionBar?.elevation = 0f
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Notifications"

        val notificationTabLayout = notificationTabLayout
        val notificationViewPager = notificationViewPager

        notificationTabLayout.addTab(notificationTabLayout.newTab().setText(R.string.unread))
        notificationTabLayout.addTab(notificationTabLayout.newTab().setText(R.string.all))

        notificationTabLayout.setupWithViewPager(notificationViewPager);

        val adapter = NotificationPagerAdapter(this, supportFragmentManager, 2)
        notificationViewPager.adapter = adapter

        for (i in 0 until notificationTabLayout.tabCount) {
            val tab = notificationTabLayout.getTabAt(i)
            tab?.customView = adapter.getTabView(i)
        }

        notificationViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(notificationTabLayout))
        notificationTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                //       notificationViewPager.currentItem = tab.position
            }
        })
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
