package com.example.financeapp.presentation.analytics.bottomsheets.type

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.analytics.bottomsheets.common.SheetPrimaryAction
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType
import com.example.financeapp.presentation.common.components.base.FinanceSelectionRow
import com.example.financeapp.presentation.common.components.base.FinanceSelectionSheetScaffold

@Composable
internal fun AnalyticsTypeSheet(
    selectedType: TransactionType?,
    onApply: (TransactionType?) -> Unit
) {
    var draftType by remember(selectedType) { mutableStateOf(selectedType) }

    FinanceSelectionSheetScaffold(title = stringResource(R.string.analytics_filter_type)) {
        AnalyticsTypeOptions.forEach { option ->
            FinanceSelectionRow(
                title = stringResource(option.titleResId),
                isSelected = draftType == option.type,
                indicatorType = FinanceSelectionIndicatorType.Radio,
                onClick = { draftType = option.type }
            )
        }
        SheetPrimaryAction(text = stringResource(R.string.done)) {
            onApply(draftType)
        }
    }
}

private data class AnalyticsTypeOption(
    val type: TransactionType?,
    val titleResId: Int
)

private val AnalyticsTypeOptions = listOf(
    AnalyticsTypeOption(TransactionType.EXPENSE, R.string.analytics_filter_expenses),
    AnalyticsTypeOption(TransactionType.INCOME, R.string.analytics_filter_income),
    AnalyticsTypeOption(null, R.string.analytics_filter_all)
)
