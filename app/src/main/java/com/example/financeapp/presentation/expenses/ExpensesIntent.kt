package com.example.financeapp.presentation.expenses

sealed interface ExpensesIntent {
    data object Retry : ExpensesIntent
}
