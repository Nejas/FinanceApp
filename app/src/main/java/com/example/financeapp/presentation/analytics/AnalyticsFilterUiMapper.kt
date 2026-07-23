package com.example.financeapp.presentation.analytics

import com.example.financeapp.R
import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.analytics.bottomsheets.AnalyticsFilterType
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodType
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AnalyticsFilterUiMapper @Inject constructor() {

    fun map(
        filter: AnalyticsFilter,
        periodFilter: AnalyticsPeriodFilterState,
        categories: List<Category>,
        accounts: List<FinancialAccount>
    ): List<AnalyticsFilterUi> {
        return listOf(
            AnalyticsFilterUi(
                type = AnalyticsFilterType.Type,
                titleResId = R.string.analytics_filter_type,
                valueResId = filter.type.analyticsTitleResId()
            ),
            AnalyticsFilterUi(
                type = AnalyticsFilterType.Period,
                titleResId = R.string.analytics_filter_period,
                valueResId = periodFilter.selectedType
                    .takeUnless { periodType -> periodType == AnalyticsPeriodType.Custom }
                    ?.titleResId,
                value = if (periodFilter.selectedType == AnalyticsPeriodType.Custom) {
                    filter.formattedPeriod()
                } else {
                    ""
                }
            ),
            AnalyticsFilterUi(
                type = AnalyticsFilterType.Category,
                titleResId = R.string.analytics_filter_articles,
                valueResId = if (filter.categoryIds.isEmpty()) {
                    R.string.analytics_filter_all_categories
                } else {
                    null
                },
                value = categories
                    .filter { category -> category.id in filter.categoryIds }
                    .joinToString(separator = ", ") { category -> category.name }
            ),
            AnalyticsFilterUi(
                type = AnalyticsFilterType.Account,
                titleResId = R.string.analytics_filter_account,
                valueResId = if (filter.accountId == null) {
                    R.string.analytics_filter_all_accounts
                } else {
                    null
                },
                value = accounts.firstOrNull { account -> account.id == filter.accountId }?.name.orEmpty()
            )
        )
    }

    private fun AnalyticsFilter.formattedPeriod(): String {
        return "${startDate.format(FilterDateFormatter)} – ${endDate.format(FilterDateFormatter)}"
    }

    private fun TransactionType?.analyticsTitleResId(): Int {
        return when (this) {
            TransactionType.EXPENSE -> R.string.analytics_filter_expenses
            TransactionType.INCOME -> R.string.analytics_filter_income
            null -> R.string.analytics_filter_all
        }
    }

    private companion object {
        val FilterDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
}
