package com.example.reporterapp.app

import android.content.Context
import com.twitter.sdk.android.core.SessionManager
import org.json.JSONObject

class SharedPreferences {

    companion object {

        private var instance: SharedPreferences? = null

        fun getInstance(context: Context): SharedPreferences {
            if (instance == null) {
                instance = getInstance(context)
            }
            return instance as SharedPreferences
        }

        private val USER_ID = "user_id"
    }

}