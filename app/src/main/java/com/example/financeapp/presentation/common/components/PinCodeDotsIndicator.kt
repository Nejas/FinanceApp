package com.example.financeapp.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.icons.FinancePinCodeEmptyDotIcon
import com.example.financeapp.presentation.common.components.icons.FinancePinCodeFilledDotIcon

@Composable
fun PinCodeDotsIndicator(
    filledCount: Int,
    modifier: Modifier = Modifier,
    totalCount: Int = PinCodeLength,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.pinCodeDotGap)
    ) {
        repeat(totalCount) { index ->
            val dotModifier = Modifier.size(sizing.pinCodeDot)
            if (index < filledCount) {
                FinancePinCodeFilledDotIcon(
                    color = color,
                    modifier = dotModifier
                )
            } else {
                FinancePinCodeEmptyDotIcon(
                    color = color,
                    modifier = dotModifier
                )
            }
        }
    }
}

const val PinCodeLength = 4
