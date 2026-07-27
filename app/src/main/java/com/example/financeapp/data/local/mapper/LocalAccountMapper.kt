package com.example.financeapp.data.local.mapper

import com.example.financeapp.data.local.db.entity.AccountEntity
import com.example.financeapp.data.local.normalizeCurrencyCode
import com.example.financeapp.data.local.db.entity.AccountSyncState
import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.data.network.model.response.AccountDetailsResponseDto
import com.example.financeapp.data.network.model.response.AccountResponseDto
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.AccountDraft
import com.example.financeapp.domain.model.SyncStatus
import com.example.financeapp.data.mapper.toMoney
import java.time.Instant

fun AccountResponseDto.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        emoji = emoji,
        balance = balance,
        currencyCode = currency.normalizeCurrencyCode(),
        createdAt = createdAt
    )
}

fun AccountDetailsResponseDto.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        emoji = emoji,
        balance = balance,
        currencyCode = currency.normalizeCurrencyCode(),
        createdAt = createdAt
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        emoji = emoji,
        balance = balance.amount.toPlainString(),
        currencyCode = balance.currency.code,
        createdAt = createdAt.toString(),
        description = description
    )
}

fun AccountDraft.toEntity(
    id: Long,
    createdAt: Instant,
    syncState: AccountSyncState
): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        emoji = emoji.orEmpty(),
        balance = balance.amount.toPlainString(),
        currencyCode = balance.currency.code,
        createdAt = createdAt.toString(),
        syncState = syncState.name
    )
}

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        balance = balance.toMoney(currencyCode.normalizeCurrencyCode()),
        emoji = emoji,
        createdAt = Instant.parse(createdAt),
        description = description,
        syncStatus = SyncStatus.valueOf(syncState)
    )
}

fun AccountDraft.toAccountSyncOperation(
    operationType: SyncOperationType,
    accountId: Long,
    serverAccountId: Long?,
    createdAtEpochMillis: Long
): SyncOperationEntity {
    return SyncOperationEntity(
        operationType = operationType.name,
        localTransactionId = 0,
        serverTransactionId = null,
        serverAccountId = serverAccountId,
        accountId = accountId,
        categoryId = 0,
        amount = "",
        currencyCode = balance.currency.code,
        transactionDate = "",
        transactionDateEpochMillis = 0,
        accountName = name,
        accountEmoji = emoji,
        accountBalance = balance.amount.toPlainString(),
        accountCurrencyCode = balance.currency.code,
        createdAtEpochMillis = createdAtEpochMillis
    )
}
