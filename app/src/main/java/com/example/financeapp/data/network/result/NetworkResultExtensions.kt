package com.example.financeapp.data.network.result

fun NetworkResult<*>.isRetryable(): Boolean {
    return when (this) {
        is NetworkResult.Success -> false
        is NetworkResult.NetworkError,
        is NetworkResult.TimeoutError -> true
        is NetworkResult.HttpError -> code == HTTP_INTERNAL_SERVER_ERROR
        is NetworkResult.SerializationError,
        is NetworkResult.UnknownError -> false
    }
}

private const val HTTP_INTERNAL_SERVER_ERROR = 500
