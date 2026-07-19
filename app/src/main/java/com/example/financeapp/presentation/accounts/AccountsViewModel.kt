package com.example.financeapp.presentation.accounts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.domain.usecase.GetFinancialAccountsUseCase
import com.example.financeapp.presentation.common.mvi.ScreenError
import dagger.hilt.android.lifecycle.HiltViewModel
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
class AccountsViewModel @Inject constructor(
    private val getFinancialAccounts: GetFinancialAccountsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AccountsState())
    val state: StateFlow<AccountsState> = _state.asStateFlow()

    private val effectChannel = Channel<AccountsEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        loadAccounts()
    }

    fun onIntent(intent: AccountsIntent) {
        when (intent) {
            AccountsIntent.Load,
            AccountsIntent.Retry -> loadAccounts()

            AccountsIntent.AnalyticsClicked -> sendEffect(AccountsEffect.OpenAnalytics)
            AccountsIntent.SettingsClicked -> sendEffect(AccountsEffect.OpenSettings)

            is AccountsIntent.AccountClicked -> {
                sendEffect(AccountsEffect.OpenAccount(intent.accountId))
            }
        }
    }

    private fun loadAccounts() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            Log.d(TAG, "Loading financial accounts")
            _state.update { state ->
                state.copy(isLoading = true, error = null)
            }

            getFinancialAccounts().fold(
                onSuccess = { overview ->
                    Log.d(TAG, "Loaded ${overview.accounts.size} financial accounts")
                    _state.update { state ->
                        state.copy(
                            accounts = overview.accounts,
                            totalBalance = overview.totalBalance,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to load financial accounts", error)
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            error = ScreenError.LOAD_FAILED
                        )
                    }
                }
            )
        }
    }

    private fun sendEffect(effect: AccountsEffect) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    private companion object {
        const val TAG = "AccountsViewModel"
    }
}
