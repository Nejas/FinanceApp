package com.example.financeapp.data.mock

import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionType
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockDataSourcesTest {

    private val financialAccounts =
        MockFinancialAccountsDataSource().getFinancialAccounts()
    private val categories = MockCategoriesDataSource().getCategories()
    private val expenses = MockExpensesDataSource().getExpenses()
    private val income = MockIncomeDataSource().getIncome()

    @Test
    fun transactions_referenceExistingAccountsAndCategories() {
        val accountIds = financialAccounts.mapTo(mutableSetOf()) { account -> account.id }
        val categoryIds = categories.mapTo(mutableSetOf()) { category -> category.id }

        (expenses + income).forEach { transaction ->
            assertTrue(transaction.accountId in accountIds)
            assertTrue(transaction.categoryId in categoryIds)
        }
    }

    @Test
    fun transactionCategories_haveExpectedTypes() {
        val categoriesById = categories.associateBy { category -> category.id }

        assertTransactionsHaveType(expenses, categoriesById, TransactionType.EXPENSE)
        assertTransactionsHaveType(income, categoriesById, TransactionType.INCOME)
    }

    @Test
    fun mockTransactions_useSingleStableDate() {
        val expectedDate = LocalDate.of(2026, 6, 12)

        (expenses + income).forEach { transaction ->
            val actualDate = transaction.transactionDate
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
            assertEquals(expectedDate, actualDate)
        }
    }

    @Test
    fun mockAmounts_matchVisibleDesignRows() {
        val expenseTotal = expenses.sumOf { transaction ->
            transaction.amount.amountInMinorUnits
        }
        val incomeTotal = income.sumOf { transaction ->
            transaction.amount.amountInMinorUnits
        }

        assertEquals(16_950L * 100, expenseTotal)
        assertEquals(106_150L * 100, incomeTotal)
    }

    private fun assertTransactionsHaveType(
        transactions: List<Transaction>,
        categoriesById: Map<Long, Category>,
        expectedType: TransactionType
    ) {
        transactions.forEach { transaction ->
            assertEquals(expectedType, categoriesById.getValue(transaction.categoryId).type)
        }
    }
}
