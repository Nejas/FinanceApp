package com.example.financeapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppNavGraph(
    currentRoute: AppRoute,
    modifier: Modifier = Modifier,
    expensesContent: @Composable (Modifier) -> Unit,
    incomeContent: @Composable (Modifier) -> Unit,
    accountsContent: @Composable (Modifier) -> Unit,
    analyticsContent: @Composable (Modifier) -> Unit = {},
) {
    when (currentRoute) {
        AppRoute.Expenses -> expensesContent(modifier)
        AppRoute.Income -> incomeContent(modifier)
        AppRoute.Accounts -> accountsContent(modifier)
        AppRoute.Analytics -> analyticsContent(modifier)
    }
}
