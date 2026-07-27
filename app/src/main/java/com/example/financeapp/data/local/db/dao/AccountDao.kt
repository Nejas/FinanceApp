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

    @Transaction
    suspend fun replaceSyncedAccounts(accounts: List<AccountEntity>) {
        val pendingAccountIds = getPendingAccountIds().toSet()
        deleteSyncedAccounts()
        upsertAccounts(accounts.filterNot { account -> account.id in pendingAccountIds })
    }

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    @Query("UPDATE accounts SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: Long)
}
