package com.example.financeapp.presentation.common.components

import androidx.compose.foundation.layout.Column
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
import com.example.financeapp.core.theme.LocalSpacing

@Composable
fun TotalSumSurface(
    totalLabel: String,
    totalSum: String,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(spacing.totalSurfaceHeight)
    ) {
        Column {
            Text(
                modifier = Modifier.padding(start = spacing.m, top = spacing.s),
                text = totalLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                modifier = Modifier.padding(
                    start = spacing.m,
                    top = spacing.xxs,
                    bottom = spacing.xs
                ),
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
