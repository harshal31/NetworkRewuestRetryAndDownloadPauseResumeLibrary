/**
 * Copyright 2023 Lenovo, All Rights Reserved *
 */
package com.example.networkeventcallback.network_retry.retrofit

import retrofit2.Response
import retrofit2.http.GET

interface JsonPlaceholderApi {

    @GET("/posts")
    suspend fun getPosts(): Response<List<PostsItem>>

}


