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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.financeapp.core.localization.AppLanguage
import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.UserSettings
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.accounts.AccountsRoute
import com.example.financeapp.presentation.analytics.AnalyticsRoute
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorBottomSheet
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorEffect
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorIntent
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorMode
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorViewModel
import com.example.financeapp.presentation.bottomSheets.components.categories.FinanceCategorySelectionSheetContent
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorBottomSheet
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorEffect
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorIntent
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorMode
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorViewModel
import com.example.financeapp.presentation.common.components.base.FinanceModalBottomSheet
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType
import com.example.financeapp.presentation.common.components.base.FinanceActionButton
import com.example.financeapp.presentation.common.components.base.AppTopBar
import com.example.financeapp.presentation.common.components.base.BottomNavigationBar
import com.example.financeapp.presentation.common.components.base.DetailTopBar
import com.example.financeapp.presentation.common.components.icons.FinancePlusIcon
import com.example.financeapp.presentation.common.network.NetworkStatusBanner
import com.example.financeapp.presentation.common.network.PendingSyncStatusBanner
import com.example.financeapp.presentation.common.network.NetworkStatusViewModel
import com.example.financeapp.presentation.common.placeholders.ScreenError
import com.example.financeapp.presentation.expenses.ExpensesRoute
import com.example.financeapp.presentation.income.IncomeRoute
import com.example.financeapp.presentation.navigation.AppNavGraph
import com.example.financeapp.presentation.navigation.AppRoute
import com.example.financeapp.presentation.navigation.isMainRoute
import com.example.financeapp.presentation.bottomSheets.settings.LanguageSettingsSheet
import com.example.financeapp.presentation.bottomSheets.settings.SettingsBottomSheet
import com.example.financeapp.presentation.bottomSheets.settings.SettingsListItem
import com.example.financeapp.presentation.bottomSheets.settings.ThemeSettingsSheet
import java.time.LocalDate
import kotlin.math.abs

