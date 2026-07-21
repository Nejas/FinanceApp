package com.example.financeapp.presentation.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.accounts.AccountsRoute
import com.example.financeapp.presentation.analytics.AnalyticsRoute
import com.example.financeapp.presentation.common.components.base.AddButton
import com.example.financeapp.presentation.common.components.base.AppTopBar
import com.example.financeapp.presentation.expenses.ExpensesRoute
import com.example.financeapp.presentation.income.IncomeRoute
import com.example.financeapp.presentation.navigation.AppNavGraph
import com.example.financeapp.presentation.common.components.base.BottomNavigationBar
import com.example.financeapp.presentation.common.network.LifecycleNetworkRefreshEffect
import com.example.financeapp.presentation.common.network.NetworkStatusBanner
import com.example.financeapp.presentation.common.network.NetworkStatusViewModel
import com.example.financeapp.presentation.navigation.AppRoute
import com.example.financeapp.presentation.navigation.isMainRoute
import kotlin.math.abs
@Composable
fun FinanceApp(
    mainViewModel: MainViewModel = hiltViewModel(),
    networkStatusViewModel: NetworkStatusViewModel = hiltViewModel()
) {
    val spacing = LocalSpacing.current
    val density = LocalDensity.current
    val edgeGuardPx = with(density) { spacing.contentSwipeEdgeGuard.toPx() }
    val swipeThresholdPx = with(density) { spacing.contentSwipeThreshold.toPx() }
    val mainState by mainViewModel.state.collectAsState()
    val isOnline by networkStatusViewModel.isOnline.collectAsState()

    LifecycleNetworkRefreshEffect(
        refreshable = mainViewModel,
        isOnline = isOnline,
        refreshImmediately = false
    )

    var selectedRouteName by rememberSaveable {
        mutableStateOf(AppRoute.Expenses.route)
    }
    val selectedRoute = selectedRouteName.toAppRoute()

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (selectedRoute.isMainRoute()) {
                AddButton(onClick = {})
            }
        },
        bottomBar = {
            if (selectedRoute.isMainRoute()) {
                BottomNavigationBar(
                    selectedRoute = selectedRoute,
                    onRouteSelected = { route ->
                        selectedRouteName = route.route
                    },

                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isOnline) {
                NetworkStatusBanner(modifier = Modifier.fillMaxWidth())
            }

            if (selectedRoute.isMainRoute()) {
                AppTopBar(
                    modifier = Modifier.fillMaxWidth(),
                    selectedDate = mainState.selectedDate,
                    onAnalyticsClick = {
                        selectedRouteName = AppRoute.Analytics.route
                    },
                    onSettingsClick = {}
                )
            }

            AnimatedContent(
                targetState = selectedRoute,
                label = "MainRouteTransition",
                transitionSpec = {
                    val forward = targetState.animationOrder >= initialState.animationOrder
                    val slideDirection = if (forward) 1 else -1
                    (
                        fadeIn(animationSpec = tween(180)) +
                            slideInHorizontally(
                                animationSpec = tween(240),
                                initialOffsetX = { width -> width / 6 * slideDirection }
                            )
                        ).togetherWith(
                        fadeOut(animationSpec = tween(160)) +
                            slideOutHorizontally(
                                animationSpec = tween(220),
                                targetOffsetX = { width -> -width / 8 * slideDirection }
                            )
                    ).using(SizeTransform(clip = false))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(edgeGuardPx, swipeThresholdPx, selectedRoute) {
                        var dragOffset = 0f
                        var canHandleSwipe = false

                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                dragOffset = 0f
                                canHandleSwipe = offset.x > edgeGuardPx &&
                                    offset.x < size.width - edgeGuardPx
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                if (canHandleSwipe) {
                                    dragOffset += dragAmount
                                    change.consume()
                                }
                            },
                            onDragCancel = {
                                dragOffset = 0f
                                canHandleSwipe = false
                            },
                            onDragEnd = {
                                if (canHandleSwipe && abs(dragOffset) >= swipeThresholdPx) {
                                    val nextRoute = if (dragOffset < 0) {
                                        selectedRoute.nextMainRoute()
                                    } else {
                                        selectedRoute.previousMainRoute()
                                    }
                                    selectedRouteName = nextRoute.route
                                }
                                dragOffset = 0f
                                canHandleSwipe = false
                            }
                        )
                    }
            ) { animatedRoute ->
                AppNavGraph(
                    modifier = Modifier.fillMaxSize(),
                    currentRoute = animatedRoute,
                    expensesContent = { routeModifier ->
                        ExpensesRoute(
                            modifier = routeModifier,
                            state = mainState.expensesState,
                            onRetry = {
                                mainViewModel.onIntent(MainIntent.Retry)
                            }
                        )
                    },
                    incomeContent = { routeModifier ->
                        IncomeRoute(
                            modifier = routeModifier,
                            state = mainState.incomeState,
                            onRetry = {
                                mainViewModel.onIntent(MainIntent.Retry)
                            }
                        )
                    },
                    accountsContent = { routeModifier ->
                        AccountsRoute(
                            modifier = routeModifier,
                            state = mainState.accountsState,
                            onRetry = {
                                mainViewModel.onIntent(MainIntent.Retry)
                            }
                        )
                    },
                    analyticsContent = { routeModifier ->
                        AnalyticsRoute(
                            modifier = routeModifier,
                            onBack = {
                                selectedRouteName = AppRoute.Expenses.route
                            }
                        )
                    }
                )
            }
        }
    }
}

private fun String.toAppRoute(): AppRoute {
    return when (this) {
        AppRoute.Income.route -> AppRoute.Income
        AppRoute.Accounts.route -> AppRoute.Accounts
        AppRoute.Analytics.route -> AppRoute.Analytics
        else -> AppRoute.Expenses
    }
}

private val AppRoute.animationOrder: Int
    get() = when (this) {
        AppRoute.Expenses -> 0
        AppRoute.Income -> 1
        AppRoute.Accounts -> 2
        AppRoute.Analytics -> 3
    }

private fun AppRoute.nextMainRoute(): AppRoute {
    return when (this) {
        AppRoute.Expenses -> AppRoute.Income
        AppRoute.Income -> AppRoute.Accounts
        AppRoute.Accounts -> AppRoute.Accounts
        AppRoute.Analytics -> AppRoute.Expenses
    }
}

private fun AppRoute.previousMainRoute(): AppRoute {
    return when (this) {
        AppRoute.Expenses -> AppRoute.Expenses
        AppRoute.Income -> AppRoute.Expenses
        AppRoute.Accounts -> AppRoute.Income
        AppRoute.Analytics -> AppRoute.Expenses
    }
}
