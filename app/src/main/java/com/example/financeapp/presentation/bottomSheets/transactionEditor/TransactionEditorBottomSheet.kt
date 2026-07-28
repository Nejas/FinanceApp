package com.example.financeapp.presentation.bottomSheets.transactionEditor

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.financeapp.R
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.presentation.bottomSheets.components.common.FinanceSingleTextInputBottomSheet
import com.example.financeapp.presentation.bottomSheets.components.common.FinanceTextInputBottomSheet
import com.example.financeapp.presentation.bottomSheets.components.account.FinanceAccountSelectionSheetContent
import com.example.financeapp.presentation.bottomSheets.components.categories.FinanceCategorySelectionSheetContent
import com.example.financeapp.presentation.common.components.FinanceFieldIcon
import com.example.financeapp.presentation.common.components.base.FinanceModalBottomSheet
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType
import com.example.financeapp.presentation.common.components.base.FinanceSelectionRow
import com.example.financeapp.presentation.common.components.base.RoundFrame
import com.example.financeapp.presentation.common.components.base.TextOvalFrame
import com.example.financeapp.presentation.common.model.FinanceFieldType
import com.example.financeapp.presentation.common.placeholders.ErrorContent
import com.example.financeapp.presentation.common.placeholders.LoadingContent
import com.example.financeapp.presentation.common.utils.formatDayMonth
import com.example.financeapp.presentation.common.utils.formatHourMinute
import com.example.financeapp.presentation.common.utils.symbol
import com.example.financeapp.presentation.bottomSheets.components.period.TransactionDateSelectionScreen
import com.example.financeapp.presentation.bottomSheets.components.period.TransactionTimeSelectionSheetContent

@Composable
fun TransactionEditorBottomSheet(
    state: TransactionEditorState,
    onIntent: (TransactionEditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        FinanceModalBottomSheet(
            onDismissRequest = {
                onIntent(TransactionEditorIntent.DismissRequested)
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
                onIntent(TransactionEditorIntent.DismissRequested)
            },
            modifier = modifier
        ) {
            ErrorContent(
                error = error,
                onRetryClick = {
                    onIntent(TransactionEditorIntent.RetryClicked)
                }
            )
        }
        return
    }

    when (state.activeField) {
        FinanceFieldType.Category -> FinanceModalBottomSheet(
            onDismissRequest = {
                onIntent(TransactionEditorIntent.FieldDismissed)
            },
            modifier = modifier
        ) {
            FinanceCategorySelectionSheetContent(
                categories = state.availableCategories.filter { category ->
                    category.type == state.transactionType
                },
                selectedCategoryIds = setOfNotNull(state.selectedCategory?.id),
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onCategoryClick = { categoryId ->
                    onIntent(TransactionEditorIntent.CategorySelected(categoryId))
                }
            )
        }

        FinanceFieldType.Account -> FinanceModalBottomSheet(
            onDismissRequest = {
                onIntent(TransactionEditorIntent.FieldDismissed)
            },
            modifier = modifier
        ) {
            FinanceAccountSelectionSheetContent(
                accounts = state.availableAccounts,
                selectedAccountId = state.effectiveAccount?.id,
                onAccountClick = { accountId ->
                    accountId?.let { selectedAccountId ->
                        onIntent(TransactionEditorIntent.AccountSelected(selectedAccountId))
                    }
                }
            )
        }

        FinanceFieldType.Date -> TransactionDateSelectionScreen(
            selectedDate = state.date,
            onDateSelected = { date ->
                onIntent(TransactionEditorIntent.DateChanged(date))
            },
            onDismissRequest = {
                onIntent(TransactionEditorIntent.FieldDismissed)
            }
        )

        FinanceFieldType.Time -> FinanceModalBottomSheet(
            onDismissRequest = {
                onIntent(TransactionEditorIntent.FieldDismissed)
            },
            modifier = modifier,
            showHandle = false
        ) {
            TransactionTimeSelectionSheetContent(
                selectedTime = state.time,
                onTimeSelected = { time ->
                    onIntent(TransactionEditorIntent.TimeChanged(time))
                },
                onDismissRequest = {
                    onIntent(TransactionEditorIntent.FieldDismissed)
                }
            )
        }

        FinanceFieldType.Description -> FinanceSingleTextInputBottomSheet(
            title = stringResource(R.string.editor_description),
            value = state.comment.orEmpty(),
            onValueChange = { comment ->
                onIntent(TransactionEditorIntent.CommentChanged(comment))
            },
            onConfirmClick = {
                onIntent(TransactionEditorIntent.FieldDismissed)
            },
            onDismissRequest = {
                onIntent(TransactionEditorIntent.FieldDismissed)
            },
            capitalization = KeyboardCapitalization.Sentences
        )

        FinanceFieldType.TransactionType,
        FinanceFieldType.Currency,
        FinanceFieldType.Period,
        FinanceFieldType.Name,
        FinanceFieldType.Emoji,
        null -> TransactionFormBottomSheet(
            state = state,
            onIntent = onIntent,
            modifier = modifier
        )
    }
}

@Composable
private fun TransactionFormBottomSheet(
    state: TransactionEditorState,
    onIntent: (TransactionEditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectLabel = stringResource(R.string.editor_select)

    FinanceTextInputBottomSheet(
        value = state.amount,
        suffix = state.currency?.symbol.orEmpty(),
        onValueChange = { amount ->
            onIntent(TransactionEditorIntent.AmountChanged(amount))
        },
        onConfirmClick = {
            onIntent(TransactionEditorIntent.ConfirmClicked)
        },
        onDismissRequest = {
            onIntent(TransactionEditorIntent.DismissRequested)
        },
        modifier = modifier,
        title = when (state.mode) {
            is TransactionEditorMode.Create -> null
            is TransactionEditorMode.Edit -> stringResource(
                R.string.transaction_editor_adjustment_title
            )
        },
        supportingText = state.formMessageResId?.let { messageResId ->
            stringResource(messageResId)
        } ?: if (state.isSaving) {
            stringResource(R.string.transaction_saving)
        } else {
            null
        }
    ) {
        TransactionEditorFieldTypes.forEachIndexed { index, fieldType ->
            FinanceSelectionRow(
                title = stringResource(fieldType.titleResId),
                isSelected = false,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = {
                    onIntent(TransactionEditorIntent.FieldClicked(fieldType))
                },
                showDivider = true,
                leadingContent = {
                    RoundFrame(
                        content = {
                            FinanceFieldIcon(
                                type = fieldType,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(LocalSizing.current.icon)
                            )
                        },
                        backgroundColor = MaterialTheme.colorScheme.background
                    )
                },
                trailingContent = {
                    TextOvalFrame(
                        text = state.resolveFieldValue(
                            type = fieldType,
                            selectLabel = selectLabel
                        ),
                        maxWidth = 190.dp
                    )
                }
            )
        }
    }
}

private fun TransactionEditorState.resolveFieldValue(
    type: FinanceFieldType,
    selectLabel: String
): String {
    return when (type) {
        FinanceFieldType.Category -> selectedCategory?.name ?: selectLabel
        FinanceFieldType.Date -> date.formatDayMonth()
        FinanceFieldType.Time -> time.formatHourMinute()
        FinanceFieldType.Account -> effectiveAccount?.name ?: selectLabel
        FinanceFieldType.Description -> comment?.takeIf { it.isNotBlank() } ?: selectLabel
        FinanceFieldType.TransactionType,
        FinanceFieldType.Currency,
        FinanceFieldType.Period,
        FinanceFieldType.Name,
        FinanceFieldType.Emoji -> selectLabel
    }
}
