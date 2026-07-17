package com.example.financeapp.domain.model.common

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money

interface FinancialAccountPayload : Named {

    val emoji: String?
    val balance: Money
    val currency: Currency
}
