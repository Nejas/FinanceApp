package com.example.financeapp.presentation.bottomSheets.components.currency

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.R
import com.example.financeapp.core.theme.CurrencyCodeTextStyle
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.FinanceCurrencyCheck
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.presentation.common.components.base.FinanceSelectionSheetScaffold
import com.example.financeapp.presentation.common.components.icons.FinanceCheckIcon
import com.example.financeapp.presentation.common.model.CurrencyUiModel
import com.example.financeapp.presentation.common.model.toUiModel

@Composable
fun FinanceCurrencySelectionSheetContent(
    selectedCurrency: Currency,
    onCurrencyClick: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    FinanceSelectionSheetScaffold(
        title = stringResource(R.string.editor_currency),
        modifier = modifier,
        bottomPadding = LocalSpacing.current.currencySheetBottom
    ) {
        Currency.entries.forEach { currency ->
            FinanceCurrencySelectionRow(
                item = currency.toUiModel(),
                isSelected = currency == selectedCurrency,
                onClick = { onCurrencyClick(currency) }
            )
        }
    }
}

@Composable
private fun FinanceCurrencySelectionRow(
    item: CurrencyUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .height(sizing.selectionSheetTallRowHeight)
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = spacing.sheetTitleHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Text(
                text = item.flag,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.size(sizing.icon)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.xxs / 2)
            ) {
                Text(
                    text = stringResource(item.nameResId),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.currency.code,
                    style = CurrencyCodeTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isSelected) {
                FinanceCheckIcon(
                    color = FinanceCurrencyCheck,
                    modifier = Modifier.size(sizing.smallIcon)
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = spacing.hairline
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 460)
@Composable
private fun FinanceCurrencySelectionSheetContentPreview() {
    FinanceAppTheme(dynamicColor = false) {
        FinanceCurrencySelectionSheetContent(
            selectedCurrency = Currency.RUB,
            onCurrencyClick = {}
        )
    }
}
