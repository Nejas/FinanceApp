package com.example.financeapp.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financeapp.R
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.icons.FinanceBarChartIcon
import com.example.financeapp.presentation.common.icons.FinanceCalendarIcon
import com.example.financeapp.presentation.common.icons.FinanceSlidersIcon
import com.example.financeapp.presentation.common.utils.formatDayMonth
import java.time.LocalDate

@Composable
fun AppTopBar(
    selectedDate: LocalDate,
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val dateShape = RoundedCornerShape(spacing.topBarDateIcon)

    Row(
        modifier = modifier
            .height(spacing.topBarHeight)
            .padding(horizontal = spacing.l),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .clip(dateShape)
                .height(32.dp)
                .width(104.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = dateShape
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = spacing.xs,
                    vertical = spacing.xs
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                FinanceCalendarIcon(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(spacing.IconSize)
                )
                Text(
                    text = selectedDate.formatDayMonth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row (Modifier.height(48.dp).width(100.dp)){
            TopBarIconButton(
            type = TopBarIconType.ANALYTICS,
            contentDescription = stringResource(R.string.open_analytics),
            onClick = onAnalyticsClick
        )
            TopBarIconButton(
                type = TopBarIconType.SETTINGS,
                contentDescription = stringResource(R.string.settings_open),
                onClick = onSettingsClick
            ) }

    }
}

@Composable
fun DetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val backDescription = stringResource(R.string.back)

    Row(
        modifier = modifier
            .height(spacing.topBarHeight)
            .padding(horizontal = spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(spacing.topBarBackSize)
                .clip(CircleShape)
                .clickable(onClick = onBackClick)
                .semantics { contentDescription = backDescription },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TopBarIconButton(
    type: TopBarIconType,
    contentDescription: String,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .padding(start = spacing.s)
            .size(spacing.NavigationIconCinteinerSize)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.background)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            TopBarIconType.ANALYTICS -> FinanceBarChartIcon(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(spacing.NavigationIconSize)
            )
            TopBarIconType.SETTINGS -> FinanceSlidersIcon(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(spacing.NavigationIconSize)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AppTopBarPreview() {
    FinanceAppTheme(dynamicColor = false) {
        AppTopBar(
            selectedDate = LocalDate.of(2026, 7, 12),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DetailTopBarPreview() {
    FinanceAppTheme(dynamicColor = false) {
        DetailTopBar(
            title = "Расходы",
            onBackClick = {}
        )
    }
}

private enum class TopBarIconType {
    ANALYTICS,
    SETTINGS
}
