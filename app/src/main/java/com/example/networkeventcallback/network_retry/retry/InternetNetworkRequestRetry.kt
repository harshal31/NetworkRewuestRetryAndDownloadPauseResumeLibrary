/**
 * Copyright 2023 Lenovo, All Rights Reserved *
 */
package com.example.networkeventcallback.network_retry.retry

import com.example.networkeventcallback.CustomNetworkRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InternetNetworkRequestRetry(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val networkFailureRetry: CustomNetworkRequest.RequestRetryAttempt
) : RetryStrategy {
    private val networkRetryScope = CoroutineScope(dispatcher)
    private var job: Job? = null
    private var count = 0

    override fun processRetry(block: () -> Unit) {
        if (count < networkFailureRetry.attempt) {
            ++count
            job = networkRetryScope.launch {
                delay(networkFailureRetry.requestIntervalInMillis)
                block()
            }
        }
    }

    override fun cancelRetry() {
        job?.let {
            it.cancel()
            count = 0
        }
    }
}