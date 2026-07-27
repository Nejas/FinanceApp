package com.example.financeapp.presentation.common.components.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.Money
import com.example.financeapp.presentation.common.model.FinanceListItemUiModel
import com.example.financeapp.presentation.common.utils.formatWithoutMinorUnits

@Composable
fun ListItemColumn(
    item: FinanceListItemUiModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (item.isPendingSync) {
                    Modifier.background(MaterialTheme.colorScheme.tertiaryContainer)
                } else {
                    Modifier
                }
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = spacing.listItemHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.listItemGap)
        ) {
            RoundFrame(
                content = {Text(text = item.leadingEmoji, style = MaterialTheme.typography.titleLarge)}

            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    color = item.contentColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                item.comment
                    ?.takeIf { it.isNotBlank() }
                    ?.let { comment ->
                        Text(
                            text = comment,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            color = item.contentColor(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
            }

            Text(
                text = item.money.formatWithoutMinorUnits(),
                style = MaterialTheme.typography.bodyMedium,
                color = item.contentColor(),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FinanceListItemUiModel.contentColor() = if (isPendingSync) {
    MaterialTheme.colorScheme.onTertiaryContainer
} else {
    MaterialTheme.colorScheme.onSurface
}

@Preview(showBackground = true)
@Composable
private fun FinanceListItemUiModelPreview() {
    FinanceAppTheme(dynamicColor = false) {
        ListItemColumn(
            item = FinanceListItemUiModel(
                id = "preview",
                title = "Продукты",
                leadingEmoji = "🛒",
                comment = "Сбербанк",
                money = Money(amountInMinorUnits = 125000)
            )
        )
    }
}
