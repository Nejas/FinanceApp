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
