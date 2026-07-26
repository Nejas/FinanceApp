package com.example.financeapp.presentation.bottomSheets.components.account

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.domain.model.FinancialAccount

@Composable
internal fun AnalyticsAccountSheet(
    accounts: List<FinancialAccount>,
    selectedAccountId: Long?,
    onAccountClick: (Long?) -> Unit
) {
    FinanceAccountSelectionSheetContent(
        accounts = accounts,
        selectedAccountId = selectedAccountId,
        onAccountClick = onAccountClick,
        allAccountsTitle = stringResource(R.string.analytics_filter_all_accounts)
    )
}
