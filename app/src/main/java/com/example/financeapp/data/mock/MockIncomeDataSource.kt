package com.example.financeapp.data.mock

import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import java.time.Instant
import javax.inject.Inject

class MockIncomeDataSource @Inject constructor() {

    fun getIncome(): List<Transaction> = income

    private companion object {
        val income = listOf(
            Transaction(
                id = 101,
                title = "Продажа старой мебели",
                amount = Money(amountInMinorUnits = 8_500L * 100),
                categoryId = MockDataIds.Categories.SALE,
                accountId = MockDataIds.FinancialAccounts.YANDEX_PAY,
                transactionDate = Instant.parse("2026-06-12T07:45:00Z")
            ),
            Transaction(
                id = 102,
                title = "Возврат налога",
                amount = Money(amountInMinorUnits = 15_000L * 100),
                categoryId = MockDataIds.Categories.TAX_REFUND,
                accountId = MockDataIds.FinancialAccounts.SBERBANK,
                transactionDate = Instant.parse("2026-06-12T08:50:00Z")
            ),
            Transaction(
                id = 103,
                title = "Премия за проект",
                amount = Money(amountInMinorUnits = 25_000L * 100),
                categoryId = MockDataIds.Categories.BONUS,
                accountId = MockDataIds.FinancialAccounts.GAZPROMBANK,
                transactionDate = Instant.parse("2026-06-12T10:00:00Z")
            ),
            Transaction(
                id = 104,
                title = "Подработка фриланс",
                amount = Money(amountInMinorUnits = 13_450L * 100),
                categoryId = MockDataIds.Categories.FREELANCE,
                accountId = MockDataIds.FinancialAccounts.YANDEX_PAY,
                transactionDate = Instant.parse("2026-06-12T12:30:00Z")
            ),
            Transaction(
                id = 105,
                title = "Сдача квартиры",
                amount = Money(amountInMinorUnits = 38_000L * 100),
                categoryId = MockDataIds.Categories.RENT,
                accountId = MockDataIds.FinancialAccounts.SBERBANK,
                transactionDate = Instant.parse("2026-06-12T14:15:00Z")
            ),
            Transaction(
                id = 106,
                title = "Кэшбэк на карту",
                amount = Money(amountInMinorUnits = 1_200L * 100),
                categoryId = MockDataIds.Categories.CASHBACK,
                accountId = MockDataIds.FinancialAccounts.YANDEX_PAY,
                transactionDate = Instant.parse("2026-06-12T16:25:00Z")
            ),
            Transaction(
                id = 107,
                title = "Подарок от родителей",
                amount = Money(amountInMinorUnits = 5_000L * 100),
                categoryId = MockDataIds.Categories.GIFT,
                accountId = MockDataIds.FinancialAccounts.GAZPROMBANK,
                transactionDate = Instant.parse("2026-06-12T18:00:00Z")
            )
        )
    }
}
