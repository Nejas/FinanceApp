package com.example.financeapp.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE accounts ADD COLUMN syncState TEXT NOT NULL DEFAULT 'SYNCED'"
        )
        database.execSQL(
            "ALTER TABLE sync_operations ADD COLUMN serverAccountId INTEGER"
        )
        database.execSQL(
            "ALTER TABLE sync_operations ADD COLUMN accountName TEXT"
        )
        database.execSQL(
            "ALTER TABLE sync_operations ADD COLUMN accountEmoji TEXT"
        )
        database.execSQL(
            "ALTER TABLE sync_operations ADD COLUMN accountBalance TEXT"
        )
        database.execSQL(
            "ALTER TABLE sync_operations ADD COLUMN accountCurrencyCode TEXT"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(CURRENCY_CODE_NORMALIZATION_SQL.format("accounts", "currencyCode"))
        database.execSQL(CURRENCY_CODE_NORMALIZATION_SQL.format("transactions", "currencyCode"))
        database.execSQL(CURRENCY_CODE_NORMALIZATION_SQL.format("sync_operations", "currencyCode"))
        database.execSQL(CURRENCY_CODE_NORMALIZATION_SQL.format("sync_operations", "accountCurrencyCode"))
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS transactions_new (
                id INTEGER NOT NULL,
                accountId INTEGER NOT NULL,
                categoryId INTEGER NOT NULL,
                amount TEXT NOT NULL,
                currencyCode TEXT NOT NULL,
                transactionDate TEXT NOT NULL,
                transactionDateEpochMillis INTEGER NOT NULL,
                comment TEXT,
                syncState TEXT NOT NULL,
                isDeletedPendingSync INTEGER NOT NULL,
                updatedAtEpochMillis INTEGER NOT NULL,
                PRIMARY KEY(id),
                FOREIGN KEY(accountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO transactions_new (
                id,
                accountId,
                categoryId,
                amount,
                currencyCode,
                transactionDate,
                transactionDateEpochMillis,
                comment,
                syncState,
                isDeletedPendingSync,
                updatedAtEpochMillis
            )
            SELECT
                transactions.id,
                transactions.accountId,
                transactions.categoryId,
                transactions.amount,
                transactions.currencyCode,
                transactions.transactionDate,
                transactions.transactionDateEpochMillis,
                transactions.comment,
                transactions.syncState,
                transactions.isDeletedPendingSync,
                transactions.updatedAtEpochMillis
            FROM transactions
            INNER JOIN accounts ON accounts.id = transactions.accountId
            """.trimIndent()
        )
        database.execSQL("DROP TABLE transactions")
        database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_transactions_accountId_transactionDateEpochMillis " +
                "ON transactions(accountId, transactionDateEpochMillis)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions(categoryId)"
        )
    }
}

private const val CURRENCY_CODE_NORMALIZATION_SQL = """
    UPDATE %1${'$'}s
    SET %2${'$'}s = CASE %2${'$'}s
        WHEN '₽' THEN 'RUB'
        WHEN char(36) THEN 'USD'
        WHEN '€' THEN 'EUR'
        WHEN '£' THEN 'GBP'
        WHEN '¥' THEN 'CNY'
        WHEN '￥' THEN 'CNY'
        ELSE UPPER(%2${'$'}s)
    END
"""
