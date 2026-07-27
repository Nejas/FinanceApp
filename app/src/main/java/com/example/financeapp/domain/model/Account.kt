package com.example.financeapp.domain.model

import java.time.Instant

data class Account(
    val id: Long,
    val name: String,
    val balance: Money,
    val emoji: String,
    val createdAt: Instant,
    val description: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
