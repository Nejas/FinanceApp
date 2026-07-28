package com.example.financeapp.presentation.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.financeapp.presentation.common.components.icons.FinanceAccountCardIcon
import com.example.financeapp.presentation.common.components.icons.FinanceArticleIcon
import com.example.financeapp.presentation.common.components.icons.FinanceCalendarIcon
import com.example.financeapp.presentation.common.components.icons.FinanceClockIcon
import com.example.financeapp.presentation.common.components.icons.FinanceCurrencyIcon
import com.example.financeapp.presentation.common.components.icons.FinanceEmojiIcon
import com.example.financeapp.presentation.common.components.icons.FinanceListTypeIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsArticleIcon
import com.example.financeapp.presentation.common.components.icons.FinanceTagIcon
import com.example.financeapp.presentation.common.model.FinanceFieldType

@Composable
fun FinanceFieldIcon(
    type: FinanceFieldType,
    color: Color,
    modifier: Modifier = Modifier
) {
    when (type) {
        FinanceFieldType.Account ->FinanceAccountCardIcon(color = color, modifier = modifier)
        FinanceFieldType.Emoji -> FinanceEmojiIcon(color = color, modifier = modifier)
        FinanceFieldType.Name -> FinanceTagIcon(color = color, modifier = modifier)

        FinanceFieldType.Category -> FinanceArticleIcon(color = color, modifier = modifier)
        FinanceFieldType.Description -> FinanceSettingsArticleIcon(color = color, modifier = modifier)
        FinanceFieldType.TransactionType -> FinanceListTypeIcon(color = color, modifier = modifier)
        FinanceFieldType.Date -> FinanceCalendarIcon(color = color, modifier = modifier)
        FinanceFieldType.Time -> FinanceClockIcon(color = color, modifier = modifier)
        FinanceFieldType.Period -> FinanceCalendarIcon(color = color, modifier = modifier)

        FinanceFieldType.Currency -> FinanceCurrencyIcon(color = color, modifier = modifier)
    }
}
