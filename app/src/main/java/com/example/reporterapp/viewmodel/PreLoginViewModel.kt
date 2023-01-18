package com.example.reporterapp.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.reporterapp.activity.SignInSignUpActivity
import com.example.reporterapp.api.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.example.reporterapp.*
import com.example.reporterapp.app.PreferenceManager
import com.example.reporterapp.callback.IRegister
import com.example.reporterapp.callback.LoginResponse
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class PreLoginViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private val apolloClient by lazy { ApolloClient.setupApollo() }

    fun registerUser(
        phoneNumber: String,
        password: String,
        userName: String,
        location: String,
        emailId: String,
        countryName: String,
        iRegister: IRegister) =
        scope.launch(Dispatchers.IO) {

            val deferred = apolloClient.mutate(
                CreateUserMutation.builder().email(emailId).mobilenumber(phoneNumber).password(password).location(location).
                    username(userName).role("Reporter").countryCode(countryName).build()
            ).toDeferred()
            val response = deferred.await()

            if (response.errors().size == 0) {
                iRegister.onRegisterSuccess()
            }
            else{
                iRegister.onRegisterFailure(response.errors().get(0).message().toString())
            }
        }

    fun logIn(userName: String, password: String, loginResponse: LoginResponse,
              activity: SignInSignUpActivity) = scope.launch(Dispatchers.IO) {
       /* if (!isOnline(getApplication())){
            Toast.makeText(getApplication(),"Please check Internet Connection",Toast.LENGTH_SHORT).show()
            return@launch
        }*/
        if (!isOnline(getApplication())){
            val snack = Snackbar.make(View(getApplication()),"Please check Internet Connection", Snackbar.LENGTH_LONG)
            snack.view.setBackgroundResource(R.color.colorPrimary);
            snack.show()
            return@launch
        }
        try {
            val deferred = apolloClient.mutate(
                LoginMutation.builder().username(userName)
                    .password(password).build()
            ).toDeferred()
            val response = deferred.await()

            if(response.errors().size == 0) {
                if(!response.data()?.login()?.user()?.user()?.role()?.role()?.toString().equals(activity.resources.getString(R.string.reporter_name),ignoreCase = true)){
                    loginResponse.fail("Invalid email id/ Mobile number or password")
                }
                else {
                    val model: CreateArticleViewModel =
                        ViewModelProviders.of(activity).get(CreateArticleViewModel::class.java)
                    val validateEmail = response.data()?.login()?.user()?.user()?.email() ?: ""
                    if (PreferenceManager.getInstance(getApplication()).userEmailId == null || PreferenceManager.getInstance(
                            getApplication()
                        ).userEmailId != validateEmail
                    ) {
                        model.deleteAllArticle()
                    }
                    PreferenceManager.getInstance(getApplication())
                        .setAppToken(response.data()?.login()?.user()?.token().toString())
                    response.data()?.login()?.user()?.user()?.isVerifiedByAdmin?.let {
                        PreferenceManager.getInstance(getApplication())
                            .setIsUserVerified(it)
                    }
                    PreferenceManager.getInstance(getApplication()).setUserId(validateEmail)
                    loginResponse.success()
                    return@launch
                }
            }
            else{
                loginResponse.fail(response.errors().get(0).message().toString())
            }
        } catch (e: ApolloException) {
            loginResponse.fail(e.message ?: "")
        } catch (e: NullPointerException) {
            loginResponse.fail(e.message ?: "")
        } finally {
        }
    }

    fun genrateOtp(
        phoneNumber: String,
        countryName: String,
        isResend: Boolean,
        isForgot: Boolean,
        iRegister: IRegister) =
        scope.launch(Dispatchers.IO) {

            val deferred = apolloClient.mutate(
                GenerateOtpMutation.builder().mobile(phoneNumber).country(countryName).isResend(isResend).isForgot(isForgot).build()
            ).toDeferred()
            val response = deferred.await()

            if (response.errors().size == 0) {
                iRegister.onRegisterSuccess()
            }
            else
                iRegister.onRegisterFailure(response.errors().get(0).message().toString())
        }

    fun verifyOtp(
        phoneNumber: String,
        otp: String,
        isForgot: Boolean,
        iRegister: IRegister) =
        scope.launch(Dispatchers.IO) {

            val deferred = apolloClient.mutate(
                VerifyOtpMutation.builder().mobile(phoneNumber).otp(otp).isForgot(isForgot).build()
            ).toDeferred()
            val response = deferred.await()

            if (response.errors().size == 0) {
                iRegister.onRegisterSuccess()
            }
            else
                iRegister.onRegisterFailure(response.errors().get(0).message().toString())
        }

    fun resetPassword(
        phoneNumber: String,
        password: String,
        iRegister: IRegister) =
        scope.launch(Dispatchers.IO) {

            val deferred = apolloClient.mutate(
                ResetPasswordMutation.builder().mobile(phoneNumber).newPassword(password).build()
            ).toDeferred()
            val response = deferred.await()

            if (response.errors().size == 0) {
                iRegister.onRegisterSuccess()
            }
            else
                iRegister.onRegisterFailure(response.errors().get(0).message().toString())
        }

    fun socialLogin(
        accessToken: String,
        provider: String,
        loginResponse: LoginResponse,
        activity: SignInSignUpActivity) =
        scope.launch(Dispatchers.IO) {

            val deferred = apolloClient.mutate(
                SocialAuthMutation.builder().accessToken(accessToken).provider(provider).build()
            ).toDeferred()
            val response = deferred.await()

            if(response.errors().size == 0) {
                if(!response.data()?.socialAuth()?.social()?.user()?.role()?.role()?.toString().equals(activity.resources.getString(R.string.reporter_name),ignoreCase = true)){
                    loginResponse.fail("Invalid email id/ Mobile number or password")
                }
                else {
                    val model: CreateArticleViewModel =
                        ViewModelProviders.of(activity).get(CreateArticleViewModel::class.java)
                    val validateEmail = response.data()?.socialAuth()?.social()?.user()?.email() ?: ""
                    if (PreferenceManager.getInstance(getApplication()).userEmailId == null || PreferenceManager.getInstance(
                            getApplication()
                        ).userEmailId != validateEmail
                    ) {
                        model.deleteAllArticle()
                    }
                    PreferenceManager.getInstance(getApplication())
                        .setAppToken(response.data()?.socialAuth()?.token().toString())
                    PreferenceManager.getInstance(getApplication()).setUserId(validateEmail)
                    loginResponse.success()
                    return@launch
                }
            }
            else{
                loginResponse.fail(response.errors().get(0).message().toString())
            }
        }

}

