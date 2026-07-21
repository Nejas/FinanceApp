package com.example.financeapp.presentation.common.components.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.icons.FinanceCheckIcon

enum class FinanceSelectionIndicatorType {
    CheckMark,
    CheckBox,
    Radio
}

@Composable
fun FinanceSelectionSheetScaffold(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 26.dp)
    ) {
        Text(
            modifier = Modifier.padding(start = spacing.sheetTitleHorizontal, top = spacing.sheetTitleVertical, bottom = spacing.sheetTitleVertical),
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
}

@Composable
fun FinanceSelectionRow(
    title: String,
    isSelected: Boolean,
    indicatorType: FinanceSelectionIndicatorType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showDivider: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val rowHeight = if (subtitle == null) {
        sizing.selectionSheetRowHeight
    } else {
        sizing.selectionSheetTallRowHeight
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .height(rowHeight)
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = spacing.sheetRowHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sheetRowGap)
        ) {
            leadingContent?.invoke()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            FinanceSelectionIndicator(
                isSelected = isSelected,
                type = indicatorType
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                thickness = spacing.hairline
            )
        }
    }
}

@Composable
fun FinanceSelectionIconFrame(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Surface(
        modifier = modifier.size(sizing.selectionSheetIcon),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(spacing.hairline, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
fun FinanceSheetPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sizing = LocalSizing.current

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(sizing.selectionSheetButtonHeight),
        shape = RoundedCornerShape(sizing.selectionSheetButtonCorner),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FinanceSelectionIndicator(
    isSelected: Boolean,
    type: FinanceSelectionIndicatorType
) {
    when (type) {
        FinanceSelectionIndicatorType.CheckMark -> {
            if (isSelected) {
                FinanceCheckIcon(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(LocalSizing.current.selectionIndicator)
                )
            }
        }
        FinanceSelectionIndicatorType.CheckBox -> FinanceCheckBoxIndicator(isSelected = isSelected)
        FinanceSelectionIndicatorType.Radio -> FinanceRadioIndicator(isSelected = isSelected)
    }
}

@Composable
private fun FinanceCheckBoxIndicator(
    isSelected: Boolean
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Surface(
        modifier = Modifier.size(sizing.selectionIndicator),
        shape = RoundedCornerShape(spacing.xxs),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (isSelected) {
            null
        } else {
            BorderStroke(spacing.hairline, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        if (isSelected) {
            Box(contentAlignment = Alignment.Center) {
                FinanceCheckIcon(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(sizing.selectionIndicatorInner)
                )
            }
        }
    }
}

@Composable
private fun FinanceRadioIndicator(
    isSelected: Boolean
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    if (isSelected) {
        Surface(
            modifier = Modifier.size(sizing.selectionIndicator),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                FinanceCheckIcon(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(sizing.selectionIndicatorInner)
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(sizing.selectionIndicator)
                .clip(CircleShape)
                .background(Color.Transparent)
                .then(
                    Modifier.border(
                        BorderStroke(spacing.hairline * 2f, MaterialTheme.colorScheme.outlineVariant),
                        CircleShape
                    )
                )
        )
    }
}
