package com.example.financeapp.presentation.common.placeholders

import com.example.financeapp.data.error.NetworkDataException

fun Throwable.toScreenError(
    isOnline: Boolean
): ScreenError {
    if (!isOnline) {
        return ScreenError.NO_INTERNET
    }

    return when (this) {
        is NetworkDataException.Network -> ScreenError.NO_INTERNET
        is NetworkDataException.Timeout -> ScreenError.TIMEOUT
        is NetworkDataException.Http -> if (code == HTTP_INTERNAL_SERVER_ERROR) {
            ScreenError.SERVER_ERROR
        } else {
            ScreenError.LOAD_FAILED
        }
        else -> ScreenError.LOAD_FAILED
    }
}

private const val HTTP_INTERNAL_SERVER_ERROR = 500
