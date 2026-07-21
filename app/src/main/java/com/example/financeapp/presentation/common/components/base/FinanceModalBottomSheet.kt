package com.example.financeapp.presentation.common.components.base

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financeapp.core.theme.FinanceAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showHandle: Boolean = true,
    skipPartiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(
            topStart = BottomSheetCorner,
            topEnd = BottomSheetCorner
        ),
        dragHandle = if (showHandle) {
            { FinanceModalBottomSheetHandle() }
        } else {
            null
        },
        content = content
    )
}

@Composable
private fun FinanceModalBottomSheetHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .size(width = 32.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FinanceModalBottomSheetPreview() {
    FinanceAppTheme(dynamicColor = false) {
        FinanceModalBottomSheet(onDismissRequest = {}) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                text = "Bottom sheet content",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private val BottomSheetCorner = 24.dp
