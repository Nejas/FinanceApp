package com.example.financeapp.presentation.navigation

import androidx.annotation.StringRes
import com.example.financeapp.R

data class BottomNavItem(
    val route: AppRoute,
    val icon: BottomNavIcon,
    @StringRes val labelResId: Int
)

enum class BottomNavIcon {
    EXPENSES,
    INCOME,
    ACCOUNTS
}

val bottomNavItems = listOf(
    BottomNavItem(
        route = AppRoute.Expenses,
        icon = BottomNavIcon.EXPENSES,
        labelResId = R.string.nav_expenses
    ),
    BottomNavItem(
        route = AppRoute.Income,
        icon = BottomNavIcon.INCOME,
        labelResId = R.string.nav_income
    ),
    BottomNavItem(
        route = AppRoute.Accounts,
        icon = BottomNavIcon.ACCOUNTS,
        labelResId = R.string.nav_accounts
    )
)
