package com.example.financeapp.domain.model

import com.example.financeapp.domain.model.common.FinancialAccountPayload

data class FinancialAccountPayloadData(
    override val name: String,
    override val emoji: String? = null,
    override val balance: Money,
    override val currency: Currency
) : FinancialAccountPayload
