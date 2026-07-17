package com.example.financeapp.data.network.result

data class RetryPolicy(
    val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    val requestTimeoutMillis: Long = DEFAULT_REQUEST_TIMEOUT_MILLIS,
    val initialDelayMillis: Long = DEFAULT_INITIAL_DELAY_MILLIS,
    val maxDelayMillis: Long = DEFAULT_MAX_DELAY_MILLIS,
    val backoffMultiplier: Double = DEFAULT_BACKOFF_MULTIPLIER
) {

    init {
        require(maxAttempts > 0) { "maxAttempts must be greater than 0" }
        require(requestTimeoutMillis > 0) { "requestTimeoutMillis must be greater than 0" }
        require(initialDelayMillis >= 0) { "initialDelayMillis cannot be negative" }
        require(maxDelayMillis >= initialDelayMillis) {
            "maxDelayMillis must be greater than or equal to initialDelayMillis"
        }
        require(backoffMultiplier >= 1.0) { "backoffMultiplier must be greater than or equal to 1.0" }
    }

    private companion object {
        const val DEFAULT_MAX_ATTEMPTS = 3
        const val DEFAULT_REQUEST_TIMEOUT_MILLIS = 15_000L
        const val DEFAULT_INITIAL_DELAY_MILLIS = 500L
        const val DEFAULT_MAX_DELAY_MILLIS = 2_000L
        const val DEFAULT_BACKOFF_MULTIPLIER = 2.0
    }
}
