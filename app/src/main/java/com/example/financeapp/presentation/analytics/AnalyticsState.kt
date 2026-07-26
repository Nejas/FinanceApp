package com.example.financeapp.presentation.analytics

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.common.model.FinanceFieldType
import com.example.financeapp.presentation.common.model.FinanceListItemUiModel
import com.example.financeapp.presentation.common.placeholders.ScreenError

data class AnalyticsState(
    val filter: AnalyticsFilter,
    val periodFilter: AnalyticsPeriodFilterState,
    val total: Money = Money(amountInMinorUnits = 0),
    val categories: List<AnalyticsCategoryUi> = emptyList(),
    val categoryColors: Map<Long, Color> = emptyMap(),
    val filters: List<AnalyticsFilterUi> = defaultAnalyticsFilters(),
    val availableCategories: List<Category> = emptyList(),
    val availableAccounts: List<FinancialAccount> = emptyList(),
    val activeFilterSheet: AnalyticsFilterSheet? = null,
    val isDetailVisible: Boolean = false,
    val transactions: List<FinanceListItemUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: ScreenError? = null
)

data class AnalyticsCategoryUi(
    val categoryId: Long,
    val title: String,
    val emoji: String,
    val amount: Money,
    val percent: Int
)

data class AnalyticsFilterUi(
    val type: FinanceFieldType,
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
    return AnalyticsFieldTypes.map { type ->
        AnalyticsFilterUi(type = type)
    }
}

val AnalyticsFieldTypes: List<FinanceFieldType> = listOf(
    FinanceFieldType.TransactionType,
    FinanceFieldType.Period,
    FinanceFieldType.Category,
    FinanceFieldType.Account
)
