package com.example.financeapp.presentation.expenses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate

@Composable
fun ExpensesRoute(
    selectedDate: LocalDate,
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(selectedDate) {
        viewModel.onIntent(ExpensesIntent.DateSelected(selectedDate))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ExpensesEffect.OpenAnalytics -> onOpenAnalytics()
                ExpensesEffect.OpenSettings -> onOpenSettings()
                is ExpensesEffect.OpenTransaction -> Unit
            }
        }
    }

    ExpensesScreen(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent
    )
}
