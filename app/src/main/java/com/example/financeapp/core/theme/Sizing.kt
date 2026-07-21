package com.example.financeapp.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Sizing(
    val keyboardButtonHeight:Dp = 48.dp,
    val topBarHeight: Dp = 64.dp,
    val topBarActionSize: Dp = 48.dp,
    val topBarActionsWidth: Dp = 100.dp,
    val detailBackButton: Dp = 48.dp,
    val dateChipWidth: Dp = 104.dp,
    val dateChipHeight: Dp = 32.dp,
    val dateChipCorner: Dp = 100.dp,
    val smallIcon: Dp = 20.dp,
    val icon: Dp = 24.dp,
    val textOvalFrameCorner: Dp = 100.dp,
    val heroHeight: Dp = 117.dp,
    val listItemHeight: Dp = 72.dp,
    val listItemIcon: Dp = 40.dp,
    val analyticsChartSize: Dp = 220.dp,
    val analyticsChartStroke: Dp = 22.dp,
    val analyticsDetailChartSize: Dp = 240.dp,
    val analyticsDetailChartStroke: Dp = 25.dp,
    val analyticsLegendDot: Dp = 12.dp,
    val analyticsDetailCategoryDot: Dp = 12.dp,
    val analyticsDetailProgressHeight: Dp = 6.dp,
    val analyticsFilterRowHeight: Dp = 56.dp,
    val analyticsFilterIcon: Dp = 32.dp,
    val analyticsFilterInnerIcon: Dp = 20.dp,
    val analyticsChipHeight: Dp = 24.dp,
    val analyticsFilterValueMaxWidth: Dp = 200.dp,
    val selectionSheetRowHeight: Dp = 64.dp,
    val selectionSheetTallRowHeight: Dp = 69.dp,
    val selectionSheetIcon: Dp = 32.dp,
    val selectionIndicator: Dp = 24.dp,
    val selectionIndicatorInner: Dp = 16.dp,
    val selectionSheetButtonHeight: Dp = 52.dp,
    val selectionSheetButtonCorner: Dp = 28.dp,
    val analyticsCustomPeriodSheetMaxHeightFraction: Float = 0.9f,
    val customPeriodDateRowHeight: Dp = 60.dp,
    val dateRangePillHeight: Dp = 36.dp,
    val dateRangePillCorner: Dp = 8.dp,
    val dateRangeDividerWidth: Dp = 12.dp,
    val dateRangeDividerHeight: Dp = 1.dp,
    val customPeriodCalendarHeaderHeight: Dp = 40.dp,
    val customPeriodWeekDaysHeight: Dp = 24.dp,
    val calendarDayHeight: Dp = 40.dp,
    val calendarSelectedDay: Dp = 40.dp,
    val customPeriodCancelButtonMinWidth: Dp = 83.dp,
    val customPeriodApplyButtonMinWidth: Dp = 123.dp,
    val customPeriodActionButtonHeight: Dp = 36.dp,
    val customPeriodApplyButtonCorner: Dp = 20.dp,
    val navigationBarHeight: Dp = 80.dp,
    val fab: Dp = 56.dp,
    val fabCorner: Dp = 16.dp,
    val splashIcon: Dp = 192.dp,
    val bottomSpacerHeight:Dp = 20.dp
)

val LocalSizing = staticCompositionLocalOf { Sizing() }
