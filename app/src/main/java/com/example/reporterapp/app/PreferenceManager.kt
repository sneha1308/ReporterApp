package com.example.reporterapp.app

import android.content.Context
import android.content.SharedPreferences

const val IS_USER_VERIFIED = "verified"
const val APP_TOKEN = "token"
const val USER_EMAIL_ID = "email"
const val MY_PREFS_NAME = "pref_name"

const val ARTICLE_PREFS_DATE="pref_date"
const val ARTICLE_PREFS_TIME="pref_time"
const val ARTICLE_PREFS_REFRESHDATE="pref_date"

class PreferenceManager(context: Context) {


    var articleDateStamp :String=""
    var articleTimeStamp : String=""

    var sharedPrefs: SharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)

    var isVerified: Boolean? = null
    var token: String? = null
    var userEmailId: String? = null
    var refresh_date: String? = null

    companion object {

        private var instance: PreferenceManager? = null
        fun getInstance(context: Context): PreferenceManager {
            if (instance == null) {
                instance = PreferenceManager(context)
            }
            return instance as PreferenceManager
        }
    }

    init {

        isVerified = sharedPrefs.getBoolean(IS_USER_VERIFIED, false)
        token = sharedPrefs.getString(APP_TOKEN, null)
        userEmailId = sharedPrefs.getString(USER_EMAIL_ID, null)
        refresh_date = sharedPrefs.getString(ARTICLE_PREFS_REFRESHDATE, null)
    }

    fun setIsUserVerified(isVerified: Boolean) {
        val prefs = sharedPrefs.edit()
        prefs.putBoolean(IS_USER_VERIFIED, isVerified)
        prefs.apply()
        this.isVerified = isVerified
    }

    fun setAppToken(token: String) {
        val prefs = sharedPrefs.edit()
        prefs.putString(APP_TOKEN, token)
        prefs.apply()
        this.token = token
    }

    fun setUserId(userId: String) {
        val prefs = sharedPrefs.edit()
        prefs.putString(USER_EMAIL_ID, userId)
        prefs.apply()
        this.userEmailId = userId
    }

    fun setRefreshDate(date: String) {
        val prefs = sharedPrefs.edit()
        prefs.putString(ARTICLE_PREFS_REFRESHDATE, date)
        prefs.apply()
        this.refresh_date = date
    }

    fun clearUserVerifiedFlag() {
        val prefs = sharedPrefs.edit()
        prefs.putBoolean(IS_USER_VERIFIED, false)
        prefs.apply()
        this.isVerified = null
    }

    fun clearToken() {
        val prefs = sharedPrefs.edit()
        prefs.putString(APP_TOKEN, null)
        prefs.apply()
        this.token = null
    }

    fun clearRefreshDate() {
        val prefs = sharedPrefs.edit()
        prefs.putString(ARTICLE_PREFS_REFRESHDATE, null)
        prefs.apply()
        this.refresh_date = null
    }

    fun updateTimeStamp(pair: Pair<String,String>){
        val prefs = sharedPrefs.edit()
        prefs.putString(ARTICLE_PREFS_DATE,pair.first)
        prefs.putString(ARTICLE_PREFS_TIME,pair.second)
        prefs.apply()
        articleDateStamp=pair.first
        articleTimeStamp=pair.second
    }


}