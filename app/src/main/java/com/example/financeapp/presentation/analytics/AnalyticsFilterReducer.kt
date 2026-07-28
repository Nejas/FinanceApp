package com.example.financeapp.presentation.analytics

import androidx.compose.runtime.Immutable
import com.example.financeapp.domain.model.TransactionAnalysisCriteria
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodResolver
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodType
import com.example.financeapp.presentation.analytics.mappers.TransactionAnalysisCriteriaUiMapper
import com.example.financeapp.presentation.common.model.FinanceFieldType
import java.time.LocalDate
import javax.inject.Inject

class TransactionAnalysisCriteriaReducer @Inject constructor(
    private val periodResolver: AnalyticsPeriodResolver,
    private val filterUiMapper: TransactionAnalysisCriteriaUiMapper
) {

    fun showDetail(state: AnalyticsState): AnalyticsState {
        return state.copy(
            isDetailVisible = true,
            activeFilterSheet = null
        )
    }

    fun hideDetail(state: AnalyticsState): AnalyticsState {
        return state.copy(isDetailVisible = false)
    }

    fun openFilterSheet(
        state: AnalyticsState,
        type: FinanceFieldType
    ): AnalyticsState {
        return state.copy(
            activeFilterSheet = type.toFilterSheet(),
            isDetailVisible = false
        )
    }

    fun dismissFilterSheet(state: AnalyticsState): AnalyticsState {
        return state.copy(activeFilterSheet = null)
    }

    fun returnToPeriodSheet(state: AnalyticsState): AnalyticsState {
        return state.copy(activeFilterSheet = TransactionAnalysisCriteriaSheet.Period)
    }

    fun applyType(
        state: AnalyticsState,
        currentFilter: TransactionAnalysisCriteria,
        currentPeriodFilter: AnalyticsPeriodFilterState,
        type: TransactionType?
    ): TransactionAnalysisCriteriaChange {
        return applyFilter(
            state = state,
            filter = currentFilter.copy(
                type = type,
                categoryIds = emptySet()
            ),
            periodFilter = currentPeriodFilter
        )
    }

    fun selectPeriod(
        state: AnalyticsState,
        currentFilter: TransactionAnalysisCriteria,
        currentPeriodFilter: AnalyticsPeriodFilterState,
        periodType: AnalyticsPeriodType
    ): TransactionAnalysisCriteriaChange {
        if (periodType == AnalyticsPeriodType.Custom) {
            return TransactionAnalysisCriteriaChange(
                state = state.copy(activeFilterSheet = TransactionAnalysisCriteriaSheet.CustomPeriod),
                filter = currentFilter,
                periodFilter = currentPeriodFilter,
                shouldReload = false
            )
        }

        val periodFilter = periodResolver.resolvePeriod(
            periodType = periodType,
            currentPeriodFilter = currentPeriodFilter
        )
        return applyFilter(
            state = state,
            filter = currentFilter.copy(
                startDate = periodFilter.startDate,
                endDate = periodFilter.endDate
            ),
            periodFilter = periodFilter
        )
    }

    fun applyCustomPeriod(
        state: AnalyticsState,
        currentFilter: TransactionAnalysisCriteria,
        startDate: LocalDate,
        endDate: LocalDate
    ): TransactionAnalysisCriteriaChange {
        val periodFilter = periodResolver.resolveCustomPeriod(
            startDate = startDate,
            endDate = endDate
        )
        return applyFilter(
            state = state,
            filter = currentFilter.copy(
                startDate = periodFilter.startDate,
                endDate = periodFilter.endDate
            ),
            periodFilter = periodFilter
        )
    }

    fun applyCategories(
        state: AnalyticsState,
        currentFilter: TransactionAnalysisCriteria,
        currentPeriodFilter: AnalyticsPeriodFilterState,
        categoryIds: Set<Long>
    ): TransactionAnalysisCriteriaChange {
        return applyFilter(
            state = state,
            filter = currentFilter.copy(categoryIds = categoryIds),
            periodFilter = currentPeriodFilter
        )
    }

    fun applyAccount(
        state: AnalyticsState,
        currentFilter: TransactionAnalysisCriteria,
        currentPeriodFilter: AnalyticsPeriodFilterState,
        accountId: Long?
    ): TransactionAnalysisCriteriaChange {
        return applyFilter(
            state = state,
            filter = currentFilter.copy(accountId = accountId),
            periodFilter = currentPeriodFilter
        )
    }

    private fun applyFilter(
        state: AnalyticsState,
        filter: TransactionAnalysisCriteria,
        periodFilter: AnalyticsPeriodFilterState
    ): TransactionAnalysisCriteriaChange {
        return TransactionAnalysisCriteriaChange(
            state = state.copy(
                filter = filter,
                periodFilter = periodFilter,
                activeFilterSheet = null,
                filters = filterUiMapper.map(
                    filter = filter,
                    periodFilter = periodFilter,
                    categories = state.availableCategories,
                    accounts = state.availableAccounts
                )
            ),
            filter = filter,
            periodFilter = periodFilter,
            shouldReload = true
        )
    }

    private fun FinanceFieldType.toFilterSheet(): TransactionAnalysisCriteriaSheet? {
        return when (this) {
            FinanceFieldType.TransactionType -> TransactionAnalysisCriteriaSheet.Type
            FinanceFieldType.Period -> TransactionAnalysisCriteriaSheet.Period
            FinanceFieldType.Category -> TransactionAnalysisCriteriaSheet.Category
            FinanceFieldType.Account -> TransactionAnalysisCriteriaSheet.Account
            FinanceFieldType.Date,
            FinanceFieldType.Time,
            FinanceFieldType.Currency,
            FinanceFieldType.Name,
            FinanceFieldType.Emoji,
            FinanceFieldType.Description -> null
        }
    }
}

@Immutable
data class TransactionAnalysisCriteriaChange(
    val state: AnalyticsState,
    val filter: TransactionAnalysisCriteria,
    val periodFilter: AnalyticsPeriodFilterState,
    val shouldReload: Boolean
)
