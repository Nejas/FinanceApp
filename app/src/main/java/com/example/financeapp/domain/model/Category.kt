package com.example.financeapp.domain.model

import com.example.financeapp.domain.model.common.DisplayEntity

data class Category(
    override val id: Long,
    override val name: String,
    override val emoji: String,
    val type: TransactionType,
    override val description: String? = null
) : DisplayEntity
