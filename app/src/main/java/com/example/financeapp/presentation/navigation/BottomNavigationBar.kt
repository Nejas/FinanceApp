package com.example.financeapp.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.icons.FinancePersonIcon
import com.example.financeapp.presentation.common.icons.FinanceReceiptIcon
import com.example.financeapp.presentation.common.icons.FinanceTrendingUpIcon
import com.lottiefiles.dotlottie.core.util.handleInternalEvent

@Composable
fun BottomNavigationBar(
    selectedRoute: AppRoute,
    onRouteSelected: (AppRoute) -> Unit
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = spacing.hairline,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        NavigationBar(
            modifier = Modifier.defaultMinSize(minHeight =  sizing.navigationBarHeight),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = item.route == selectedRoute
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onRouteSelected(item.route) },
                    icon = {
                        val iconColor = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                        val iconModifier = Modifier.size(sizing.icon)
                        when (item.icon) {
                            BottomNavIcon.EXPENSES -> FinanceReceiptIcon(
                                color = iconColor,
                                modifier = iconModifier
                            )
                            BottomNavIcon.INCOME -> FinanceTrendingUpIcon(
                                color = iconColor,
                                modifier = iconModifier
                            )
                            BottomNavIcon.ACCOUNTS -> FinancePersonIcon(
                                color = iconColor,
                                modifier = iconModifier
                            )
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(item.labelResId),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.secondary,
                        unselectedIconColor = MaterialTheme.colorScheme.outline,
                        unselectedTextColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}
