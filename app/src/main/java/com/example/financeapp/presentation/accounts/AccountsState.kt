package com.example.financeapp.presentation.accounts

import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.Money
import com.example.financeapp.presentation.common.placeholders.ScreenError

data class AccountsState(
    val accounts: List<Account> = emptyList(),
    val totalBalance: Money = Money(amountInMinorUnits = 0),
    val isLoading: Boolean = false,
    val error: ScreenError? = null
)
