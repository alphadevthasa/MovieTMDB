package com.thasa.movietmdb.core.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.thasa.movietmdb.core.network.api.TmdbApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

fun MockWebServer.tmdbApi(): TmdbApi = Retrofit.Builder()
    .baseUrl(url("/"))
    .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()))
    .build()
    .create(TmdbApi::class.java)

fun MockWebServer.enqueueJson(body: String, code: Int = 200) {
    enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body))
}

fun moviePageJson(page: Int, totalPages: Int, vararg ids: Int): String {
    val items = ids.joinToString(",") {
        """{"id":$it,"title":"Movie $it","poster_path":null,"vote_average":7.0,"release_date":"2020-01-01"}"""
    }
    return """{"page":$page,"total_pages":$totalPages,"results":[$items]}"""
}

fun reviewPageJson(page: Int, totalPages: Int, vararg ids: String): String {
    val items = ids.joinToString(",") {
        """{"id":"$it","author":"a$it","author_details":{"avatar_path":null,"rating":null},"content":"c","created_at":"2020-01-01T00:00:00.000Z"}"""
    }
    return """{"page":$page,"total_pages":$totalPages,"results":[$items]}"""
}
