package com.example.reporterapp.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import com.example.reporterapp.fragment.SignInFragment
import com.example.reporterapp.fragment.SignUpFragment

class SignInSignUpPagerAdapter(private val myContext: Context, fm: FragmentManager, private var totalTabs: Int) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> {
                SignInFragment()
            }
            1 -> {
                SignUpFragment()
            }
            else -> null
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }

}