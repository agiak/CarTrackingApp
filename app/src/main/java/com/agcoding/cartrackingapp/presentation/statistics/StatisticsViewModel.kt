package com.agcoding.cartrackingapp.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.GlobalStatistics
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetGlobalStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getGlobalStatisticsUseCase: GetGlobalStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            getGlobalStatisticsUseCase()
                .catch { e ->
                    _uiState.value = StatisticsUiState.Error(e.message ?: "Unknown error")
                }
                .collect { statistics ->
                    _uiState.value = StatisticsUiState.Success(statistics)
                }
        }
    }
}

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val statistics: GlobalStatistics) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

