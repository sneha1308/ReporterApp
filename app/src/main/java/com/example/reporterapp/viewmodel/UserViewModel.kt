package com.example.reporterapp.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.widget.Toast
import com.example.reporterapp.UserRepository
import com.example.reporterapp.callback.LoginResponse
import com.example.reporterapp.database.AppDatabase
import com.example.reporterapp.database.table.User
import com.example.reporterapp.isOnline
import com.example.reporterapp.showMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private val userRepository: UserRepository
    private val postLoginViewModel: PostLoginViewModel

    var searchByLiveData: LiveData<User>? = null
    private val filterLiveData = MutableLiveData<Int>()

    init {

        val userDao = AppDatabase.getDatabase(application, scope).userDao()
        userRepository = UserRepository(userDao)

        postLoginViewModel = PostLoginViewModel(this)
        searchByLiveData = Transformations.switchMap(filterLiveData) { v ->
            userRepository.getUser()
        }

    }

    fun setData() {
        // Toast.makeText(getApplication(),"setData$id",Toast.LENGTH_SHORT).show();
        this.filterLiveData.value
    }

    fun getData() {
        searchByLiveData = userRepository.getUser()
    }

    fun fetchData() {
        if (isOnline(getApplication())) {
            postLoginViewModel.getProfileDetails()
        } else
            showMsg(getApplication(), "Please check Internet Connection", Toast.LENGTH_SHORT)
        return

    }

    fun updateContactInfo(user: User) {
        postLoginViewModel.updateContactInfo(user)
    }

    fun updateProfilePhoto(user: User) {
        postLoginViewModel.updateProfilePhoto(user)
    }

    fun updateLinks(user: User) {
        postLoginViewModel.updateLinks(user)
    }

    fun changepassword(
        oldPass: String,
        newPass: String,
        callBackResponse: LoginResponse
    ) {

        postLoginViewModel.changepassword(oldPass, newPass, callBackResponse)
    }


    fun insert(category: User) = scope.launch(Dispatchers.IO) {
        userRepository.insert(category)
    }

    fun deleteUser() = scope.launch(Dispatchers.IO) {
        userRepository.deleteAll()
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}