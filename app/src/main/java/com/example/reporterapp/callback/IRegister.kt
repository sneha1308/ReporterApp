package com.example.reporterapp.callback

interface IRegister {
    fun onRegisterSuccess()

    fun onRegisterFailure(message: String)

    fun displayError(message: String)
}