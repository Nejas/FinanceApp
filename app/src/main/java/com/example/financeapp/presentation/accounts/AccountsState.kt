package com.example.financeapp.presentation.accounts

import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Money
import com.example.financeapp.presentation.common.mvi.ScreenError

data class AccountsState(
    val accounts: List<FinancialAccount> = emptyList(),
    val totalBalance: Money = Money(amountInMinorUnits = 0),
    val isLoading: Boolean = false,
    val error: ScreenError? = null
)
