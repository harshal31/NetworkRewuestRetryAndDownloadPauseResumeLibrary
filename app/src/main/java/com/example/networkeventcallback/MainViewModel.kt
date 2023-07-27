/**
 * Copyright 2023 Lenovo, All Rights Reserved *
 */
package com.example.networkeventcallback

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkeventcallback.network_retry.retrofit.ApiCall
import com.example.networkeventcallback.network_retry.retrofit.PostsItem
import kotlinx.coroutines.Dispatchers

class MainViewModel: ViewModel() {

    var isDownloadStart = true

    init {
        callPosts()
    }

    private fun callPosts() {
        val network = CustomNetworkRequest.Builder<List<PostsItem>>()
            .addCoroutineScope(viewModelScope)
            .addCoroutineDispatcher(Dispatchers.Default)
            .addNetworkRetryAttempt(CustomNetworkRequest.RequestRetryAttempt(2, 1000))
            .addRetrofitFunction { ApiCall.getRetrofit().getPosts() }
            .addNetworkResponseCallback {
                Log.d(MainViewModel::class.java.simpleName, "response $it")
            }
            .build()
    }
}

