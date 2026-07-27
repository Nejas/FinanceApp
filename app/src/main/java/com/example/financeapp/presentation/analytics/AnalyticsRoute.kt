package com.example.financeapp.presentation.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.financeapp.presentation.analytics.ui.AnalyticsScreen

@Composable
fun AnalyticsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
