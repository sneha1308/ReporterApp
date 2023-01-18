package com.example.reporterapp.api

import com.example.reporterapp.database.table.Article
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

interface RetrofitService {
    @GET("/posts")
    fun getPosts(): Deferred<Response<List<Article>>>
}