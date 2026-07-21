package com.example.financeapp.core.coroutines

import kotlinx.coroutines.CancellationException

suspend inline fun <T> suspendRunCatching(
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
