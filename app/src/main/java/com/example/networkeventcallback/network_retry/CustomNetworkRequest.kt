/**
 * Copyright 2023 Lenovo, All Rights Reserved *
 */
package com.example.networkeventcallback

import com.example.networkeventcallback.network_retry.custom_network_request_impl.CustomNetworkRequestImpl
import com.example.networkeventcallback.network_retry.network_base_call.ResponseState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Response

interface CustomNetworkRequest {

    data class RequestRetryAttempt(val attempt: Int = 0, val requestIntervalInMillis: Long = 0L)

    fun cancelNetworkRetryAttempt()

    fun cancelServerRetryAttempt()

    data class Builder<T>(
        private var requestScope: CoroutineScope? = null,
        private var dispatcher: CoroutineDispatcher = Dispatchers.Default,
        private var retroFunction: (suspend () -> Response<T>)? = null,
        private var networkRequestResponse: ((ResponseState<T>) -> Unit)? = null,
        private var networkRequestRetryAttempt: RequestRetryAttempt = RequestRetryAttempt(),
        private var serverFailureretryAttempt: RequestRetryAttempt = RequestRetryAttempt()
    ) {
        fun addCoroutineScope(scope: CoroutineScope) = apply {
            this.requestScope = scope
        }

        fun addNetworkRetryAttempt(attempt: RequestRetryAttempt) = apply {
            this.networkRequestRetryAttempt = attempt
        }

        fun addServerFailureRetryAttempt(attempt: RequestRetryAttempt) = apply {
            this.serverFailureretryAttempt = attempt
        }

        fun addRetrofitFunction(retroFunction: suspend () -> Response<T>) = apply {
            this.retroFunction = retroFunction
        }

        fun addCoroutineDispatcher(dispatcher: CoroutineDispatcher) = apply {
            this.dispatcher = dispatcher
        }

        fun addNetworkResponseCallback(block: (ResponseState<T>) -> Unit) = apply {
            this.networkRequestResponse = block
        }

        fun build(): CustomNetworkRequest  {
            return when {
                requestScope == null -> throw Exception("Need coroutine scope to make network request")
                retroFunction == null -> throw Exception("Add network request")
                networkRequestResponse == null -> throw Exception("Need to add network response to get callback")
                else -> CustomNetworkRequestImpl(
                    requestScope!!,
                    dispatcher,
                    retroFunction!!,
                    networkRequestResponse!!,
                    networkRequestRetryAttempt,
                    serverFailureretryAttempt
                )
            }
        }
    }
}