/**
 * Copyright 2023 Lenovo, All Rights Reserved *
 */
package com.example.networkeventcallback.network_retry.custom_network_request_impl

import com.example.networkeventcallback.CustomNetworkRequest
import com.example.networkeventcallback.network_retry.network_base_call.ResponseState
import com.example.networkeventcallback.network_retry.network_base_call.coroutineApiCall
import com.example.networkeventcallback.network_retry.retry.InternetNetworkRequestRetry
import com.example.networkeventcallback.network_retry.retry.RetryStrategy
import com.example.networkeventcallback.network_retry.retry.ServerFailureRequestRetry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CustomNetworkRequestImpl<T>(
    private val requestScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val retroFunction: suspend () -> Response<T>,
    private val networkRequestResponse: (ResponseState<T>) -> Unit,
    private val networkRequestRetryAttempt: CustomNetworkRequest.RequestRetryAttempt,
    private val networkRequestFailureRetryAttempt: CustomNetworkRequest.RequestRetryAttempt
) : CustomNetworkRequest {

    private val NETWORK_FAILURE: String = "502"
    private val SERVER_FAILURE: String = "500"
    private val networkFailureRetry: RetryStrategy = InternetNetworkRequestRetry(networkFailureRetry = networkRequestRetryAttempt)
    private val serverFailureRetry: RetryStrategy = ServerFailureRequestRetry(serverFailureRetry = networkRequestFailureRetryAttempt)

    init {
        makeNetworkRequest()
    }

    private fun makeNetworkRequest() {

        requestScope.launch(dispatcher) {

            postResponseToMainThread {
                networkRequestResponse.invoke(ResponseState.Loading())
            }

            val response = coroutineApiCall { retroFunction() }

            if (response is ResponseState.Failure) {
                when (response.responseCode) {
                    NETWORK_FAILURE -> {
                        postResponseToMainThread {
                            networkRequestResponse.invoke(response)
                        }
                        if (networkRequestRetryAttempt.attempt > 0) {
                            networkFailureRetry.processRetry {
                                makeNetworkRequest()
                            }
                        }
                    }

                    SERVER_FAILURE -> {
                        postResponseToMainThread {
                            networkRequestResponse.invoke(response)
                        }
                        if (networkRequestFailureRetryAttempt.attempt > 0) {
                            serverFailureRetry.processRetry {
                                makeNetworkRequest()
                            }
                        }
                    }
                }
            } else {
                serverFailureRetry.cancelRetry()
                networkFailureRetry.cancelRetry()
                postResponseToMainThread {
                    networkRequestResponse.invoke(response)
                }
            }
        }
    }

    suspend fun postResponseToMainThread(block: () -> Unit) {
        withContext(Dispatchers.Main) {
            block()
        }
    }

    override fun cancelNetworkRetryAttempt() {
        networkFailureRetry.cancelRetry()
    }

    override fun cancelServerRetryAttempt() {
        serverFailureRetry.cancelRetry()
    }
}