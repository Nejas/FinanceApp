package com.example.financeapp.presentation.analytics.bottomsheets.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.base.FinanceSheetPrimaryButton

@Composable
fun SheetPrimaryAction(
    text: String,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    FinanceSheetPrimaryButton(
        modifier = Modifier.padding(start = spacing.sheetButtonHorizontal,
            end = spacing.sheetButtonHorizontal,
            top = spacing.sheetButtonTop,
            bottom = spacing.sheetButtonBottom),
        text = text,
        onClick = onClick
    )
}
