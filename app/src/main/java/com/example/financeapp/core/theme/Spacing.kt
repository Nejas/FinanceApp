package com.example.financeapp.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val default: Dp = 0.dp,
    val hairline: Dp = 1.dp,
    val xxs: Dp = 4.dp,
    val xs: Dp = 6.dp,
    val contentGap: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val listItemVertical: Dp = 14.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val actionGap: Dp = 24.dp,
    val topBarDateIcon: Dp = 28.dp,
    val xl: Dp = 32.dp,
    val screenHeaderTop: Dp = 36.dp,
    val topBarActionIcon: Dp = 36.dp,
    val xxl: Dp = 40.dp,
    val topBarActionSize: Dp = 44.dp,
    val topBarBackSize: Dp = 48.dp,
    val totalBottomGap: Dp = 52.dp,
    val iconContainer: Dp = 56.dp,
    val topBarHeight: Dp = 56.dp,
    val totalSurfaceHeight: Dp = 100.dp,
    val screenBottomContent: Dp = 96.dp,
    val contentSwipeEdgeGuard: Dp = 32.dp,
    val contentSwipeThreshold: Dp = 80.dp,


    val itemHeight: Dp = 72.dp,

    val itemIconContainer:Dp = 40.dp,
    val NavigationIconCinteinerSize:Dp = 48.dp,

    val IconSize:Dp = 20.dp,
    val NavigationIconSize:Dp = 28.dp,
    val s: Dp = 8.dp,
    val xm: Dp = 12.dp,
    val m: Dp = 16.dp,
    val mx: Dp = 20.dp,
    val l: Dp = 24.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
