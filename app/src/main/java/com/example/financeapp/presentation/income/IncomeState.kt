package com.example.financeapp.presentation.income

import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.presentation.common.placeholders.ScreenError

data class IncomeState(
    val transactions: List<Transaction> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
    val total: Money = Money(amountInMinorUnits = 0),
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: ScreenError? = null
)
