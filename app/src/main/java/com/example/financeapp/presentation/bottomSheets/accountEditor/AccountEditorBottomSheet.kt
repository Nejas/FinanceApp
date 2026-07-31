package com.example.financeapp.presentation.bottomSheets.accountEditor

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financeapp.R
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.presentation.bottomSheets.components.common.FinanceSingleTextInputBottomSheet
import com.example.financeapp.presentation.bottomSheets.components.common.FinanceTextInputBottomSheet
import com.example.financeapp.presentation.bottomSheets.components.currency.FinanceCurrencySelectionSheetContent
import com.example.financeapp.presentation.common.components.FinanceFieldIcon
import com.example.financeapp.presentation.common.components.base.FinanceModalBottomSheet
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType
import com.example.financeapp.presentation.common.components.base.FinanceSelectionRow
import com.example.financeapp.presentation.common.components.base.RoundFrame
import com.example.financeapp.presentation.common.components.base.TextOvalFrame
import com.example.financeapp.presentation.common.placeholders.ErrorContent
import com.example.financeapp.presentation.common.placeholders.LoadingContent
import com.example.financeapp.presentation.common.utils.symbol

@Composable
fun AccountEditorBottomSheet(
    state: AccountEditorState,
    onIntent: (AccountEditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        FinanceModalBottomSheet(
            onDismissRequest = {
                onIntent(AccountEditorIntent.DismissRequested)
            },
            modifier = modifier
        ) {
            LoadingContent()
        }
        return
    }

    state.error?.let { error ->
        FinanceModalBottomSheet(
            onDismissRequest = {
                onIntent(AccountEditorIntent.DismissRequested)
            },
            modifier = modifier
        ) {
            ErrorContent(
                error = error,
                onRetryClick = {
                    onIntent(AccountEditorIntent.RetryClicked)
                }
            )
        }
        return
    }

    when (state.activeField) {
        AccountEditorField.Name -> FinanceSingleTextInputBottomSheet(
            title = stringResource(R.string.editor_name),
            value = state.name,
            onValueChange = { name ->
                onIntent(AccountEditorIntent.NameChanged(name))
            },
            onConfirmClick = {
                onIntent(AccountEditorIntent.FieldDismissed)
            },
            onDismissRequest = {
                onIntent(AccountEditorIntent.FieldDismissed)
            }
        )
        AccountEditorField.Emoji -> FinanceSingleTextInputBottomSheet(
            title = stringResource(R.string.editor_emoji),
            value = state.emoji,
            onValueChange = { emoji ->
                onIntent(AccountEditorIntent.EmojiChanged(emoji))
            },
            onConfirmClick = {
                onIntent(AccountEditorIntent.FieldDismissed)
            },
            onDismissRequest = {
                onIntent(AccountEditorIntent.FieldDismissed)
            },
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.None
        )
        AccountEditorField.Currency -> FinanceModalBottomSheet(
            onDismissRequest = {
                onIntent(AccountEditorIntent.FieldDismissed)
            },
            modifier = modifier
        ) {
            FinanceCurrencySelectionSheetContent(
                selectedCurrency = state.selectedCurrency,
                onCurrencyClick = { currency ->
                    onIntent(AccountEditorIntent.CurrencySelected(currency))
                }
            )
        }
        null -> AccountFormBottomSheet(
            state = state,
            onIntent = onIntent,
            modifier = modifier
        )
    }
}

@Composable
private fun AccountFormBottomSheet(
    state: AccountEditorState,
    onIntent: (AccountEditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectLabel = stringResource(R.string.editor_select)

    FinanceTextInputBottomSheet(
        value = state.balance,
        suffix = state.selectedCurrency.symbol,
        onValueChange = { balance ->
            onIntent(AccountEditorIntent.BalanceChanged(balance))
        },
        onConfirmClick = {
            onIntent(AccountEditorIntent.ConfirmClicked)
        },
        onDismissRequest = {
            onIntent(AccountEditorIntent.DismissRequested)
        },
        modifier = modifier,
        title = when (state.mode) {
            is AccountEditorMode.Create -> null
            is AccountEditorMode.Edit -> stringResource(R.string.account_balance_adjustment)
        },
        supportingText = state.formMessageResId?.let { messageResId ->
            stringResource(messageResId)
        } ?: if (state.isSaving) {
            stringResource(R.string.account_saving)
        } else {
            null
        }
    ) {
        AccountEditorFieldRow(
            field = AccountEditorField.Name,
            value = state.name.ifBlank { selectLabel },
            onClick = { onIntent(AccountEditorIntent.NameClicked) }
        )
        AccountEditorFieldRow(
            field = AccountEditorField.Emoji,
            value = state.emoji.ifBlank { selectLabel },
            onClick = { onIntent(AccountEditorIntent.EmojiClicked) }
        )
        AccountEditorFieldRow(
            field = AccountEditorField.Currency,
            value = state.selectedCurrency.code,
            onClick = { onIntent(AccountEditorIntent.CurrencyClicked) },
            showDivider = false
        )
    }
}

@Composable
private fun AccountEditorFieldRow(
    field: AccountEditorField,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    FinanceSelectionRow(
        title = stringResource(field.titleResId),
        isSelected = false,
        indicatorType = FinanceSelectionIndicatorType.CheckMark,
        onClick = onClick,
        modifier = modifier,
        showDivider = showDivider,
        leadingContent = {
            RoundFrame(content = {
                FinanceFieldIcon(
                    icon = field.icon,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(LocalSizing.current.icon)
                )
            })
        },
        trailingContent = {
            TextOvalFrame(text = value, maxWidth = 190.dp)
        }
    )
}
