package com.example.financeapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId", "transactionDateEpochMillis"]),
        Index(value = ["categoryId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: Long,
    val accountId: Long,
    val categoryId: Long,
    val amount: String,
    val currencyCode: String,
    val transactionDate: String,
    val transactionDateEpochMillis: Long,
    val comment: String? = null,
    val syncState: String = TransactionSyncState.SYNCED.name,
    val isDeletedPendingSync: Boolean = false,
    val updatedAtEpochMillis: Long
)

enum class TransactionSyncState {
    SYNCED,
    PENDING
}
