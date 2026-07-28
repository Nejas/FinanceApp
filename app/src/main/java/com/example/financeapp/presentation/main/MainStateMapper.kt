package com.example.financeapp.presentation.main

import com.example.financeapp.domain.model.TransactionCategorySummary
import com.example.financeapp.domain.model.AccountSummary
import com.example.financeapp.presentation.accounts.AccountsState
import com.example.financeapp.presentation.common.model.TransactionsSectionState
import com.example.financeapp.presentation.expenses.ExpensesState
import com.example.financeapp.presentation.income.IncomeState

internal fun TransactionCategorySummary.toExpensesState(): ExpensesState {
    return toTransactionsSectionState()
}

internal fun TransactionCategorySummary.toIncomeState(): IncomeState {
    return toTransactionsSectionState()
}

private fun TransactionCategorySummary.toTransactionsSectionState(): TransactionsSectionState {
    return TransactionsSectionState(
        transactions = overview.transactions,
        categoriesById = categories.associateBy { category -> category.id },
        total = overview.total,
        isLoading = false,
        hasLoaded = true,
        error = null
    )
}

internal fun AccountSummary.toAccountsState(): AccountsState {
    return AccountsState(
        accounts = accounts,
        totalBalance = totalBalance,
        isLoading = false,
        hasLoaded = true,
        error = null
    )
}
