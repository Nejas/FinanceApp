package com.example.financeapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.financeapp.data.local.db.entity.AccountEntity

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY id")
    suspend fun getAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccount(id: Long): AccountEntity?

    @Upsert
    suspend fun upsertAccounts(accounts: List<AccountEntity>)

    @Upsert
    suspend fun upsertAccount(account: AccountEntity)

    @Query("SELECT id FROM accounts WHERE syncState = 'PENDING'")
    suspend fun getPendingAccountIds(): List<Long>

    @Query("DELETE FROM accounts WHERE syncState = 'SYNCED'")
    suspend fun deleteSyncedAccounts()

    @Query("DELETE FROM accounts WHERE syncState = 'SYNCED' AND id NOT IN (:accountIds)")
    suspend fun deleteSyncedAccountsExcept(accountIds: Set<Long>)

    @Transaction
    suspend fun replaceSyncedAccounts(accounts: List<AccountEntity>) {
        val pendingAccountIds = getPendingAccountIds().toSet()
        val syncedAccountIds = accounts.mapTo(mutableSetOf()) { account -> account.id }
        upsertAccounts(accounts.filterNot { account -> account.id in pendingAccountIds })
        if (syncedAccountIds.isEmpty()) {
            deleteSyncedAccounts()
        } else {
            deleteSyncedAccountsExcept(syncedAccountIds)
        }
    }

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    @Query("UPDATE accounts SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: Long)
}
