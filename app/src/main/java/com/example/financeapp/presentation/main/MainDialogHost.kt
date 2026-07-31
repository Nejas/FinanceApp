package com.example.financeapp.presentation.main

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R

internal sealed interface DeleteTarget {
    data class Transaction(val id: Long) : DeleteTarget
    data class Account(val id: Long) : DeleteTarget
}

@Composable
internal fun MainDialogHost(
    deleteTarget: DeleteTarget?,
    failedSyncOperationsCount: Int?,
    onDeleteConfirmed: (DeleteTarget) -> Unit,
    onDeleteDismissed: () -> Unit,
    onRetryFailedSync: () -> Unit,
    onDiscardFailedSync: () -> Unit,
    onSyncDialogDismissed: () -> Unit
) {
    deleteTarget?.let { target ->
        DeleteConfirmationDialog(
            onConfirmClick = { onDeleteConfirmed(target) },
            onDismissRequest = onDeleteDismissed
        )
    }
    failedSyncOperationsCount?.let { count ->
        SyncFailureDialog(
            failedOperationsCount = count,
            onRetryClick = onRetryFailedSync,
            onDiscardClick = onDiscardFailedSync,
            onDismissRequest = onSyncDialogDismissed
        )
    }
}

@Composable
private fun SyncFailureDialog(
    failedOperationsCount: Int,
    onRetryClick: () -> Unit,
    onDiscardClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.sync_failed_title), style = MaterialTheme.typography.titleMedium) },
        text = { Text(stringResource(R.string.sync_failed_message, failedOperationsCount), style = MaterialTheme.typography.bodyMedium) },
        confirmButton = { TextButton(onClick = onRetryClick) { Text(stringResource(R.string.retry)) } },
        dismissButton = {
            TextButton(onClick = onDiscardClick) {
                Text(stringResource(R.string.sync_discard_changes), color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.action_confirmation_title), style = MaterialTheme.typography.titleMedium) },
        text = { Text(stringResource(R.string.delete_confirmation_message), style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.action_cancel)) } }
    )
}
