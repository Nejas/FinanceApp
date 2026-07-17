package com.example.financeapp.domain.model

import com.example.financeapp.domain.model.common.DisplayEntity

data class FinancialAccount(
    override val id: Long,
    override val name: String,
    val balance: Money,
    override val emoji: String,
    override val description: String? = null
) : DisplayEntity
