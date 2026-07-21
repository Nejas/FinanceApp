package com.example.financeapp.data.network.result

data class RetryPolicy(
    val maxRetries: Int = DEFAULT_MAX_RETRIES,
    val requestTimeoutMillis: Long = DEFAULT_REQUEST_TIMEOUT_MILLIS,
    val retryDelayMillis: Long = DEFAULT_RETRY_DELAY_MILLIS
) {

    init {
        require(maxRetries >= 0) { "maxRetries не может быть отрицательным" }
        require(requestTimeoutMillis > 0) { "requestTimeoutMillis должен быть больше 0" }
        require(retryDelayMillis >= 0) { "retryDelayMillis не может быть отрицательным" }
    }

    private companion object {
        const val DEFAULT_MAX_RETRIES = 3
        const val DEFAULT_REQUEST_TIMEOUT_MILLIS = 15_000L
        const val DEFAULT_RETRY_DELAY_MILLIS = 2_000L
    }
}
