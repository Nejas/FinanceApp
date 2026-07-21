package com.example.financeapp.presentation.common.components.base

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.FinanceMutedText
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing

@Composable
fun TotalSumSurface(
    totalLabel: String,
    totalSum: String,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(sizing.heroHeight)
    ) {
        Column(
            modifier = Modifier.padding(
                start = spacing.heroHorizontal,
                top = spacing.heroTop,
                end = spacing.heroHorizontal,
                bottom = spacing.heroBottom
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.heroGap)
        ) {
            Text(
                text = totalLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = FinanceMutedText
            )
            Text(
                text = totalSum,
                style = MaterialTheme.typography.displayLarge,
            )
        }
    }
}

@Preview
@Composable
fun TotalSurfacePreview() {
    FinanceAppTheme(dynamicColor = false) {
        TotalSumSurface("доходы", "123111")
    }
}
