package com.example.financeapp.presentation.analytics

import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodResolver
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodType
import com.example.financeapp.presentation.analytics.mappers.AnalyticsFilterUiMapper
import com.example.financeapp.presentation.common.model.FinanceFieldType
import java.time.LocalDate
import javax.inject.Inject

class AnalyticsFilterReducer @Inject constructor(
    private val periodResolver: AnalyticsPeriodResolver,
    private val filterUiMapper: AnalyticsFilterUiMapper
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
        return state.copy(activeFilterSheet = AnalyticsFilterSheet.Period)
    }

    fun applyType(
        state: AnalyticsState,
        currentFilter: AnalyticsFilter,
        currentPeriodFilter: AnalyticsPeriodFilterState,
        type: TransactionType?
    ): AnalyticsFilterChange {
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
        currentFilter: AnalyticsFilter,
        currentPeriodFilter: AnalyticsPeriodFilterState,
        periodType: AnalyticsPeriodType
    ): AnalyticsFilterChange {
        if (periodType == AnalyticsPeriodType.Custom) {
            return AnalyticsFilterChange(
                state = state.copy(activeFilterSheet = AnalyticsFilterSheet.CustomPeriod),
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
        currentFilter: AnalyticsFilter,
        startDate: LocalDate,
        endDate: LocalDate
    ): AnalyticsFilterChange {
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
        currentFilter: AnalyticsFilter,
        currentPeriodFilter: AnalyticsPeriodFilterState,
        categoryIds: Set<Long>
    ): AnalyticsFilterChange {
        return applyFilter(
            state = state,
            filter = currentFilter.copy(categoryIds = categoryIds),
            periodFilter = currentPeriodFilter
        )
    }

    fun applyAccount(
        state: AnalyticsState,
        currentFilter: AnalyticsFilter,
        currentPeriodFilter: AnalyticsPeriodFilterState,
        accountId: Long?
    ): AnalyticsFilterChange {
        return applyFilter(
            state = state,
            filter = currentFilter.copy(accountId = accountId),
            periodFilter = currentPeriodFilter
        )
    }

    private fun applyFilter(
        state: AnalyticsState,
        filter: AnalyticsFilter,
        periodFilter: AnalyticsPeriodFilterState
    ): AnalyticsFilterChange {
        return AnalyticsFilterChange(
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

    private fun FinanceFieldType.toFilterSheet(): AnalyticsFilterSheet? {
        return when (this) {
            FinanceFieldType.TransactionType -> AnalyticsFilterSheet.Type
            FinanceFieldType.Period -> AnalyticsFilterSheet.Period
            FinanceFieldType.Category -> AnalyticsFilterSheet.Category
            FinanceFieldType.Account -> AnalyticsFilterSheet.Account
            FinanceFieldType.Date,
            FinanceFieldType.Time,
            FinanceFieldType.Currency,
            FinanceFieldType.Name,
            FinanceFieldType.Emoji,
            FinanceFieldType.Description -> null
        }
    }
}

data class AnalyticsFilterChange(
    val state: AnalyticsState,
    val filter: AnalyticsFilter,
    val periodFilter: AnalyticsPeriodFilterState,
    val shouldReload: Boolean
)
