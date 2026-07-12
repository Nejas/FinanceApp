package com.example.financeapp.presentation.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AccountsRoute(
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AccountsEffect.OpenAnalytics -> onOpenAnalytics()
                AccountsEffect.OpenSettings -> onOpenSettings()
                is AccountsEffect.OpenAccount -> Unit
            }
        }
    }

    AccountsScreen(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent
    )
}
