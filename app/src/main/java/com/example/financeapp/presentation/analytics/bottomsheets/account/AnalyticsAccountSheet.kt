package com.example.financeapp.presentation.analytics.bottomsheets.account

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.financeapp.R
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIconFrame
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType
import com.example.financeapp.presentation.common.components.base.FinanceSelectionRow
import com.example.financeapp.presentation.common.components.base.FinanceSelectionSheetScaffold
import com.example.financeapp.presentation.common.components.icons.FinanceAccountCardIcon

@Composable
internal fun AnalyticsAccountSheet(
    accounts: List<FinancialAccount>,
    selectedAccountId: Long?,
    onAccountClick: (Long?) -> Unit
) {
    FinanceSelectionSheetScaffold(title = stringResource(R.string.analytics_filter_account)) {
        FinanceSelectionRow(
            title = stringResource(R.string.analytics_filter_all_accounts),
            isSelected = selectedAccountId == null,
            indicatorType = FinanceSelectionIndicatorType.CheckMark,
            onClick = { onAccountClick(null) },
            leadingContent = {
                FinanceSelectionIconFrame(content = {
                    Text("💳",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,)
                })
            }
        )
        accounts.forEach { account ->
            FinanceSelectionRow(
                title = account.name,
                subtitle = account.description,
                isSelected = selectedAccountId == account.id,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = { onAccountClick(account.id) },
                leadingContent = {
                    FinanceSelectionIconFrame(content = {
                        Text(
                            text = account.emoji,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    })
                }
            )
        }
    }
}
