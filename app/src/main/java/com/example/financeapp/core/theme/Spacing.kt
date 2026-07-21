package com.example.financeapp.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val none: Dp = 0.dp,
    val hairline: Dp = 1.dp,
    val xxs: Dp = 4.dp,
    val xs: Dp = 6.dp,
    val s: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 32.dp,
    val contentGap: Dp = 8.dp,
    val actionGap: Dp = 24.dp,
    val topBarHorizontal: Dp = 12.dp,
    val topBarActionsGap: Dp = 4.dp,
    val textOvalFrameHorizontal: Dp = 12.dp,
    val dateChipHorizontal: Dp = 12.dp,
    val dateChipVertical: Dp = 6.dp,
    val dateChipGap: Dp = 4.dp,
    val heroHorizontal: Dp = 20.dp,
    val heroTop: Dp = 12.dp,
    val heroBottom: Dp = 32.dp,
    val heroGap: Dp = 4.dp,
    val listItemHorizontal: Dp = 16.dp,
    val listItemGap: Dp = 16.dp,
    val listBottomPadding: Dp = 120.dp,
    val analyticsContentHorizontal: Dp = 16.dp,
    val analyticsChartTop: Dp = 32.dp,
    val analyticsLegendTop: Dp = 24.dp,
    val analyticsLegendBottom: Dp = 32.dp,
    val analyticsTransactionsTop: Dp = 45.dp,
    val analyticsSectionTitleBottom: Dp = 16.dp,
    val analyticsBottomPadding: Dp = 24.dp,
    val analyticsDetailTitleTop: Dp = 4.dp,
    val analyticsDetailChartTop: Dp = 34.dp,
    val analyticsDetailCategoriesTop: Dp = 28.dp,
    val analyticsDetailContentGap: Dp = 32.dp,
    val analyticsDetailCategoryGap: Dp = 24.dp,
    val analyticsDetailCategoryHorizontal: Dp = 24.dp,
    val analyticsDetailCategoryVertical: Dp = 14.dp,
    val analyticsDetailCategoryLabelGap: Dp = 8.dp,
    val analyticsDetailProgressTop: Dp = 8.dp,
    val analyticsDetailBottomPadding: Dp = 32.dp,
    val sheetTitleHorizontal: Dp = 20.dp,
    val sheetTitleVertical: Dp = 12.dp,
    val sheetRowHorizontal: Dp = 20.dp,
    val sheetRowGap: Dp = 16.dp,
    val sheetButtonHorizontal: Dp = 16.dp,
    val sheetButtonTop: Dp = 26.dp,
    val sheetButtonBottom: Dp = 16.dp,
    val sheetCalendarHorizontal: Dp = 24.dp,
    val customPeriodTitleHorizontal: Dp = 24.dp,
    val customPeriodTitleBottom: Dp = 16.dp,
    val customPeriodDateHorizontal: Dp = 24.dp,
    val customPeriodDateGap: Dp = 12.dp,
    val customPeriodCalendarGap: Dp = 24.dp,
    val customPeriodCalendarHeaderHorizontal: Dp = 16.dp,
    val customPeriodCalendarHeaderVertical: Dp = 8.dp,
    val customPeriodCalendarGridHorizontal: Dp = 32.dp,
    val customPeriodActionsTop: Dp = 32.dp,
    val customPeriodActionsHorizontal: Dp = 24.dp,
    val customPeriodActionsGap: Dp = 12.dp,
    val contentSwipeEdgeGuard: Dp = 32.dp,
    val contentSwipeThreshold: Dp = 80.dp
)



val LocalSpacing = staticCompositionLocalOf { Spacing() }
