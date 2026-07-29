package com.example.financeapp.presentation.bottomSheets.components.account

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.financeapp.R
import com.example.financeapp.domain.model.Account
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIconFrame
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType
import com.example.financeapp.presentation.common.components.base.FinanceSelectionRow
import com.example.financeapp.presentation.common.components.base.FinanceSelectionSheetScaffold

@Composable
fun FinanceAccountSelectionSheetContent(
    accounts: List<Account>,
    selectedAccountId: Long?,
    onAccountClick: (Long?) -> Unit,
    allAccountsTitle: String? = null
) {
    FinanceSelectionSheetScaffold(
        title = stringResource(R.string.editor_account)
    ) {
        if (allAccountsTitle != null) {
            FinanceSelectionRow(
                title = allAccountsTitle,
                isSelected = selectedAccountId == null,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = { onAccountClick(null) },
                leadingContent = {
                    FinanceSelectionIconFrame(
                        content = {
                            Text(
                                text = "💳",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    )
                }
            )
        }
        accounts.forEach { account ->
            FinanceSelectionRow(
                title = account.name,
                subtitle = account.description,
                isSelected = selectedAccountId == account.id,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = { onAccountClick(account.id) },
                leadingContent = {
                    FinanceSelectionIconFrame(
                        content = {
                            Text(
                                text = account.emoji,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    )
                }
            )
        }
    }
}
