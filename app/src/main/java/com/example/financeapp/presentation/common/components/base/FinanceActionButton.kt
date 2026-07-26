package com.example.financeapp.presentation.common.components.base

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.example.financeapp.core.theme.LocalSizing

@Composable
fun FinanceActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape? = null,
    modifier: Modifier = Modifier
) {
    val sizing = LocalSizing.current

    FloatingActionButton(
        modifier = modifier.size(sizing.fab),
        shape = shape ?: RoundedCornerShape(sizing.fabCorner),
        containerColor = containerColor,
        contentColor = contentColor,
        onClick = onClick
    ) {
        icon()
    }
}
