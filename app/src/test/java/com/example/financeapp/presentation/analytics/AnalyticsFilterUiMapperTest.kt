package com.example.financeapp.presentation.analytics

import com.example.financeapp.R
import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.analytics.bottomsheets.AnalyticsFilterType
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodType
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnalyticsFilterUiMapperTest {

    private val mapper = AnalyticsFilterUiMapper()

    @Test
    fun map_returnsSelectedFilterLabels() {
        val filter = AnalyticsFilter(
            accountId = 2,
            startDate = LocalDate.of(2026, 7, 10),
            endDate = LocalDate.of(2026, 7, 12),
            type = TransactionType.INCOME,
            currency = Currency.RUB,
            categoryIds = setOf(1, 3)
        )
        val periodFilter = AnalyticsPeriodFilterState(
            selectedType = AnalyticsPeriodType.Custom,
            startDate = filter.startDate,
            endDate = filter.endDate
        )

        val filters = mapper.map(
            filter = filter,
            periodFilter = periodFilter,
            categories = listOf(
                category(id = 1, name = "Salary"),
                category(id = 2, name = "Food"),
                category(id = 3, name = "Bonus")
            ),
            accounts = listOf(account(id = 2, name = "Main account"))
        )

        assertEquals(
            R.string.analytics_filter_income,
            filters.first { item -> item.type == AnalyticsFilterType.Type }.valueResId
        )
        filters.first { item -> item.type == AnalyticsFilterType.Period }.let { item ->
            assertNull(item.valueResId)
            assertEquals("10.07.2026 – 12.07.2026", item.value)
        }
        filters.first { item -> item.type == AnalyticsFilterType.Category }.let { item ->
            assertNull(item.valueResId)
            assertEquals("Salary, Bonus", item.value)
        }
        filters.first { item -> item.type == AnalyticsFilterType.Account }.let { item ->
            assertNull(item.valueResId)
            assertEquals("Main account", item.value)
        }
    }

    @Test
    fun map_returnsDefaultLabelsForEmptySelections() {
        val filter = AnalyticsFilter(
            accountId = null,
            startDate = LocalDate.of(2026, 7, 1),
            endDate = LocalDate.of(2026, 7, 31),
            type = null,
            currency = Currency.RUB,
            categoryIds = emptySet()
        )
        val periodFilter = AnalyticsPeriodFilterState(
            selectedType = AnalyticsPeriodType.Month,
            startDate = filter.startDate,
            endDate = filter.endDate
        )

        val filters = mapper.map(
            filter = filter,
            periodFilter = periodFilter,
            categories = emptyList(),
            accounts = emptyList()
        )

        assertEquals(
            R.string.analytics_filter_all,
            filters.first { item -> item.type == AnalyticsFilterType.Type }.valueResId
        )
        assertEquals(
            R.string.analytics_period_month,
            filters.first { item -> item.type == AnalyticsFilterType.Period }.valueResId
        )
        assertEquals(
            R.string.analytics_filter_all_categories,
            filters.first { item -> item.type == AnalyticsFilterType.Category }.valueResId
        )
        assertEquals(
            R.string.analytics_filter_all_accounts,
            filters.first { item -> item.type == AnalyticsFilterType.Account }.valueResId
        )
    }

    private fun category(
        id: Long,
        name: String
    ): Category {
        return Category(
            id = id,
            name = name,
            emoji = "icon",
            type = TransactionType.EXPENSE
        )
    }

    private fun account(
        id: Long,
        name: String
    ): FinancialAccount {
        return FinancialAccount(
            id = id,
            name = name,
            balance = Money(amountInMinorUnits = 0, currency = Currency.RUB),
            emoji = "wallet",
            createdAt = Instant.parse("2026-07-01T00:00:00Z")
        )
    }
}
