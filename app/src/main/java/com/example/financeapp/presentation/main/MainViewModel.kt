package com.example.financeapp.presentation.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class MainViewModel @Inject constructor(
    clock: Clock
) : ViewModel() {

    private val _state = MutableStateFlow(
        MainState(selectedDate = LocalDate.now(clock))
    )
    val state: StateFlow<MainState> = _state.asStateFlow()

    fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.DateSelected -> {
                _state.update { state ->
                    state.copy(selectedDate = intent.date)
                }
            }
        }
    }
}
