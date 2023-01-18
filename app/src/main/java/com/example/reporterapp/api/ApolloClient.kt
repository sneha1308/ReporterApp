package com.example.reporterapp.api

import android.provider.Settings.System.DATE_FORMAT
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import com.example.reporterapp.type.CustomType
import okhttp3.OkHttpClient
import org.jetbrains.annotations.NotNull
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit





object ApolloClient {
    fun setupApollo(): ApolloClient {
        return ApolloClient.builder()
            //.serverUrl("http://111.93.2.184/graphql/")
            .serverUrl("https://api.totaltelugu.com/api/v1/reporter/")
            //.serverUrl("http://111.93.2.184/api/v1/reporter/")
            //.addCustomTypeAdapter(CustomType.DATETIME, jsonCustomTypeAdapter)
            .addCustomTypeAdapter(CustomType.DATETIME, dateCustomTypeAdapter)
            .okHttpClient(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val builder = original.newBuilder().method(
                            original.method(),
                            original.body()
                        )
                        chain.proceed(builder.build())
                    }
                    .build()
            )
            .build()
    }
    private val jsonCustomTypeAdapter = object : CustomTypeAdapter<JSONObject> {
        override fun decode(value: CustomTypeValue<*>): JSONObject {
            return JSONObject()
        }

        override fun encode(value: JSONObject): CustomTypeValue<*> {
            return CustomTypeValue.GraphQLJsonString(value.toString())
        }
    }


    val dateCustomTypeAdapter = object :CustomTypeAdapter<String> {
            override fun decode(value: CustomTypeValue<*>):String {
                try {
                    var date = value.value.toString()
                    println("=======================$date")
                    return date
                } catch (e:ParseException) {
                    throw RuntimeException(e);
                }
            }

        override fun encode(value: String):CustomTypeValue<*> {
                return CustomTypeValue.GraphQLString(value);
            }
        }
}