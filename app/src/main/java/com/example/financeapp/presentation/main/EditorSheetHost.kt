package com.example.financeapp.presentation.main

import androidx.compose.runtime.Composable
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorBottomSheet
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorHostState
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorIntent
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorBottomSheet
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorHostState
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorIntent

@Composable
internal fun EditorSheetHost(
    transactionState: TransactionEditorHostState,
    accountState: AccountEditorHostState,
    onTransactionIntent: (TransactionEditorIntent) -> Unit,
    onAccountIntent: (AccountEditorIntent) -> Unit
) {
    transactionState.form?.let { state ->
        TransactionEditorBottomSheet(state = state, onIntent = onTransactionIntent)
    }
    accountState.form?.let { state ->
        AccountEditorBottomSheet(state = state, onIntent = onAccountIntent)
    }
}
