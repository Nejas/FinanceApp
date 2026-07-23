package com.example.financeapp.presentation.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.financeapp.R
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.accounts.AccountsRoute
import com.example.financeapp.presentation.analytics.AnalyticsRoute
import com.example.financeapp.presentation.common.components.base.AddButton
import com.example.financeapp.presentation.common.components.base.AppTopBar
import com.example.financeapp.presentation.common.components.base.BottomNavigationBar
import com.example.financeapp.presentation.common.components.base.DetailTopBar
import com.example.financeapp.presentation.common.network.LifecycleNetworkRefreshEffect
import com.example.financeapp.presentation.common.network.NetworkStatusBanner
import com.example.financeapp.presentation.common.network.NetworkStatusViewModel
import com.example.financeapp.presentation.expenses.ExpensesRoute
import com.example.financeapp.presentation.income.IncomeRoute
import com.example.financeapp.presentation.navigation.AppNavGraph
import com.example.financeapp.presentation.navigation.AppRoute
import com.example.financeapp.presentation.navigation.isMainRoute
import java.time.LocalDate
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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedRoute = navBackStackEntry?.destination?.route.toAppRoute()

    LifecycleNetworkRefreshEffect(
        refreshable = mainViewModel,
        isOnline = isOnline,
        refreshImmediately = false
    )

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppShellTopBar(
                mode = selectedRoute.toTopBarMode(),
                selectedDate = mainState.selectedDate,
                onAnalyticsClick = {
                    navController.navigateToRoute(AppRoute.Analytics)
                },
                onBackClick = {
                    navController.navigateBackToMain()
                },
                onSettingsClick = {}
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedRoute.isMainRoute(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AddButton(onClick = {})
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedRoute.isMainRoute(),
                enter = slideInVertically { height -> height } + fadeIn(),
                exit = slideOutVertically { height -> height } + fadeOut()
            ) {
                BottomNavigationBar(
                    selectedRoute = selectedRoute,
                    onRouteSelected = { route ->
                        navController.navigateToRoute(route)
                    }
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

            AppNavGraph(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .pointerInput(edgeGuardPx, swipeThresholdPx, selectedRoute) {
                        var dragOffset = 0f
                        var canHandleSwipe = false

                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                dragOffset = 0f
                                canHandleSwipe = selectedRoute.isMainRoute() &&
                                    offset.x > edgeGuardPx &&
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
                                    navController.navigateToRoute(nextRoute)
                                }
                                dragOffset = 0f
                                canHandleSwipe = false
                            }
                        )
                    },
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
                            navController.navigateBackToMain()
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun AppShellTopBar(
    mode: TopBarMode,
    selectedDate: LocalDate,
    onAnalyticsClick: () -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    AnimatedContent(
        targetState = mode,
        label = "AppShellTopBar"
    ) { targetMode ->
        when (targetMode) {
            TopBarMode.Main -> {
                AppTopBar(
                    modifier = Modifier.fillMaxWidth(),
                    selectedDate = selectedDate,
                    onAnalyticsClick = onAnalyticsClick,
                    onSettingsClick = onSettingsClick
                )
            }
            TopBarMode.Analytics -> {
                DetailTopBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.analytics_title),
                    onBackClick = onBackClick
                )
            }
        }
    }
}

private fun AppRoute.toTopBarMode(): TopBarMode {
    return if (isMainRoute()) {
        TopBarMode.Main
    } else {
        TopBarMode.Analytics
    }
}

private fun String?.toAppRoute(): AppRoute {
    return when (this) {
        AppRoute.Income.route -> AppRoute.Income
        AppRoute.Accounts.route -> AppRoute.Accounts
        AppRoute.Analytics.route -> AppRoute.Analytics
        else -> AppRoute.Expenses
    }
}

private enum class TopBarMode {
    Main,
    Analytics
}

private fun NavHostController.navigateBackToMain() {
    if (!popBackStack()) {
        navigateToRoute(AppRoute.Expenses)
    }
}

private fun NavHostController.navigateToRoute(route: AppRoute) {
    if (currentDestination?.route == route.route) return
    if (route.isMainRoute()) {
        navigate(route.route) {
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    } else {
        navigate(route.route) {
            launchSingleTop = true
        }
    }
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
