package com.example.financeapp.presentation.analytics

import androidx.compose.ui.graphics.Color
import com.example.financeapp.domain.model.AnalyticsCategoryBreakdown
import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.AnalyticsOverview
import com.example.financeapp.domain.model.AnalyticsTransactionEntry
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodType
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class AnalyticsStateMapperTest {

    private val mapper = AnalyticsStateMapper(
        filterUiMapper = AnalyticsFilterUiMapper()
    )

    @Test
    fun mapOverview_returnsUiStateFromDomainOverview() {
        val food = Category(
            id = 10,
            name = "Food",
            emoji = "food",
            type = TransactionType.EXPENSE
        )
        val account = FinancialAccount(
            id = 20,
            name = "Main account",
            balance = Money(amountInMinorUnits = 50_000, currency = Currency.RUB),
            emoji = "wallet",
            createdAt = Instant.parse("2026-07-01T00:00:00Z")
        )
        val transaction = Transaction(
            id = 30,
            amount = Money(amountInMinorUnits = 12_500, currency = Currency.RUB),
            categoryId = food.id,
            accountId = account.id,
            transactionDate = Instant.parse("2026-07-20T12:00:00Z"),
            comment = "Lunch"
        )
        val filter = AnalyticsFilter(
            accountId = account.id,
            startDate = LocalDate.of(2026, 7, 1),
            endDate = LocalDate.of(2026, 7, 31),
            type = TransactionType.EXPENSE,
            currency = Currency.RUB,
            categoryIds = setOf(food.id)
        )
        val periodFilter = AnalyticsPeriodFilterState(
            selectedType = AnalyticsPeriodType.Month,
            startDate = filter.startDate,
            endDate = filter.endDate
        )
        val overview = AnalyticsOverview(
            total = transaction.amount,
            categories = listOf(
                AnalyticsCategoryBreakdown(
                    categoryId = food.id,
                    category = food,
                    amount = transaction.amount,
                    sharePercent = 100
                )
            ),
            availableCategories = listOf(food),
            transactions = listOf(
                AnalyticsTransactionEntry(
                    transaction = transaction,
                    category = food,
                    account = account
                )
            ),
            filter = filter
        )
        val categoryItems = mapper.mapCategories(overview.categories)
        val categoryColors = mapOf(food.id to Color.Red)

        val state = mapper.mapOverview(
            currentState = AnalyticsState(
                filter = filter.copy(categoryIds = emptySet()),
                periodFilter = periodFilter,
                availableAccounts = listOf(account),
                isLoading = true
            ),
            overview = overview,
            categoryItems = categoryItems,
            categoryColors = categoryColors,
            periodFilter = periodFilter
        )

        assertEquals(filter, state.filter)
        assertEquals(transaction.amount, state.total)
        assertEquals("Food", state.categories.single().title)
        assertEquals("food", state.categories.single().emoji)
        assertEquals(100, state.categories.single().percent)
        assertEquals(Color.Red, state.categoryColors.getValue(food.id))
        assertEquals("Food", state.transactions.single().title)
        assertEquals("food", state.transactions.single().leadingEmoji)
        assertEquals("Main account", state.transactions.single().comment)
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertNull(state.error)
    }
}
