package com.example.financeapp.presentation.analytics

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.financeapp.presentation.common.network.LifecycleNetworkRefreshEffect
import com.example.financeapp.presentation.common.network.NetworkStatusViewModel
@Composable
fun AnalyticsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = hiltViewModel(),
    networkStatusViewModel: NetworkStatusViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isOnline by networkStatusViewModel.isOnline.collectAsState()

    LifecycleNetworkRefreshEffect(
        refreshable = viewModel,
        isOnline = isOnline,
        refreshImmediately = false
    )

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AnalyticsEffect.NavigateBack -> onBack()
            }
        }
    }

    AnalyticsScreen(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent
    )
}
