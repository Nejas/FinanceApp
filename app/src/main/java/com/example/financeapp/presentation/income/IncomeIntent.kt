package com.example.financeapp.presentation.income

sealed interface IncomeIntent {
    data object Retry : IncomeIntent
}
