package com.example.financeapp.presentation.main

import com.example.financeapp.presentation.accounts.AccountsState
import com.example.financeapp.presentation.expenses.ExpensesState
import com.example.financeapp.presentation.income.IncomeState
import java.time.LocalDate

data class MainState(
    val selectedDate: LocalDate = LocalDate.now(),
    val expensesState: ExpensesState = ExpensesState(),
    val incomeState: IncomeState = IncomeState(),
    val accountsState: AccountsState = AccountsState()
)
