package com.example.financeapp.presentation.analytics

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.financeapp.domain.model.TransactionAnalysisCriteria
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.common.model.FinanceFieldType
import com.example.financeapp.presentation.common.model.FinanceListItemUiModel
import com.example.financeapp.presentation.common.placeholders.ScreenError

@Immutable
data class AnalyticsState(
    val filter: TransactionAnalysisCriteria,
    val periodFilter: AnalyticsPeriodFilterState,
    val total: Money = Money(amountInMinorUnits = 0),
    val categories: List<AnalyticsCategoryUi> = emptyList(),
    val categoryColors: Map<Long, Color> = emptyMap(),
    val filters: List<TransactionAnalysisCriteriaUi> = defaultTransactionAnalysisCriterias(),
    val availableCategories: List<Category> = emptyList(),
    val availableAccounts: List<Account> = emptyList(),
    val activeFilterSheet: TransactionAnalysisCriteriaSheet? = null,
    val isDetailVisible: Boolean = false,
    val transactions: List<FinanceListItemUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: ScreenError? = null
)

@Immutable
data class AnalyticsCategoryUi(
    val categoryId: Long,
    val title: String,
    val emoji: String,
    val amount: Money,
    val percent: Int
)

@Immutable
data class TransactionAnalysisCriteriaUi(
    val type: FinanceFieldType,
    @StringRes val valueResId: Int? = null,
    val value: String = ""
)

enum class TransactionAnalysisCriteriaSheet {
    Type,
    Period,
    CustomPeriod,
    Category,
    Account
}

fun defaultTransactionAnalysisCriteria(periodFilter: AnalyticsPeriodFilterState): TransactionAnalysisCriteria {
    return TransactionAnalysisCriteria(
        accountId = null,
        startDate = periodFilter.startDate,
        endDate = periodFilter.endDate,
        type = TransactionType.EXPENSE,
        currency = Currency.RUB
    )
}

fun defaultTransactionAnalysisCriterias(): List<TransactionAnalysisCriteriaUi> {
    return AnalyticsFieldTypes.map { type ->
        TransactionAnalysisCriteriaUi(type = type)
    }
}

val AnalyticsFieldTypes: List<FinanceFieldType> = listOf(
    FinanceFieldType.TransactionType,
    FinanceFieldType.Period,
    FinanceFieldType.Category,
    FinanceFieldType.Account
)
