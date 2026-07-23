package com.example.financeapp.presentation.main

import com.example.financeapp.domain.model.CategorizedTransactionsOverview
import com.example.financeapp.domain.model.FinancialAccountsOverview
import com.example.financeapp.presentation.accounts.AccountsState
import com.example.financeapp.presentation.common.model.TransactionsSectionState
import com.example.financeapp.presentation.expenses.ExpensesState
import com.example.financeapp.presentation.income.IncomeState

internal fun CategorizedTransactionsOverview.toExpensesState(): ExpensesState {
    return toTransactionsSectionState()
}

internal fun CategorizedTransactionsOverview.toIncomeState(): IncomeState {
    return toTransactionsSectionState()
}

private fun CategorizedTransactionsOverview.toTransactionsSectionState(): TransactionsSectionState {
    return TransactionsSectionState(
        transactions = overview.transactions,
        categoriesById = categories.associateBy { category -> category.id },
        total = overview.total,
        isLoading = false,
        hasLoaded = true,
        error = null
    )
}

internal fun FinancialAccountsOverview.toAccountsState(): AccountsState {
    return AccountsState(
        accounts = accounts,
        totalBalance = totalBalance,
        isLoading = false,
        error = null
    )
}
