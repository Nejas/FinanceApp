package com.example.financeapp.presentation.analytics

import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.analytics.bottomsheets.AnalyticsFilterType
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodType
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
        type: AnalyticsFilterType
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

    private fun AnalyticsFilterType.toFilterSheet(): AnalyticsFilterSheet {
        return when (this) {
            AnalyticsFilterType.Type -> AnalyticsFilterSheet.Type
            AnalyticsFilterType.Period -> AnalyticsFilterSheet.Period
            AnalyticsFilterType.Category -> AnalyticsFilterSheet.Category
            AnalyticsFilterType.Account -> AnalyticsFilterSheet.Account
        }
    }
}

data class AnalyticsFilterChange(
    val state: AnalyticsState,
    val filter: AnalyticsFilter,
    val periodFilter: AnalyticsPeriodFilterState,
    val shouldReload: Boolean
)
