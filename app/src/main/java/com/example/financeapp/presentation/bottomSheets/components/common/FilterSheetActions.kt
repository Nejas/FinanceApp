package com.example.financeapp.presentation.bottomSheets.components.common

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.base.FinanceSheetPrimaryButton

@Composable
fun SheetPrimaryAction(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current

    FinanceSheetPrimaryButton(
        modifier = modifier.padding(start = spacing.sheetButtonHorizontal,
            end = spacing.sheetButtonHorizontal,
            top = spacing.sheetButtonTop,),
        text = text,
        onClick = onClick
    )
}
