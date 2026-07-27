package com.example.financeapp.data.local.mapper

import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.normalizeCurrencyCode
import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.data.local.db.entity.TransactionEntity
import com.example.financeapp.data.local.db.entity.TransactionSyncState
import com.example.financeapp.data.mapper.toApiAmountString
import com.example.financeapp.data.mapper.toMoney
import com.example.financeapp.data.network.model.response.TransactionPlainResponseDto
import com.example.financeapp.data.network.model.response.TransactionResponseDto
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionDraft
import com.example.financeapp.domain.model.SyncStatus
import java.time.Instant

fun TransactionResponseDto.toEntity(
    updatedAtEpochMillis: Long
): TransactionEntity {
    return TransactionEntity(
        id = id,
        accountId = account.id,
        categoryId = category.id,
        amount = amount,
        currencyCode = account.currency.normalizeCurrencyCode(),
        transactionDate = transactionDate,
        transactionDateEpochMillis = Instant.parse(transactionDate).toEpochMilli(),
        comment = comment,
        syncState = TransactionSyncState.SYNCED.name,
        isDeletedPendingSync = false,
        updatedAtEpochMillis = updatedAtEpochMillis
    )
}

fun TransactionPlainResponseDto.toEntity(
    currencyCode: String,
    updatedAtEpochMillis: Long
): TransactionEntity {
    return TransactionEntity(
        id = id,
        accountId = accountId,
        categoryId = categoryId,
        amount = amount,
        currencyCode = currencyCode.normalizeCurrencyCode(),
        transactionDate = transactionDate,
        transactionDateEpochMillis = Instant.parse(transactionDate).toEpochMilli(),
        comment = comment,
        syncState = TransactionSyncState.SYNCED.name,
        isDeletedPendingSync = false,
        updatedAtEpochMillis = updatedAtEpochMillis
    )
}

fun Transaction.toEntity(
    syncState: TransactionSyncState = TransactionSyncState.SYNCED,
    isDeletedPendingSync: Boolean = false,
    updatedAtEpochMillis: Long
): TransactionEntity {
    return TransactionEntity(
        id = id,
        accountId = accountId,
        categoryId = categoryId,
        amount = amount.toApiAmountString(),
        currencyCode = amount.currency.code,
        transactionDate = transactionDate.toString(),
        transactionDateEpochMillis = transactionDate.toEpochMilli(),
        comment = comment,
        syncState = syncState.name,
        isDeletedPendingSync = isDeletedPendingSync,
        updatedAtEpochMillis = updatedAtEpochMillis
    )
}

fun TransactionDraft.toEntity(
    id: Long,
    syncState: TransactionSyncState,
    isDeletedPendingSync: Boolean = false,
    updatedAtEpochMillis: Long
): TransactionEntity {
    return TransactionEntity(
        id = id,
        accountId = accountId,
        categoryId = categoryId,
        amount = amount.toApiAmountString(),
        currencyCode = amount.currency.code,
        transactionDate = transactionDate.toString(),
        transactionDateEpochMillis = transactionDate.toEpochMilli(),
        comment = comment,
        syncState = syncState.name,
        isDeletedPendingSync = isDeletedPendingSync,
        updatedAtEpochMillis = updatedAtEpochMillis
    )
}

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount.toMoney(currencyCode.normalizeCurrencyCode()),
        categoryId = categoryId,
        accountId = accountId,
        transactionDate = Instant.parse(transactionDate),
        comment = comment,
        syncStatus = SyncStatus.valueOf(syncState)
    )
}

fun TransactionDraft.toSyncOperation(
    operationType: SyncOperationType,
    localTransactionId: Long,
    serverTransactionId: Long?,
    createdAtEpochMillis: Long
): SyncOperationEntity {
    return SyncOperationEntity(
        operationType = operationType.name,
        localTransactionId = localTransactionId,
        serverTransactionId = serverTransactionId,
        accountId = accountId,
        categoryId = categoryId,
        amount = amount.toApiAmountString(),
        currencyCode = amount.currency.code,
        transactionDate = transactionDate.toString(),
        transactionDateEpochMillis = transactionDate.toEpochMilli(),
        comment = comment,
        createdAtEpochMillis = createdAtEpochMillis
    )
}
