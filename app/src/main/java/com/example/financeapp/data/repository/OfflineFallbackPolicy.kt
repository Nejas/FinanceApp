package com.example.financeapp.data.repository

import com.example.financeapp.data.error.NetworkDataException

internal fun Throwable.canReadFromLocalCache(isOnline: Boolean): Boolean {
    return !isOnline || this is NetworkDataException.Network
}
