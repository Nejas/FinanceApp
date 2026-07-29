package com.example.financeapp.presentation.analytics

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.financeapp.R
import com.example.financeapp.domain.model.TransactionAnalysisCriteria
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.common.model.FinanceFieldIconType
import com.example.financeapp.presentation.common.model.FinanceFieldUi
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
    val field: AnalyticsFilterField,
    @StringRes val valueResId: Int? = null,
    val value: String = ""
)

@Immutable
sealed interface AnalyticsFilterField : FinanceFieldUi {
    val saveableKey: String

    data object TransactionType : AnalyticsFilterField {
        override val saveableKey: String = "transaction_type"
        override val titleResId: Int = R.string.analytics_filter_type
        override val icon: FinanceFieldIconType = FinanceFieldIconType.ListType
    }

    data object Period : AnalyticsFilterField {
        override val saveableKey: String = "period"
        override val titleResId: Int = R.string.analytics_filter_period
        override val icon: FinanceFieldIconType = FinanceFieldIconType.Calendar
    }

    data object Category : AnalyticsFilterField {
        override val saveableKey: String = "category"
        override val titleResId: Int = R.string.editor_category
        override val icon: FinanceFieldIconType = FinanceFieldIconType.Article
    }

    data object Account : AnalyticsFilterField {
        override val saveableKey: String = "account"
        override val titleResId: Int = R.string.editor_account
        override val icon: FinanceFieldIconType = FinanceFieldIconType.Account
    }
}

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
    return AnalyticsFilterFields.map { field ->
        TransactionAnalysisCriteriaUi(field = field)
    }
}

val AnalyticsFilterFields: List<AnalyticsFilterField> = listOf(
    AnalyticsFilterField.TransactionType,
    AnalyticsFilterField.Period,
    AnalyticsFilterField.Category,
    AnalyticsFilterField.Account
)
