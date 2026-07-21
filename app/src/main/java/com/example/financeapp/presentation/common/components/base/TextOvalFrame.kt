package com.example.financeapp.presentation.common.components.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing

@Composable
fun TextOvalFrame(
    text: String,
    modifier: Modifier = Modifier,
    minWidth: Dp? = null,
    maxWidth: Dp? = null
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Surface(
        modifier = modifier
            .height(sizing.analyticsChipHeight)
            .then(
                when {
                    minWidth != null && maxWidth != null -> Modifier.widthIn(
                        min = minWidth,
                        max = maxWidth
                    )
                    minWidth != null -> Modifier.widthIn(min = minWidth)
                    maxWidth != null -> Modifier.widthIn(max = maxWidth)
                    else -> Modifier
                }
            ),
        shape = RoundedCornerShape(sizing.textOvalFrameCorner),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(spacing.hairline, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = spacing.textOvalFrameHorizontal),
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TextOvalFramePreview() {
    TextOvalFrame(
        text = "Ремонт, Авто, Супермаркеты, Транспорт",
        maxWidth = LocalSizing.current.analyticsFilterValueMaxWidth
    )
}
