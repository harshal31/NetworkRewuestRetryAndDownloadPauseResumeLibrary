/**
 * Copyright 2023 Lenovo, All Rights Reserved *
 */
package com.example.networkeventcallback.network_retry.retry

interface RetryStrategy {
    fun processRetry(block: () -> Unit)
    fun cancelRetry()
}