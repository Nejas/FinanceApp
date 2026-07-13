package com.example.financeapp.presentation.common.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.presentation.common.icons.FinancePlusIcon

@Composable
fun AddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sizing = LocalSizing.current

    FloatingActionButton(
        modifier = modifier.size(sizing.fab),
        shape = RoundedCornerShape(sizing.fabCorner),
        containerColor = MaterialTheme.colorScheme.secondary,
        onClick = onClick
    ) {
        FinancePlusIcon(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(sizing.icon)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddButtonPreview() {
    FinanceAppTheme(dynamicColor = false) {
        AddButton(onClick = {})
    }
}
