package com.example.financeapp.presentation.common.components.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing

@Composable
fun RoundFrame(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp? = null
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val frameSize = size ?: sizing.listItemIcon

    Surface(
        modifier = modifier.size(frameSize),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(spacing.hairline, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
