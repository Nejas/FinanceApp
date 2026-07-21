package com.example.financeapp.presentation.navigation

import androidx.annotation.StringRes
import com.example.financeapp.R

enum class BottomNavIcon {
    EXPENSES,
    INCOME,
    ACCOUNTS
}

data class BottomNavItem(
    val route: AppRoute,
    val icon: BottomNavIcon,
    @StringRes val labelResId: Int
)
