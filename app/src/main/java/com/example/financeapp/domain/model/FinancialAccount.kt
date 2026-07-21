package com.example.financeapp.domain.model

import com.example.financeapp.domain.model.common.DisplayEntity
import java.time.Instant

data class FinancialAccount(
    override val id: Long,
    override val name: String,
    val balance: Money,
    override val emoji: String,
    val createdAt: Instant,
    override val description: String? = null
) : DisplayEntity
