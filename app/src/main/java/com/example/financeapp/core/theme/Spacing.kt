package com.example.financeapp.core.theme

import android.inputmethodservice.Keyboard
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
    val contentSwipeEdgeGuard: Dp = 32.dp,
    val contentSwipeThreshold: Dp = 80.dp
)

@Immutable
data class Sizing(
    val keyboardButtonHeight:Dp = 48.dp,
    val topBarHeight: Dp = 64.dp,
    val dateChipWidth: Dp = 104.dp,
    val dateChipHeight: Dp = 32.dp,
    val dateChipCorner: Dp = 100.dp,
    val topBarActionSize: Dp = 48.dp,
    val topBarActionsWidth: Dp = 100.dp,
    val detailBackButton: Dp = 48.dp,
    val smallIcon: Dp = 20.dp,
    val icon: Dp = 24.dp,
    val heroHeight: Dp = 117.dp,
    val listItemHeight: Dp = 72.dp,
    val listItemIcon: Dp = 40.dp,
    val navigationBarHeight: Dp = 80.dp,
    val fab: Dp = 56.dp,
    val fabCorner: Dp = 16.dp,
    val splashIcon: Dp = 192.dp,
    val bottomSpacerHeight:Dp = 20.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
val LocalSizing = staticCompositionLocalOf { Sizing() }
