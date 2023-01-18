package com.example.reporterapp.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.reporterapp.R
import com.example.reporterapp.app.PreferenceManager
import com.example.reporterapp.getLocalTime
import com.example.reporterapp.localToUTC
import com.example.reporterapp.localToUTCForArticles

class SplashActivity : AppCompatActivity() {

    private lateinit var mDelayHandler: Handler
    private val splashDelay = 3000L

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {
            val intent = Intent(
                applicationContext,
                if (PreferenceManager.getInstance(this).token == null) SignInSignUpActivity::class.java else MainActivity::class.java
            ).apply {
                putExtra("localTime", if (PreferenceManager.getInstance(applicationContext).refresh_date == null ) localToUTCForArticles() else
                    PreferenceManager.getInstance(applicationContext).refresh_date)
            }
            intent.putExtra("clearData", true)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        mDelayHandler = Handler()
        mDelayHandler.postDelayed(mRunnable, splashDelay)
    }

    public override fun onDestroy() {
        mDelayHandler.removeCallbacks(mRunnable)
        super.onDestroy()
    }
}
