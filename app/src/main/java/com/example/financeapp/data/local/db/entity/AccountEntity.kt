package com.example.financeapp.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val emoji: String,
    val balance: String,
    val currencyCode: String,
    val createdAt: String,
    val description: String? = null,
    @ColumnInfo(defaultValue = "'SYNCED'")
    val syncState: String = AccountSyncState.SYNCED.name
)

enum class AccountSyncState {
    SYNCED,
    PENDING
}
