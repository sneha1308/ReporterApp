package com.example.reporterapp.activity

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller

class SwipeDisableViewPager : ViewPager {

    var isStopScroll=false

    constructor(context: Context) : super(context) {
        setMyScroller()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setMyScroller()

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (isStopScroll)
            return  false

        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isStopScroll)
            return  false

        return super.onTouchEvent(event)
    }

    private fun setMyScroller() {
        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(this, MyScroller(context))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopScrolling(enabled: Boolean) {
        isStopScroll=enabled
    }

    inner class MyScroller(context: Context) : Scroller(context, DecelerateInterpolator()) {

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            if (!isStopScroll)
                super.startScroll(startX, startY, dx, dy, duration )
        }
    }
}