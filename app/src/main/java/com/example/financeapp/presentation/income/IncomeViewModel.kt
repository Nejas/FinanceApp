package com.example.financeapp.presentation.income

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
class IncomeViewModel @Inject constructor(
    private val getTransactionsForDate: GetTransactionsForDateUseCase,
    private val getCategories: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(IncomeState())
    val state: StateFlow<IncomeState> = _state.asStateFlow()

    private val effectChannel = Channel<IncomeEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var loadJob: Job? = null
    private var lastSelectedDate: LocalDate? = null

    fun onIntent(intent: IncomeIntent) {
        when (intent) {
            IncomeIntent.Load,
            IncomeIntent.Retry -> {
                lastSelectedDate?.let(::loadIncome)
            }

            IncomeIntent.AnalyticsClicked -> sendEffect(IncomeEffect.OpenAnalytics)
            IncomeIntent.SettingsClicked -> sendEffect(IncomeEffect.OpenSettings)

            is IncomeIntent.DateSelected -> {
                lastSelectedDate = intent.date
                loadIncome(intent.date)
            }

            is IncomeIntent.TransactionClicked -> {
                sendEffect(IncomeEffect.OpenTransaction(intent.transactionId))
            }
        }
    }

    private fun loadIncome(selectedDate: LocalDate) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            Log.d(TAG, "Loading income for date=$selectedDate")
            _state.update { state ->
                state.copy(isLoading = true, error = null)
            }

            val categoriesResult = getCategories(TransactionType.INCOME)
            val transactionsResult = getTransactionsForDate(
                date = selectedDate,
                type = TransactionType.INCOME,
            )

            transactionsResult.fold(
                onSuccess = { overview ->
                    val categoriesById = categoriesResult
                        .getOrDefault(emptyList())
                        .associateBy { category -> category.id }
                    Log.d(TAG, "Loaded ${overview.transactions.size} income transactions")
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
                    Log.e(TAG, "Failed to load income", error)
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

    private fun sendEffect(effect: IncomeEffect) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    private companion object {
        const val TAG = "IncomeViewModel"
    }
}
