package com.example.financeapp.presentation.common.network

import androidx.lifecycle.ViewModel
import com.example.financeapp.core.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NetworkStatusViewModel @Inject constructor(
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline = networkMonitor.isOnline
}
