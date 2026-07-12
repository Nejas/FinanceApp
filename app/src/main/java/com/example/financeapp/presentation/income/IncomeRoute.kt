package com.example.financeapp.presentation.income

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate

@Composable
fun IncomeRoute(
    selectedDate: LocalDate,
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(selectedDate) {
        viewModel.onIntent(IncomeIntent.DateSelected(selectedDate))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                IncomeEffect.OpenAnalytics -> onOpenAnalytics()
                IncomeEffect.OpenSettings -> onOpenSettings()
                is IncomeEffect.OpenTransaction -> Unit
            }
        }
    }

    IncomeScreen(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent
    )
}
