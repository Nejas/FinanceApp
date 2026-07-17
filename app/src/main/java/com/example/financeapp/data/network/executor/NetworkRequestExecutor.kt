package com.example.financeapp.data.network.executor

import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.network.result.RetryPolicy
import com.example.financeapp.data.network.result.isRetryable
import com.example.financeapp.data.network.result.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout

@Singleton
class NetworkRequestExecutor @Inject constructor(
    private val retryPolicy: RetryPolicy,
    private val semaphore: Semaphore
) : NetworkCallExecutor {

    override suspend fun <T> execute(
        call: suspend () -> T
    ): NetworkResult<T> {
        return semaphore.withPermit {
            executeWithRetry(call)
        }
    }

    private suspend fun <T> executeWithRetry(
        call: suspend () -> T
    ): NetworkResult<T> {
        var attempt = 1
        var delayMillis = retryPolicy.initialDelayMillis
        var lastResult: NetworkResult<T>

        while (true) {
            lastResult = safeApiCall {
                withTimeout(retryPolicy.requestTimeoutMillis) {
                    call()
                }
            }

            if (lastResult is NetworkResult.Success) {
                return lastResult
            }

            if (attempt >= retryPolicy.maxAttempts || !lastResult.isRetryable()) {
                return lastResult
            }

            if (delayMillis > 0) {
                delay(delayMillis)
            }

            delayMillis = (delayMillis * retryPolicy.backoffMultiplier)
                .roundToLong()
                .coerceAtMost(retryPolicy.maxDelayMillis)
            attempt++
        }
    }
}
