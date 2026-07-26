package com.example.financeapp.presentation.common.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.financeapp.R

@Immutable
enum class FinanceFieldType(
    @StringRes val titleResId: Int
) {
    Account(R.string.editor_account),
    Category(R.string.editor_category),
    TransactionType(R.string.analytics_filter_type),
    Date(R.string.editor_date),
    Time(R.string.editor_time),
    Currency(R.string.editor_currency),
    Period(R.string.analytics_filter_period),
    Name(R.string.editor_name),
    Emoji(R.string.editor_emoji),
    Description(R.string.editor_description)
}
