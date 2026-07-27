package com.example.financeapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.financeapp.data.local.db.entity.TransactionEntity

@Dao
interface TransactionDao {

    @Query(
        """
        SELECT * FROM transactions
        WHERE accountId IN (:accountIds)
          AND transactionDateEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis
          AND isDeletedPendingSync = 0
        ORDER BY transactionDateEpochMillis DESC
        """
    )
    suspend fun getTransactionsByPeriod(
        accountIds: Set<Long>,
        startEpochMillis: Long,
        endEpochMillis: Long
    ): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE accountId IN (:accountIds)
          AND transactionDateEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis
          AND syncState = 'PENDING'
          AND isDeletedPendingSync = 0
        ORDER BY transactionDateEpochMillis DESC
        """
    )
    suspend fun getPendingTransactionsByPeriod(
        accountIds: Set<Long>,
        startEpochMillis: Long,
        endEpochMillis: Long
    ): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id AND isDeletedPendingSync = 0 LIMIT 1")
    suspend fun getTransaction(id: Long): TransactionEntity?

    @Upsert
    suspend fun upsertTransactions(transactions: List<TransactionEntity>)

    @Upsert
    suspend fun upsertTransaction(transaction: TransactionEntity)

    @Query(
        """
        DELETE FROM transactions
        WHERE accountId = :accountId
          AND transactionDateEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis
          AND syncState = 'SYNCED'
        """
    )
    suspend fun deleteSyncedTransactionsForPeriod(
        accountId: Long,
        startEpochMillis: Long,
        endEpochMillis: Long
    )

    @Transaction
    suspend fun replaceSyncedTransactionsForPeriod(
        accountId: Long,
        startEpochMillis: Long,
        endEpochMillis: Long,
        transactions: List<TransactionEntity>
    ) {
        deleteSyncedTransactionsForPeriod(
            accountId = accountId,
            startEpochMillis = startEpochMillis,
            endEpochMillis = endEpochMillis
        )
        upsertTransactions(transactions)
    }

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query(
        """
        UPDATE transactions
        SET isDeletedPendingSync = 1,
            syncState = 'PENDING',
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE id = :id
        """
    )
    suspend fun markDeletedPendingSync(
        id: Long,
        updatedAtEpochMillis: Long
    )

    @Query("UPDATE transactions SET accountId = :newAccountId WHERE accountId = :oldAccountId")
    suspend fun replaceAccountId(oldAccountId: Long, newAccountId: Long)

    @Query(
        """
        UPDATE transactions
        SET syncState = 'SYNCED',
            isDeletedPendingSync = 0
        WHERE id = :id
        """
    )
    suspend fun markSynced(id: Long)
}
