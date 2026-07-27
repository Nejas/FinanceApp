package com.example.financeapp.presentation.analytics.mappers

import com.example.financeapp.R
import com.example.financeapp.domain.model.TransactionAnalysisCriteria
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.analytics.TransactionAnalysisCriteriaUi
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodType
import com.example.financeapp.presentation.common.model.FinanceFieldType
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TransactionAnalysisCriteriaUiMapper @Inject constructor() {
    fun map(
        filter: TransactionAnalysisCriteria,
        periodFilter: AnalyticsPeriodFilterState,
        categories: List<Category>,
        accounts: List<Account>
    ): List<TransactionAnalysisCriteriaUi> {
        return listOf(
            TransactionAnalysisCriteriaUi(
                type = FinanceFieldType.TransactionType,
                valueResId = filter.type.analyticsTitleResId()
            ),
            TransactionAnalysisCriteriaUi(
                type = FinanceFieldType.Period,
                valueResId = periodFilter.selectedType
                    .takeUnless { periodType -> periodType == AnalyticsPeriodType.Custom }
                    ?.titleResId,
                value = if (periodFilter.selectedType == AnalyticsPeriodType.Custom) {
                    filter.formattedPeriod()
                } else {
                    ""
                }
            ),
            TransactionAnalysisCriteriaUi(
                type = FinanceFieldType.Category,
                valueResId = if (filter.categoryIds.isEmpty()) {
                    R.string.analytics_filter_all_categories
                } else {
                    null
                },
                value = categories
                    .filter { category -> category.id in filter.categoryIds }
                    .joinToString(separator = ", ") { category -> category.name }
            ),
            TransactionAnalysisCriteriaUi(
                type = FinanceFieldType.Account,
                valueResId = if (filter.accountId == null) {
                    R.string.analytics_filter_all_accounts
                } else {
                    null
                },
                value = accounts.firstOrNull { account -> account.id == filter.accountId }?.name.orEmpty()
            )
        )
    }

    private fun TransactionAnalysisCriteria.formattedPeriod(): String {
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
