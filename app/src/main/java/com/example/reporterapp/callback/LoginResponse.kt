package com.example.reporterapp.callback

interface LoginResponse{

    fun success()
    fun fail(e:String)
    fun displayError(message: String)

}