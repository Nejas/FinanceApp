package com.example.financeapp.presentation.analytics

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.financeapp.R
import com.example.financeapp.domain.model.AnalyticsCategorySummary
import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.analytics.bottomsheets.AnalyticsFilterType
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.common.model.RouteScreenItem
import com.example.financeapp.presentation.common.placeholders.ScreenError

data class AnalyticsState(
    val filter: AnalyticsFilter,
    val periodFilter: AnalyticsPeriodFilterState,
    val total: Money = Money(amountInMinorUnits = 0),
    val categories: List<AnalyticsCategorySummary> = emptyList(),
    val categoryColors: Map<Long, Color> = emptyMap(),
    val filters: List<AnalyticsFilterUi> = defaultAnalyticsFilters(),
    val availableCategories: List<Category> = emptyList(),
    val availableAccounts: List<FinancialAccount> = emptyList(),
    val activeFilterSheet: AnalyticsFilterSheet? = null,
    val isDetailVisible: Boolean = false,
    val transactions: List<RouteScreenItem> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: ScreenError? = null
)

data class AnalyticsFilterUi(
    val type: AnalyticsFilterType,
    @StringRes val titleResId: Int,
    @StringRes val valueResId: Int? = null,
    val value: String = ""
)

enum class AnalyticsFilterSheet {
    Type,
    Period,
    CustomPeriod,
    Category,
    Account
}

fun defaultAnalyticsFilter(periodFilter: AnalyticsPeriodFilterState): AnalyticsFilter {
    return AnalyticsFilter(
        accountId = null,
        startDate = periodFilter.startDate,
        endDate = periodFilter.endDate,
        type = TransactionType.EXPENSE,
        currency = Currency.RUB
    )
}

fun defaultAnalyticsFilters(): List<AnalyticsFilterUi> {
    return listOf(
        AnalyticsFilterUi(
            type = AnalyticsFilterType.Type,
            titleResId = R.string.analytics_filter_type
        ),
        AnalyticsFilterUi(
            type = AnalyticsFilterType.Period,
            titleResId = R.string.analytics_filter_period
        ),
        AnalyticsFilterUi(
            type = AnalyticsFilterType.Category,
            titleResId = R.string.analytics_filter_articles
        ),
        AnalyticsFilterUi(
            type = AnalyticsFilterType.Account,
            titleResId = R.string.analytics_filter_account
        )
    )
}
