package com.example.financeapp.data.sync

import com.example.financeapp.data.local.db.dao.AccountDao
import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.dao.TransactionDao
import com.example.financeapp.data.local.db.entity.AccountEntity
import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.data.local.db.entity.TransactionEntity
import com.example.financeapp.data.network.model.request.AccountCreateRequestDto
import com.example.financeapp.data.network.model.request.AccountUpdateRequestDto
import com.example.financeapp.data.network.model.request.TransactionRequestDto
import com.example.financeapp.data.network.model.response.AccountBriefResponseDto
import com.example.financeapp.data.network.model.response.AccountDetailsResponseDto
import com.example.financeapp.data.network.model.response.AccountHistoryResponseDto
import com.example.financeapp.data.network.model.response.AccountResponseDto
import com.example.financeapp.data.network.model.response.CategoryResponseDto
import com.example.financeapp.data.network.model.response.TransactionPlainResponseDto
import com.example.financeapp.data.network.model.response.TransactionResponseDto
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource

internal class ImmediateSyncTransactionRunner : SyncTransactionRunner {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
}

internal class FakeTransactionDao : TransactionDao {

    val upsertedTransactions = mutableListOf<TransactionEntity>()
    val deletedTransactionIds = mutableListOf<Long>()

    override suspend fun getTransactionsByPeriod(
        accountIds: Set<Long>,
        startEpochMillis: Long,
        endEpochMillis: Long
    ): List<TransactionEntity> = emptyList()

    override suspend fun getPendingTransactionsByPeriod(
        accountIds: Set<Long>,
        startEpochMillis: Long,
        endEpochMillis: Long
    ): List<TransactionEntity> = emptyList()

    override suspend fun getTransaction(id: Long): TransactionEntity? = null

    override suspend fun upsertTransactions(transactions: List<TransactionEntity>) {
        upsertedTransactions += transactions
    }

    override suspend fun upsertTransaction(transaction: TransactionEntity) {
        upsertedTransactions += transaction
    }

    override suspend fun deleteSyncedTransactionsForPeriod(
        accountId: Long,
        startEpochMillis: Long,
        endEpochMillis: Long
    ) = Unit

    override suspend fun deleteTransaction(id: Long) {
        deletedTransactionIds += id
    }

    override suspend fun markDeletedPendingSync(
        id: Long,
        updatedAtEpochMillis: Long
    ) = Unit

    override suspend fun replaceAccountId(oldAccountId: Long, newAccountId: Long) = Unit

    override suspend fun markSynced(id: Long) = Unit
}

internal class FakeAccountDao : AccountDao {

    val upsertedAccounts = mutableListOf<AccountEntity>()
    val deletedAccountIds = mutableListOf<Long>()

    override suspend fun getAccounts(): List<AccountEntity> = emptyList()

    override suspend fun getAccount(id: Long): AccountEntity? = null

    override suspend fun upsertAccounts(accounts: List<AccountEntity>) {
        upsertedAccounts += accounts
    }

    override suspend fun upsertAccount(account: AccountEntity) {
        upsertedAccounts += account
    }

    override suspend fun getPendingAccountIds(): List<Long> = emptyList()

    override suspend fun deleteSyncedAccounts() = Unit

    override suspend fun deleteSyncedAccountsExcept(accountIds: Set<Long>) = Unit

    override suspend fun deleteAccount(id: Long) {
        deletedAccountIds += id
    }

    override suspend fun markSynced(id: Long) = Unit
}

internal class FakeSyncOperationDao : SyncOperationDao {

    val deletedOperationIds = mutableListOf<Long>()

    override suspend fun getPendingOperations(): List<SyncOperationEntity> = emptyList()

    override suspend fun getFailedOperations(): List<SyncOperationEntity> = emptyList()

    override suspend fun getCreateTransactionOperation(localTransactionId: Long): SyncOperationEntity? = null

    override suspend fun getCreateAccountOperation(accountId: Long): SyncOperationEntity? = null

    override suspend fun deletePendingOperationsForTransaction(localTransactionId: Long) = Unit

    override suspend fun deleteOperationsForTransaction(localTransactionId: Long) = Unit

    override suspend fun deleteOperationsForAccount(accountId: Long) = Unit

    override suspend fun upsertOperation(operation: SyncOperationEntity) = Unit

    override suspend fun deleteOperation(id: Long) {
        deletedOperationIds += id
    }

    override suspend fun recordRetryableError(id: Long, message: String) = Unit

    override suspend fun markFailed(id: Long, message: String) = Unit

    override suspend fun retryFailedOperations() = Unit

    override suspend fun deleteFailedOperations() = Unit

    override suspend fun replaceTransactionAccountId(oldAccountId: Long, newAccountId: Long) = Unit
}

internal class FakeFinanceRemoteDataSource : FinanceRemoteDataSource {

    var getTransactionResult: NetworkResult<TransactionResponseDto> =
        NetworkResult.Success(transactionResponse())
    var updateTransactionResult: NetworkResult<TransactionResponseDto> =
        NetworkResult.Success(transactionResponse(comment = "local"))
    var deleteTransactionResult: NetworkResult<Unit> = NetworkResult.Success(Unit)
    var getAccountResult: NetworkResult<AccountDetailsResponseDto> =
        NetworkResult.Success(accountDetailsResponse())
    var updateAccountResult: NetworkResult<AccountResponseDto> =
        NetworkResult.Success(accountResponse(name = "Local account"))

