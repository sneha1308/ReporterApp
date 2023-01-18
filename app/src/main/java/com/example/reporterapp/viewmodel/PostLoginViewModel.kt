package com.example.reporterapp.viewmodel


import com.apollographql.apollo.coroutines.toDeferred
import com.example.reporterapp.*
import com.example.reporterapp.api.ApolloClient
import com.example.reporterapp.app.PreferenceManager
import com.example.reporterapp.callback.LoginResponse
import com.example.reporterapp.database.table.User
import com.example.reporterapp.type.ProfileInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PostLoginViewModel( val model: UserViewModel){

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private val apolloClient by lazy { ApolloClient.setupApollo() }
    init {

    }


    fun getProfileDetails() = scope.launch {

        val deferred =  apolloClient.query(GetProfileQuery.builder().token(PreferenceManager.getInstance(model.getApplication()).token?:"").build())
            .toDeferred()
             deferred.await()

        if(deferred.await().errors().size <=0){
                val userProfileDetails = deferred.await().data()?.GetProfile()
                    model.deleteUser()
                    var twitterUrl = ""
                    var linkedInUrl = ""
                    var lastLoginDate: Long? = System.currentTimeMillis()
                    if(userProfileDetails?.lastLogin() != null && userProfileDetails?.lastLogin().toString().isNotEmpty()){
                        lastLoginDate = convertToNewFormat(userProfileDetails?.lastLogin().toString())
                    }
                    for (i in userProfileDetails?.socialSet()!!) {
                        if(i.provider().equals("twitter",true )){
                            twitterUrl = i.url()
                        }else if(i.provider().equals("linkedin",true )){
                            linkedInUrl = i.url()
                        }
                    }
                    if(!userProfileDetails?.lastLogin().toString().isEmpty()){
                        //lastloggedInDate = dateFormatter(userProfileDetails.lastLogin().toString())
                    }
                    model.insert(
                        User(
                            userProfileDetails?.username()?:"",
                            "",
                            userProfileDetails?.location() ?: "",
                            userProfileDetails?.location() ?: "",
                            linkedInUrl,
                            twitterUrl,
                            lastLoginDate!!,
                            userProfileDetails?.mobilenumber(),
                            userProfileDetails.profilephoto(),
                            userProfileDetails.isVerifiedByAdmin


                        )
                    )
                } else{
            //showMsg(model.getApplication(), "Not able to get the user profile details", Toast.LENGTH_SHORT)
        }


    }


    fun updateContactInfo(user: User) = scope.launch {
        val deferred = apolloClient.mutate(
            UpdateProfileMutation.builder().token(PreferenceManager.getInstance(model.getApplication()).token?:"").profile(
                ProfileInput.builder().location(user?.location).build()
            ).build()
        ).toDeferred()

         deferred.await()
        if(deferred.await().errors().size <=0 )
            model.insert(user)
        /*else
            showMsg(model.getApplication(), "Something Went Wrong", Toast.LENGTH_SHORT)*/

    }

    fun updateLinks(user: User) = scope.launch {
        val deferred = apolloClient.mutate(
            UpdateProfileMutation.builder().token(PreferenceManager.getInstance(model.getApplication()).token?:"").profile(
                ProfileInput.builder().linkedin(user?.linkedin).twitter(user?.twitter).build()
            ).build()
        ).toDeferred()

        deferred.await()
        if(deferred.await().errors().size <=0 )
            model.insert(user)
        /*else
            showMsg(model.getApplication(), "Something Went Wrong", Toast.LENGTH_SHORT)*/

    }

    fun changepassword(
        oldPass: String,
        newPass: String,
        callBackResponse: LoginResponse
    ) = scope.launch {
        val deferred = apolloClient.mutate(
            ChangePasswordMutation.builder().token(PreferenceManager.getInstance(model.getApplication()).token?:"").newpassword(newPass)
                .oldpassword(oldPass).build()
        ).toDeferred()

        deferred.await()
        if(deferred.await().errors().size == 0 ){
            callBackResponse.success()
        }
        else{
            deferred.await().errors().get(0).message()?.let { callBackResponse.fail(it) }
        }

    }

    fun updateProfilePhoto(user: User) = scope.launch {
        val deferred = apolloClient.mutate(
            UpdateProfileMutation.builder().token(PreferenceManager.getInstance(model.getApplication()).token?:"").profile(
                ProfileInput.builder().avatar(user.profile_Photo).build()
            ).build()
        ).toDeferred()

        deferred.await()
        if(deferred.await().errors().size <=0 )
            model.insert(user)
        /*else
            showMsg(model.getApplication(), "Something Went Wrong", Toast.LENGTH_SHORT)*/

    }

}