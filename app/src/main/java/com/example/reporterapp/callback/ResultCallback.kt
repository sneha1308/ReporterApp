package com.example.reporterapp.callback

interface ResultCallback {
    fun result(isLast:Boolean=false, mobile:String)
}