@Composable
fun FinanceApp(
    userSettings: UserSettings,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
    networkStatusViewModel: NetworkStatusViewModel = hiltViewModel(),
    transactionEditorViewModel: TransactionEditorViewModel = hiltViewModel(),
    accountEditorViewModel: AccountEditorViewModel = hiltViewModel()
) {
    val spacing = LocalSpacing.current
    val density = LocalDensity.current
    val sizing = LocalSizing.current
    val edgeGuardPx = with(density) { spacing.contentSwipeEdgeGuard.toPx() }
    val swipeThresholdPx = with(density) { spacing.contentSwipeThreshold.toPx() }
    val mainState by mainViewModel.state.collectAsState()
    val transactionEditorState by transactionEditorViewModel.state.collectAsState()
    val accountEditorState by accountEditorViewModel.state.collectAsState()
    val isOnline by networkStatusViewModel.isOnline.collectAsState()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDeleteTarget by remember { mutableStateOf<DeleteTarget?>(null) }
    var failedSyncOperationsCount by remember { mutableStateOf<Int?>(null) }
    var isSettingsSheetVisible by rememberSaveable { mutableStateOf(false) }
    var activeSettingsItemKey by rememberSaveable { mutableStateOf<String?>(null) }
    val activeSettingsItem = SettingsListItem.fromSaveableKey(activeSettingsItemKey)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedRoute = navBackStackEntry?.destination?.route.toAppRoute()
    val selectedTransactionType = selectedRoute.toTransactionTypeOrNull()
    val deleteFailedMessage = stringResource(R.string.delete_failed)
    val deleteFailedNoInternetMessage = stringResource(R.string.delete_failed_no_internet)
    val accountDeleteHasTransactionsMessage = stringResource(R.string.account_delete_has_transactions)
    val transactionSavedLocallyMessage = stringResource(R.string.transaction_saved_locally)

    LaunchedEffect(
        mainViewModel,
        snackbarHostState,
        deleteFailedMessage,
        deleteFailedNoInternetMessage,
        accountDeleteHasTransactionsMessage
    ) {
        mainViewModel.effects.collect { effect ->
            when (effect) {
                is MainEffect.DeleteFailed -> {
                    val message = when (effect.error) {
                        ScreenError.NO_INTERNET -> deleteFailedNoInternetMessage
                        ScreenError.ACCOUNT_HAS_TRANSACTIONS -> accountDeleteHasTransactionsMessage
                        ScreenError.SERVER_ERROR,
                        ScreenError.TIMEOUT,
                        ScreenError.LOAD_FAILED -> deleteFailedMessage
                    }
                    snackbarHostState.showSnackbar(message)
                }
                is MainEffect.SyncFailed -> {
                    failedSyncOperationsCount = effect.count
                }
            }
        }
    }

    LaunchedEffect(transactionEditorViewModel, mainViewModel, snackbarHostState, transactionSavedLocallyMessage) {
        transactionEditorViewModel.effects.collect { effect ->
            when (effect) {
                is TransactionEditorEffect.Saved -> {
                    mainViewModel.onIntent(MainIntent.DataChanged)
                    if (effect.transactionId < 0) {
                        snackbarHostState.showSnackbar(transactionSavedLocallyMessage)
                    }
                }
                TransactionEditorEffect.Close -> Unit
            }
        }
    }

    LaunchedEffect(accountEditorViewModel, mainViewModel) {
        accountEditorViewModel.effects.collect { effect ->
            when (effect) {
                is AccountEditorEffect.Saved -> {
                    mainViewModel.onIntent(MainIntent.DataChanged)
                }
                AccountEditorEffect.Close -> Unit
            }
        }
    }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
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
                onSettingsClick = {
                    isSettingsSheetVisible = true
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedRoute != AppRoute.Analytics,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FinanceActionButton(
                    onClick = {
                        if (selectedRoute == AppRoute.Accounts) {
                            accountEditorViewModel.onIntent(
                                AccountEditorIntent.Open(AccountEditorMode.Create)
                            )
                        } else {
                            selectedTransactionType?.let { transactionType ->
                                transactionEditorViewModel.onIntent(
                                    TransactionEditorIntent.Open(
                                        TransactionEditorMode.Create(transactionType)
                                    )
                                )
                            }
                        }
                    },
                    icon = {
                        FinancePlusIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(sizing.icon)
                        )
                    }
                )
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
            if (mainState.hasPendingSync) {
                PendingSyncStatusBanner(modifier = Modifier.fillMaxWidth())
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
                        },
                        onTransactionClick = { transactionId ->
                            transactionEditorViewModel.onIntent(
                                TransactionEditorIntent.Open(
                                    TransactionEditorMode.Edit(
                                        transactionId = transactionId,
                                        transactionType = TransactionType.EXPENSE
                                    )
                                )
                            )
                        },
                        onTransactionDeleteRequest = { transactionId ->
                            pendingDeleteTarget = DeleteTarget.Transaction(transactionId)
                        }
                    )
                },
                incomeContent = { routeModifier ->
                    IncomeRoute(
                        modifier = routeModifier,
                        state = mainState.incomeState,
                        onRetry = {
                            mainViewModel.onIntent(MainIntent.Retry)
                        },
                        onTransactionClick = { transactionId ->
                            transactionEditorViewModel.onIntent(
                                TransactionEditorIntent.Open(
                                    TransactionEditorMode.Edit(
                                        transactionId = transactionId,
                                        transactionType = TransactionType.INCOME
                                    )
                                )
                            )
                        },
                        onTransactionDeleteRequest = { transactionId ->
                            pendingDeleteTarget = DeleteTarget.Transaction(transactionId)
                        }
                    )
                },
                accountsContent = { routeModifier ->
                    AccountsRoute(
                        modifier = routeModifier,
                        state = mainState.accountsState,
                        onRetry = {
                            mainViewModel.onIntent(MainIntent.Retry)
                        },
                        onAccountClick = { accountId ->
                            accountEditorViewModel.onIntent(
                                AccountEditorIntent.Open(
                                    AccountEditorMode.Edit(accountId)
                                )
                            )
                        },
                        onAccountDeleteRequest = { accountId ->
                            pendingDeleteTarget = DeleteTarget.Account(accountId)
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

    transactionEditorState.form?.let { formState ->
        TransactionEditorBottomSheet(
            state = formState,
            onIntent = transactionEditorViewModel::onIntent
        )
    }

    accountEditorState.form?.let { formState ->
        AccountEditorBottomSheet(
            state = formState,
            onIntent = accountEditorViewModel::onIntent
        )
    }

    if (isSettingsSheetVisible) {
        when (activeSettingsItem) {
            SettingsListItem.Articles -> FinanceModalBottomSheet(
                onDismissRequest = {
                    activeSettingsItemKey = null
                }
            ) {
                FinanceCategorySelectionSheetContent(
                    categories = mainState.settingsCategories(),
                    selectedCategoryIds = emptySet(),
                    indicatorType = FinanceSelectionIndicatorType.CheckMark,
                    onCategoryClick = null
                )
            }
            SettingsListItem.Language -> FinanceModalBottomSheet(
                onDismissRequest = {
                    activeSettingsItemKey = null
                }
            ) {
                LanguageSettingsSheet(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { language ->
                        onLanguageSelected(language)
                        activeSettingsItemKey = null
                    }
                )
            }
            SettingsListItem.Theme -> FinanceModalBottomSheet(
                onDismissRequest = {
                    activeSettingsItemKey = null
                }
            ) {
                ThemeSettingsSheet(
                    selectedThemeMode = userSettings.themeMode,
                    onThemeModeSelected = onThemeModeSelected
                )
            }
            null -> SettingsBottomSheet(
                onDismissRequest = {
                    isSettingsSheetVisible = false
                    activeSettingsItemKey = null
                },
                onItemClick = { item ->
                    when (item) {
                        SettingsListItem.Articles,
                        SettingsListItem.Language,
                        SettingsListItem.Theme -> {
                            activeSettingsItemKey = item.saveableKey
                        }
                        SettingsListItem.Biometrics,
                        SettingsListItem.Currency,
                        SettingsListItem.PinCode -> Unit
                    }
                }
            )
            SettingsListItem.Biometrics,
            SettingsListItem.Currency,
            SettingsListItem.PinCode -> Unit
        }
    }

    pendingDeleteTarget?.let { target ->
        DeleteConfirmationDialog(
            onConfirmClick = {
                when (target) {
                    is DeleteTarget.Transaction -> {
                        mainViewModel.onIntent(MainIntent.DeleteTransaction(target.id))
                    }
                    is DeleteTarget.Account -> {
                        mainViewModel.onIntent(MainIntent.DeleteFinancialAccount(target.id))
                    }
                }
                pendingDeleteTarget = null
            },
            onDismissRequest = {
                pendingDeleteTarget = null
            }
        )
    }

    failedSyncOperationsCount?.let { count ->
        SyncFailureDialog(
            failedOperationsCount = count,
            onRetryClick = {
                mainViewModel.onIntent(MainIntent.RetryFailedSyncOperations)
                failedSyncOperationsCount = null
            },
            onDiscardClick = {
                mainViewModel.onIntent(MainIntent.DiscardFailedSyncOperations)
                failedSyncOperationsCount = null
            },
            onDismissRequest = {
                failedSyncOperationsCount = null
            }
        )
    }
}

@Composable
private fun SyncFailureDialog(
    failedOperationsCount: Int,
    onRetryClick: () -> Unit,
    onDiscardClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.sync_failed_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = stringResource(R.string.sync_failed_message, failedOperationsCount),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onRetryClick) {
                Text(text = stringResource(R.string.retry))
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscardClick) {
                Text(
                    text = stringResource(R.string.sync_discard_changes),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.action_confirmation_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_confirmation_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
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

private fun AppRoute.toTransactionTypeOrNull(): TransactionType? {
    return when (this) {
        AppRoute.Expenses -> TransactionType.EXPENSE
        AppRoute.Income -> TransactionType.INCOME
        AppRoute.Accounts,
        AppRoute.Analytics -> null
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

private sealed interface DeleteTarget {
    data class Transaction(val id: Long) : DeleteTarget
    data class Account(val id: Long) : DeleteTarget
}

private fun MainState.settingsCategories(): List<Category> {
    return (expensesState.categoriesById.values + incomeState.categoriesById.values)
        .distinctBy { category -> category.id }
        .sortedWith(
            compareBy<Category> { category -> category.type }
                .thenBy { category -> category.name }
        )
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
