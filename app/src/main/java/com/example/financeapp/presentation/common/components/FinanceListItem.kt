package com.example.financeapp.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.model.FinanceListItemUiModel

@Composable
fun FinanceListItem(
    item: FinanceListItemUiModel,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(item.id) }
            .padding(horizontal = spacing.md, vertical = spacing.listItemVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xl)
    ) {
        Surface(
            modifier = Modifier.size(spacing.itemIconContainer),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(spacing.hairline, MaterialTheme.colorScheme.outline)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = item.leadingEmoji, style = MaterialTheme.typography.titleLarge)
            }
        }
        Text(
            text = item.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )


        Text(
            text = item.trailingText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FinanceListItemPreview() {
    FinanceAppTheme(dynamicColor = false) {
        FinanceListItem(
            item = FinanceListItemUiModel(
                id = "preview",
                title = "Продукты",
                leadingEmoji = "🛒",
                trailingText = "-1 250 ₽"
            ),
            onClick = {}
        )
    }
}
