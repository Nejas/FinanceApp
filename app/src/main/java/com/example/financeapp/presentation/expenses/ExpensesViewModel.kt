package com.example.financeapp.presentation.expenses

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.usecase.GetCategoriesUseCase
import com.example.financeapp.domain.usecase.GetTransactionsForDateUseCase
import com.example.financeapp.presentation.common.mvi.ScreenError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
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
class ExpensesViewModel @Inject constructor(
    private val getTransactionsForDate: GetTransactionsForDateUseCase,
    private val getCategories: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExpensesState())
    val state: StateFlow<ExpensesState> = _state.asStateFlow()

    private val effectChannel = Channel<ExpensesEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var loadJob: Job? = null
    private var lastSelectedDate: LocalDate? = null

    fun onIntent(intent: ExpensesIntent) {
        when (intent) {
            ExpensesIntent.Load,
            ExpensesIntent.Retry -> {
                lastSelectedDate?.let(::loadExpenses)
            }

            ExpensesIntent.AnalyticsClicked -> sendEffect(ExpensesEffect.OpenAnalytics)
            ExpensesIntent.SettingsClicked -> sendEffect(ExpensesEffect.OpenSettings)

            is ExpensesIntent.DateSelected -> {
                lastSelectedDate = intent.date
                loadExpenses(intent.date)
            }

            is ExpensesIntent.TransactionClicked -> {
                sendEffect(ExpensesEffect.OpenTransaction(intent.transactionId))
            }
        }
    }

    private fun loadExpenses(selectedDate: LocalDate) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            Log.d(TAG, "Loading expenses for date=$selectedDate")
            _state.update { state ->
                state.copy(isLoading = true, error = null)
            }

            val categoriesResult = getCategories(TransactionType.EXPENSE)
            val transactionsResult = getTransactionsForDate(
                date = selectedDate,
                type = TransactionType.EXPENSE,
            )

            transactionsResult.fold(
                onSuccess = { overview ->
                    val categoriesById = categoriesResult
                        .getOrDefault(emptyList())
                        .associateBy { category -> category.id }
                    Log.d(TAG, "Loaded ${overview.transactions.size} expenses")
                    _state.update { state ->
                        state.copy(
                            transactions = overview.transactions,
                            categoriesById = categoriesById,
                            total = overview.total,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to load expenses", error)
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

    private fun sendEffect(effect: ExpensesEffect) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    private companion object {
        const val TAG = "ExpensesViewModel"
    }
}
