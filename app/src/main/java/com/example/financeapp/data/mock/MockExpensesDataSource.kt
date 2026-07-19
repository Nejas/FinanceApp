package com.example.financeapp.data.mock

import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import java.time.Instant
import javax.inject.Inject

class MockExpensesDataSource @Inject constructor() {

    fun getExpenses(): List<Transaction> = expenses

    private companion object {
        val expenses = listOf(
            Transaction(
                id = 1,
                title = "Покупка канцтоваров",
                amount = Money(amountInMinorUnits = 1_200L * 100),
                categoryId = MockDataIds.Categories.STATIONERY,
                accountId = MockDataIds.FinancialAccounts.YANDEX_PAY,
                transactionDate = Instant.parse("2026-06-12T08:15:00Z")
            ),
            Transaction(
                id = 2,
                title = "Обед в кафе",
                amount = Money(amountInMinorUnits = 750L * 100),
                categoryId = MockDataIds.Categories.CAFE,
                accountId = MockDataIds.FinancialAccounts.SBERBANK,
                transactionDate = Instant.parse("2026-06-12T09:30:00Z")
            ),
            Transaction(
                id = 3,
                title = "Топливо для машины",
                amount = Money(amountInMinorUnits = 2_300L * 100),
                categoryId = MockDataIds.Categories.FUEL,
                accountId = MockDataIds.FinancialAccounts.GAZPROMBANK,
                transactionDate = Instant.parse("2026-06-12T10:45:00Z")
            ),
            Transaction(
                id = 4,
                title = "Подписка на сервис",
                amount = Money(amountInMinorUnits = 450L * 100),
                categoryId = MockDataIds.Categories.SUBSCRIPTIONS,
                accountId = MockDataIds.FinancialAccounts.YANDEX_PAY,
                transactionDate = Instant.parse("2026-06-12T11:20:00Z")
            ),
            Transaction(
                id = 5,
                title = "Ремонт техники",
                amount = Money(amountInMinorUnits = 5_800L * 100),
                categoryId = MockDataIds.Categories.REPAIRS,
                accountId = MockDataIds.FinancialAccounts.SBERBANK,
                transactionDate = Instant.parse("2026-06-12T12:10:00Z")
            ),
            Transaction(
                id = 6,
                title = "Покупка билетов",
                amount = Money(amountInMinorUnits = 3_200L * 100),
                categoryId = MockDataIds.Categories.TICKETS,
                accountId = MockDataIds.FinancialAccounts.YANDEX_PAY,
                transactionDate = Instant.parse("2026-06-12T13:40:00Z")
            ),
            Transaction(
                id = 7,
                title = "Оплата интернета",
                amount = Money(amountInMinorUnits = 800L * 100),
                categoryId = MockDataIds.Categories.INTERNET,
                accountId = MockDataIds.FinancialAccounts.GAZPROMBANK,
                transactionDate = Instant.parse("2026-06-12T15:00:00Z")
            ),
            Transaction(
                id = 8,
                title = "Магазин продуктов",
                amount = Money(amountInMinorUnits = 2_450L * 100),
                categoryId = MockDataIds.Categories.GROCERIES,
                accountId = MockDataIds.FinancialAccounts.SBERBANK,
                transactionDate = Instant.parse("2026-06-12T17:35:00Z")
            )
        )
    }
}
