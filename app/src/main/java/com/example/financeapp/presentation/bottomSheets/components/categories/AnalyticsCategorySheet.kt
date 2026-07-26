package com.example.financeapp.presentation.bottomSheets.components.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.domain.model.Category
import com.example.financeapp.presentation.bottomSheets.components.common.SheetPrimaryAction
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType

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

    FinanceCategorySelectionSheetContent(
        categories = categories,
        selectedCategoryIds = draftCategoryIds,
        indicatorType = FinanceSelectionIndicatorType.CheckBox,
        onCategoryClick = { categoryId ->
            val updatedCategoryIds = draftCategoryIds.toggle(categoryId)
            if (updatedCategoryIds.isNotEmpty()) {
                draftCategoryIds = updatedCategoryIds
            }
        },
        actions = {
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
    )
}

private fun Set<Long>.toggle(id: Long): Set<Long> {
    return if (id in this) this - id else this + id
}
