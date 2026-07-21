package com.example.financeapp.presentation.common.network

interface NetworkRefreshable {

    fun refreshFromNetwork(isSilent: Boolean = false)
}