    var updateTransactionCalls = 0
    var deleteTransactionCalls = 0
    var updateAccountCalls = 0

    override suspend fun getAccounts(): NetworkResult<List<AccountResponseDto>> {
        error("Not used")
    }

    override suspend fun createAccount(
        request: AccountCreateRequestDto
    ): NetworkResult<AccountResponseDto> {
        error("Not used")
    }

    override suspend fun getAccount(id: Long): NetworkResult<AccountDetailsResponseDto> {
        return getAccountResult
    }

    override suspend fun updateAccount(
        id: Long,
        request: AccountUpdateRequestDto
    ): NetworkResult<AccountResponseDto> {
        updateAccountCalls++
        return updateAccountResult
    }

    override suspend fun deleteAccount(id: Long): NetworkResult<Unit> {
        error("Not used")
    }

    override suspend fun getAccountHistory(id: Long): NetworkResult<AccountHistoryResponseDto> {
        error("Not used")
    }

    override suspend fun getCategories(): NetworkResult<List<CategoryResponseDto>> {
        error("Not used")
    }

    override suspend fun getCategoriesByType(isIncome: Boolean): NetworkResult<List<CategoryResponseDto>> {
        error("Not used")
    }

    override suspend fun createTransaction(
        request: TransactionRequestDto
    ): NetworkResult<TransactionPlainResponseDto> {
        error("Not used")
    }

    override suspend fun getTransaction(id: Long): NetworkResult<TransactionResponseDto> {
        return getTransactionResult
    }

    override suspend fun updateTransaction(
        id: Long,
        request: TransactionRequestDto
    ): NetworkResult<TransactionResponseDto> {
        updateTransactionCalls++
        return updateTransactionResult
    }

    override suspend fun deleteTransaction(id: Long): NetworkResult<Unit> {
        deleteTransactionCalls++
        return deleteTransactionResult
    }

    override suspend fun getTransactionsByPeriod(
        accountId: Long,
        startDate: String?,
        endDate: String?
    ): NetworkResult<List<TransactionResponseDto>> {
        error("Not used")
    }
}

internal fun transactionOperation(
    operationType: SyncOperationType,
    createdAtEpochMillis: Long,
    id: Long = 1L,
    transactionId: Long = 10L
): SyncOperationEntity {
    return SyncOperationEntity(
        id = id,
        operationType = operationType.name,
        localTransactionId = transactionId,
        serverTransactionId = transactionId,
        accountId = 20L,
        categoryId = 30L,
        amount = "100.00",
        currencyCode = "RUB",
        transactionDate = "2026-07-28T09:00:00Z",
        transactionDateEpochMillis = 1_775_204_000_000L,
        comment = "pending",
        createdAtEpochMillis = createdAtEpochMillis
    )
}

internal fun accountOperation(
    createdAtEpochMillis: Long,
    id: Long = 1L,
    accountId: Long = 20L
): SyncOperationEntity {
    return SyncOperationEntity(
        id = id,
        operationType = SyncOperationType.UPDATE_ACCOUNT.name,
        localTransactionId = 0,
        serverAccountId = accountId,
        accountId = accountId,
        categoryId = 0,
        amount = "",
        currencyCode = "RUB",
        transactionDate = "",
        transactionDateEpochMillis = 0,
        accountName = "Pending account",
        accountEmoji = "*",
        accountBalance = "1000.00",
        accountCurrencyCode = "RUB",
        createdAtEpochMillis = createdAtEpochMillis
    )
}

internal fun transactionResponse(
    id: Long = 10L,
    accountId: Long = 20L,
    categoryId: Long = 30L,
    amount: String = "100.00",
    comment: String? = "server",
    updatedAt: String = "2026-07-28T10:00:00Z"
): TransactionResponseDto {
    return TransactionResponseDto(
        id = id,
        account = AccountBriefResponseDto(
            id = accountId,
            name = "Account",
            emoji = "*",
            balance = "1000.00",
            currency = "RUB"
        ),
        category = CategoryResponseDto(
            id = categoryId,
            name = "Category",
            emoji = "*",
            isIncome = false
        ),
        amount = amount,
        transactionDate = "2026-07-28T09:00:00Z",
        comment = comment,
        createdAt = "2026-07-28T08:00:00Z",
        updatedAt = updatedAt
    )
}

internal fun accountResponse(
    id: Long = 20L,
    name: String = "Server account",
    updatedAt: String = "2026-07-28T10:00:00Z"
): AccountResponseDto {
    return AccountResponseDto(
        id = id,
        userId = 1L,
        name = name,
        emoji = "*",
        balance = "1000.00",
        currency = "RUB",
        createdAt = "2026-07-28T08:00:00Z",
        updatedAt = updatedAt
    )
}

internal fun accountDetailsResponse(
    id: Long = 20L,
    name: String = "Server account",
    updatedAt: String = "2026-07-28T10:00:00Z"
): AccountDetailsResponseDto {
    return AccountDetailsResponseDto(
        id = id,
        name = name,
        emoji = "*",
        balance = "1000.00",
        currency = "RUB",
        incomeStats = emptyList(),
        expenseStats = emptyList(),
        createdAt = "2026-07-28T08:00:00Z",
        updatedAt = updatedAt
    )
}
