package com.example.financeapp.presentation.bottomSheets.components.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.financeapp.R
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.Category
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType
import com.example.financeapp.presentation.common.components.base.FinanceSelectionRow
import com.example.financeapp.presentation.common.components.icons.FinanceBackIcon
import com.example.financeapp.presentation.common.components.icons.FinanceCloseIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSearchIcon

@Composable
fun FinanceCategorySelectionSheetContent(
    categories: List<Category>,
    selectedCategoryIds: Set<Long>,
    indicatorType: FinanceSelectionIndicatorType,
    onCategoryClick: (Long) -> Unit,
    actions: @Composable ColumnScope.() -> Unit = {}
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    var searchQuery by remember { mutableStateOf("") }
    val filteredCategories = remember(categories, searchQuery) {
        categories.filterByQuery(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = sizing.categorySelectionSheetMaxHeight)
            .padding(bottom = spacing.categorySheetListBottom)
    ) {
        Text(
            modifier = Modifier.padding(
                start = spacing.sheetTitleHorizontal,
                top = spacing.categorySheetTitleTop
            ),
            text = stringResource(R.string.settings_articles),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        FinanceCategorySearchField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
            },
            modifier = Modifier
                .padding(
                    start = spacing.categorySheetSearchHorizontal,
                    top = spacing.categorySheetSearchTop,
                    end = spacing.categorySheetSearchHorizontal,
                    bottom = spacing.categorySheetSearchBottom
                )
                .fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            items(
                items = filteredCategories,
                key = { category -> category.id }
            ) { category ->
                FinanceSelectionRow(
                    title = category.name,
                    isSelected = category.id in selectedCategoryIds,
                    indicatorType = indicatorType,
                    onClick = { onCategoryClick(category.id) },
                    rowHeight = sizing.categorySheetRowHeight,
                    leadingContent = {
                        Text(
                            text = category.emoji,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        }
        actions()
    }
}

@Composable
private fun FinanceCategorySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    val placeholderStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(sizing.categorySearchHeight)
            .clip(RoundedCornerShape(sizing.categorySearchCorner))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
        textStyle = textStyle,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sizing.categorySearchHeight)
                    .padding(
                        horizontal = spacing.categorySheetSearchContentHorizontal,
                        vertical = spacing.categorySheetSearchContentVertical
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
            ) {
                if (isFocused) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.size(sizing.topBarActionSize)
                    ) {
                        FinanceBackIcon(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(sizing.icon)
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text(
                            text = stringResource(R.string.settings_article_search_hint),
                            style = placeholderStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
                if (isFocused) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(sizing.topBarActionSize)
                    ) {
                        FinanceCloseIcon(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(sizing.icon)
                        )
                    }
                } else {
                    FinanceSearchIcon(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(sizing.icon)
                    )
                }
            }
        }
    )
}

private fun List<Category>.filterByQuery(query: String): List<Category> {
    val trimmedQuery = query.trim()
    return if (trimmedQuery.isBlank()) {
        this
    } else {
        filter { category ->
            category.name.contains(trimmedQuery, ignoreCase = true)
        }
    }
}
