package com.example.financeapp.presentation.navigation

sealed class AppRoute(val route: String) {
    data object Expenses : AppRoute("expenses")
    data object Income : AppRoute("income")
    data object Accounts : AppRoute("accounts")
    data object Analytics : AppRoute("analytics")
}

fun AppRoute.isMainRoute(): Boolean {
    return this == AppRoute.Expenses || this == AppRoute.Income || this == AppRoute.Accounts
}
