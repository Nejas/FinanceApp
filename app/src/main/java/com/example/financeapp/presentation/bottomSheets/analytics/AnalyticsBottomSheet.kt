package com.example.financeapp.presentation.bottomSheets.analytics

import androidx.compose.runtime.Composable
import com.example.financeapp.presentation.analytics.TransactionAnalysisCriteriaSheet
import com.example.financeapp.presentation.analytics.AnalyticsIntent
import com.example.financeapp.presentation.analytics.AnalyticsState
import com.example.financeapp.presentation.bottomSheets.components.account.AnalyticsAccountSheet
import com.example.financeapp.presentation.bottomSheets.components.categories.AnalyticsCategorySheet
import com.example.financeapp.presentation.bottomSheets.components.customperiod.AnalyticsCustomPeriodSheet
import com.example.financeapp.presentation.bottomSheets.components.detail.AnalyticsDetailSheet
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodSheet
import com.example.financeapp.presentation.bottomSheets.components.type.AnalyticsTypeSheet
import com.example.financeapp.presentation.common.components.base.FinanceModalBottomSheet

@Composable
fun AnalyticsBottomSheet(
    state: AnalyticsState,
    onIntent: (AnalyticsIntent) -> Unit
) {
    if (state.isDetailVisible) {
        FinanceModalBottomSheet(
            onDismissRequest = { onIntent(AnalyticsIntent.DetailDismissed) }
        ) {
            AnalyticsDetailSheet(
                total = state.total,
                categories = state.categories,
                categoryColors = state.categoryColors,
            )
        }
        return
    }

    when (state.activeFilterSheet) {
        TransactionAnalysisCriteriaSheet.Type -> FinanceModalBottomSheet(
            onDismissRequest = { onIntent(AnalyticsIntent.FilterDismissed) }
        ) {
            AnalyticsTypeSheet(
                selectedType = state.filter.type,
                onApply = { type -> onIntent(AnalyticsIntent.TypeApplied(type)) }
            )
        }
        TransactionAnalysisCriteriaSheet.Period -> FinanceModalBottomSheet(
            onDismissRequest = { onIntent(AnalyticsIntent.FilterDismissed) }
        ) {
            AnalyticsPeriodSheet(
                periodFilter = state.periodFilter,
                onPeriodClick = { periodType -> onIntent(AnalyticsIntent.PeriodSelected(periodType)) }
            )
        }
        TransactionAnalysisCriteriaSheet.CustomPeriod -> FinanceModalBottomSheet(
            onDismissRequest = { onIntent(AnalyticsIntent.FilterDismissed) },
            skipPartiallyExpanded = true
        ) {
            AnalyticsCustomPeriodSheet(
                initialStartDate = state.periodFilter.startDate,
                initialEndDate = state.periodFilter.endDate,
                onCancelClick = { onIntent(AnalyticsIntent.CustomPeriodCancelClicked) },
                onApplyClick = { startDate, endDate ->
                    onIntent(AnalyticsIntent.CustomPeriodApplied(startDate, endDate))
                }
            )
        }
        TransactionAnalysisCriteriaSheet.Category -> FinanceModalBottomSheet(
            onDismissRequest = { onIntent(AnalyticsIntent.FilterDismissed) }
        ) {
            AnalyticsCategorySheet(
                categories = state.availableCategories,
                selectedCategoryIds = state.filter.categoryIds,
                onApply = { categoryIds ->
                    onIntent(AnalyticsIntent.CategorySelectionApplied(categoryIds))
                }
            )
        }
        TransactionAnalysisCriteriaSheet.Account -> FinanceModalBottomSheet(
            onDismissRequest = { onIntent(AnalyticsIntent.FilterDismissed) }
        ) {
            AnalyticsAccountSheet(
                accounts = state.availableAccounts,
                selectedAccountId = state.filter.accountId,
                onAccountClick = { accountId -> onIntent(AnalyticsIntent.AccountSelected(accountId)) }
            )
        }
        null -> Unit
    }
}
