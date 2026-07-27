package com.example.financeapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_operations",
    indices = [
        Index(value = ["status", "createdAtEpochMillis"]),
        Index(value = ["localTransactionId"])
    ]
)
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String,
    val status: String = SyncOperationStatus.PENDING.name,
    val localTransactionId: Long,
    val serverTransactionId: Long? = null,
    val serverAccountId: Long? = null,
    val accountId: Long,
    val categoryId: Long,
    val amount: String,
    val currencyCode: String,
    val transactionDate: String,
    val transactionDateEpochMillis: Long,
    val comment: String? = null,
    val accountName: String? = null,
    val accountEmoji: String? = null,
    val accountBalance: String? = null,
    val accountCurrencyCode: String? = null,
    val attempts: Int = 0,
    val lastError: String? = null,
    val createdAtEpochMillis: Long
)

enum class SyncOperationType {
    CREATE_TRANSACTION,
    UPDATE_TRANSACTION,
    DELETE_TRANSACTION,
    CREATE_ACCOUNT,
    UPDATE_ACCOUNT
}

enum class SyncOperationStatus {
    PENDING,
    FAILED
}
