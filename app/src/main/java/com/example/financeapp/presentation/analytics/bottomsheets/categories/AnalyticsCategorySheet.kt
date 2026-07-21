package com.example.financeapp.presentation.analytics.bottomsheets.categories

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.financeapp.R
import com.example.financeapp.domain.model.Category
import com.example.financeapp.presentation.analytics.bottomsheets.common.SheetPrimaryAction
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIconFrame
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType
import com.example.financeapp.presentation.common.components.base.FinanceSelectionRow
import com.example.financeapp.presentation.common.components.base.FinanceSelectionSheetScaffold

@Composable
internal fun AnalyticsCategorySheet(
    categories: List<Category>,
    selectedCategoryIds: Set<Long>,
    onApply: (Set<Long>) -> Unit
) {
    val allCategoryIds = remember(categories) { categories.map { category -> category.id }.toSet() }
    var draftCategoryIds by remember(categories, selectedCategoryIds) {
        val availableSelectedCategoryIds = selectedCategoryIds.intersect(allCategoryIds)
        mutableStateOf(availableSelectedCategoryIds.ifEmpty { allCategoryIds })
    }

    FinanceSelectionSheetScaffold(title = stringResource(R.string.analytics_filter_articles)) {
        categories.forEach { category ->
            FinanceSelectionRow(
                title = category.name,
                isSelected = category.id in draftCategoryIds,
                indicatorType = FinanceSelectionIndicatorType.CheckBox,
                onClick = {
                    val updatedCategoryIds = draftCategoryIds.toggle(category.id)
                    if (updatedCategoryIds.isNotEmpty()) {
                        draftCategoryIds = updatedCategoryIds
                    }
                },
                leadingContent = {
                    FinanceSelectionIconFrame(content = {
                        Text(
                            text = category.emoji,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    })
                }
            )
        }
        SheetPrimaryAction(text = stringResource(R.string.picker_apply)) {
            onApply(
                if (draftCategoryIds == allCategoryIds) {
                    emptySet()
                } else {
                    draftCategoryIds
                }
            )
        }
    }
}

private fun Set<Long>.toggle(id: Long): Set<Long> {
    return if (id in this) this - id else this + id
}
