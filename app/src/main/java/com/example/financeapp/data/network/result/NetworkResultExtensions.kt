package com.example.financeapp.data.network.result

fun NetworkResult<*>.isRetryable(): Boolean {
    return when (this) {
        is NetworkResult.Success -> false
        is NetworkResult.NetworkError,
        is NetworkResult.TimeoutError -> true
        is NetworkResult.HttpError -> code == HTTP_REQUEST_TIMEOUT ||
            code == HTTP_TOO_MANY_REQUESTS ||
            code in HTTP_SERVER_ERROR_RANGE
        is NetworkResult.SerializationError,
        is NetworkResult.UnknownError -> false
    }
}

private const val HTTP_REQUEST_TIMEOUT = 408
private const val HTTP_TOO_MANY_REQUESTS = 429
private val HTTP_SERVER_ERROR_RANGE = 500..599
