package com.example.financeapp.domain.model.common

import com.example.financeapp.domain.model.Currency
import java.math.BigDecimal

interface MoneyAmount {

    val amount: BigDecimal
    val currency: Currency
}
