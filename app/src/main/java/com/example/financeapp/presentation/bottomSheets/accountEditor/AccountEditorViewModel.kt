package com.example.financeapp.presentation.bottomSheets.accountEditor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.R
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.AccountDraft
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.usecase.AccountUseCases
import com.example.financeapp.presentation.common.placeholders.toScreenError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountEditorViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val reducer: AccountEditorReducer,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(AccountEditorHostState())
    val state: StateFlow<AccountEditorHostState> = _state.asStateFlow()

    private val effectChannel = Channel<AccountEditorEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var loadJob: Job? = null
    private var saveJob: Job? = null

    fun onIntent(intent: AccountEditorIntent) {
        when (intent) {
            is AccountEditorIntent.Open -> open(intent.mode)
            AccountEditorIntent.ConfirmClicked -> save()
            AccountEditorIntent.DismissRequested -> dismiss()
            AccountEditorIntent.RetryClicked -> retry()
            else -> reduce(intent)
        }
    }

    private fun open(mode: AccountEditorMode) {
        loadJob?.cancel()
        saveJob?.cancel()

        _state.value = AccountEditorHostState(
            form = AccountEditorState(
                mode = mode,
                isLoading = mode is AccountEditorMode.Edit
            )
        )

        if (mode is AccountEditorMode.Edit) {
            loadJob = viewModelScope.launch {
                load(mode)
            }
        }
    }

    private suspend fun load(mode: AccountEditorMode.Edit) {
        val result = accountUseCases.getAccount(mode.accountId)

        result.fold(
            onSuccess = { account ->
                updateCurrentForm(mode) { form ->
                    form.withAccount(account)
                }
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to load account editor", error)
                updateCurrentForm(mode) { form ->
                    form.copy(
                        isLoading = false,
                        error = error.toScreenError(networkMonitor.isOnline.value)
                    )
                }
            }
        )
    }

    private fun AccountEditorState.withAccount(account: Account): AccountEditorState {
        return copy(
            name = account.name,
            emoji = account.emoji,
            balance = account.balance.amount
                .setScale(0, RoundingMode.DOWN)
                .toPlainString(),
            selectedCurrency = account.balance.currency,
            isLoading = false,
            error = null
        )
    }

    private fun reduce(intent: AccountEditorIntent) {
        _state.update { hostState ->
            val form = hostState.form ?: return@update hostState
            hostState.copy(form = reducer.reduce(form, intent))
        }
    }

    private fun save() {
        val form = _state.value.form ?: return
        if (form.isLoading || form.isSaving) return

        val validationMessage = reducer.validationMessage(form)
        if (validationMessage != null) {
            _state.update { hostState ->
                hostState.copy(
                    form = hostState.form?.copy(formMessageResId = validationMessage)
                )
            }
            return
        }

        val payload = AccountDraft(
            name = form.name.trim(),
            emoji = form.emoji.trim().takeIf { emoji -> emoji.isNotBlank() },
            balance = Money(
                amount = form.balance.toAccountBalanceAmount(),
                currency = form.selectedCurrency
            )
        )

        _state.update { hostState ->
            hostState.copy(
                form = hostState.form?.copy(
                    isSaving = true,
                    formMessageResId = null
                )
            )
        }

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val result = when (val mode = form.mode) {
                AccountEditorMode.Create -> accountUseCases.create(payload)
                is AccountEditorMode.Edit -> accountUseCases.update(
                    id = mode.accountId,
                    draft = payload
                )
            }

            result.fold(
                onSuccess = { account ->
                    _state.value = AccountEditorHostState()
                    effectChannel.send(AccountEditorEffect.Saved(account.id))
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to save account", error)
                    updateCurrentForm(form.mode) { currentForm ->
                        currentForm.copy(
                            isSaving = false,
                            formMessageResId = R.string.account_save_failed
                        )
                    }
                }
            )
        }
    }

    private fun retry() {
        _state.value.form?.mode?.let(::open)
    }

    private fun dismiss() {
        val form = _state.value.form ?: return
        if (form.isSaving) return

        loadJob?.cancel()
        _state.value = AccountEditorHostState()
        effectChannel.trySend(AccountEditorEffect.Close)
    }

    private inline fun updateCurrentForm(
        mode: AccountEditorMode,
        transform: (AccountEditorState) -> AccountEditorState
    ) {
        _state.update { hostState ->
            val form = hostState.form
            if (form == null || form.mode != mode) {
                hostState
            } else {
                hostState.copy(form = transform(form))
            }
        }
    }

    private companion object {
        const val TAG = "AccountEditorVM"
    }
}

private fun String.toAccountBalanceAmount(): BigDecimal {
    return if (isBlank()) {
        BigDecimal.ZERO
    } else {
        toBigDecimal()
    }
}
