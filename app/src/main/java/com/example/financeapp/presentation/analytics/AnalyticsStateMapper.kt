package com.example.financeapp.presentation.analytics

import androidx.compose.ui.graphics.Color
import com.example.financeapp.domain.model.AnalyticsCategoryBreakdown
import com.example.financeapp.domain.model.AnalyticsOverview
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.common.model.RouteScreenItem
import javax.inject.Inject

class AnalyticsStateMapper @Inject constructor(
    private val filterUiMapper: AnalyticsFilterUiMapper
) {

    fun mapCategories(categories: List<AnalyticsCategoryBreakdown>): List<AnalyticsCategoryUi> {
        return categories.map { breakdown ->
            AnalyticsCategoryUi(
                categoryId = breakdown.categoryId,
                title = breakdown.category?.name.orEmpty(),
                emoji = breakdown.category?.emoji.orEmpty(),
                amount = breakdown.amount,
                percent = breakdown.sharePercent
            )
        }
    }

    fun mapOverview(
        currentState: AnalyticsState,
        overview: AnalyticsOverview,
        categoryItems: List<AnalyticsCategoryUi>,
        categoryColors: Map<Long, Color>,
        periodFilter: AnalyticsPeriodFilterState
    ): AnalyticsState {
        return currentState.copy(
            filter = overview.filter,
            periodFilter = periodFilter,
            total = overview.total,
            categories = categoryItems,
            availableCategories = overview.availableCategories,
            categoryColors = categoryColors,
            filters = filterUiMapper.map(
                filter = overview.filter,
                periodFilter = periodFilter,
                categories = overview.availableCategories,
                accounts = currentState.availableAccounts
            ),
            transactions = overview.transactions.map { entry ->
                RouteScreenItem(
                    id = entry.transaction.id.toString(),
                    title = entry.category?.name.orEmpty(),
                    leadingEmoji = entry.category?.emoji.orEmpty(),
                    comment = entry.account?.name.orEmpty(),
                    money = entry.transaction.amount
                )
            },
            isLoading = false,
            isEmpty = categoryItems.isEmpty() && overview.transactions.isEmpty(),
            error = null
        )
    }
}
