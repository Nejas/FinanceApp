package com.example.financeapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.financeapp.data.local.db.entity.SyncOperationEntity

@Dao
interface SyncOperationDao {

    @Query(
        """
        SELECT * FROM sync_operations
        WHERE status = 'PENDING'
        ORDER BY createdAtEpochMillis ASC, id ASC
        """
    )
    suspend fun getPendingOperations(): List<SyncOperationEntity>

    @Query("SELECT * FROM sync_operations WHERE status = 'FAILED' ORDER BY createdAtEpochMillis ASC, id ASC")
    suspend fun getFailedOperations(): List<SyncOperationEntity>

    @Query(
        """
        SELECT * FROM sync_operations
        WHERE localTransactionId = :localTransactionId
          AND operationType = 'CREATE_TRANSACTION'
        ORDER BY id DESC
        LIMIT 1
        """
    )
    suspend fun getCreateTransactionOperation(localTransactionId: Long): SyncOperationEntity?

    @Query(
        """
        SELECT * FROM sync_operations
        WHERE accountId = :accountId
          AND operationType = 'CREATE_ACCOUNT'
        ORDER BY id DESC
        LIMIT 1
        """
    )
    suspend fun getCreateAccountOperation(accountId: Long): SyncOperationEntity?

    @Query(
        """
        DELETE FROM sync_operations
        WHERE localTransactionId = :localTransactionId
          AND status = 'PENDING'
        """
    )
    suspend fun deletePendingOperationsForTransaction(localTransactionId: Long)

    @Query("DELETE FROM sync_operations WHERE localTransactionId = :localTransactionId")
    suspend fun deleteOperationsForTransaction(localTransactionId: Long)

    @Query("DELETE FROM sync_operations WHERE accountId = :accountId AND operationType IN ('CREATE_ACCOUNT', 'UPDATE_ACCOUNT')")
    suspend fun deleteOperationsForAccount(accountId: Long)

    @Upsert
    suspend fun upsertOperation(operation: SyncOperationEntity)

    @Query("DELETE FROM sync_operations WHERE id = :id")
    suspend fun deleteOperation(id: Long)

    @Query(
        """
        UPDATE sync_operations
        SET attempts = attempts + 1,
            lastError = :message
        WHERE id = :id
        """
    )
    suspend fun recordRetryableError(id: Long, message: String)

    @Query(
        """
        UPDATE sync_operations
        SET status = 'FAILED',
            attempts = attempts + 1,
            lastError = :message
        WHERE id = :id
        """
    )
    suspend fun markFailed(id: Long, message: String)

    @Query("UPDATE sync_operations SET status = 'PENDING', lastError = NULL WHERE status = 'FAILED'")
    suspend fun retryFailedOperations()

    @Query("DELETE FROM sync_operations WHERE status = 'FAILED'")
    suspend fun deleteFailedOperations()

    @Query(
        """
        UPDATE sync_operations
        SET accountId = :newAccountId
        WHERE accountId = :oldAccountId
          AND operationType IN ('CREATE_TRANSACTION', 'UPDATE_TRANSACTION', 'DELETE_TRANSACTION')
        """
    )
    suspend fun replaceTransactionAccountId(oldAccountId: Long, newAccountId: Long)
}
