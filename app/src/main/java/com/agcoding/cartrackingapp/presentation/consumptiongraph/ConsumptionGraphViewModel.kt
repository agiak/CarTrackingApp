package com.agcoding.cartrackingapp.presentation.consumptiongraph

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.ConsumptionTrendData
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetConsumptionTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsumptionGraphViewModel @Inject constructor(
    private val getConsumptionTrendUseCase: GetConsumptionTrendUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long? = savedStateHandle.get<String>("carId")?.toLongOrNull()

    private val _uiState = MutableStateFlow<ConsumptionGraphUiState>(ConsumptionGraphUiState.Loading)
    val uiState: StateFlow<ConsumptionGraphUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(TrendPeriod.ALL_TIME)
    val selectedPeriod: StateFlow<TrendPeriod> = _selectedPeriod.asStateFlow()

    private val _showPeriodSelector = MutableStateFlow(false)
    val showPeriodSelector: StateFlow<Boolean> = _showPeriodSelector.asStateFlow()

    init {
        loadTrendData()
    }

    fun selectPeriod(period: TrendPeriod) {
        _selectedPeriod.value = period
        _showPeriodSelector.value = false
        loadTrendData()
    }

    fun showPeriodSelector() {
        _showPeriodSelector.value = true
    }

    fun hidePeriodSelector() {
        _showPeriodSelector.value = false
    }

    private fun loadTrendData() {
        viewModelScope.launch {
            _uiState.value = ConsumptionGraphUiState.Loading

            getConsumptionTrendUseCase(
                carId = carId,
                period = _selectedPeriod.value
            )
                .catch { e ->
                    _uiState.value = ConsumptionGraphUiState.Error(
                        e.message ?: "Failed to load consumption trend"
                    )
                }
                .collect { trendData ->
                    _uiState.value = if (trendData == null) {
                        ConsumptionGraphUiState.NoData
                    } else {
                        ConsumptionGraphUiState.Success(trendData)
                    }
                }
        }
    }

    fun retry() {
        loadTrendData()
    }
}

sealed class ConsumptionGraphUiState {
    object Loading : ConsumptionGraphUiState()
    object NoData : ConsumptionGraphUiState()
    data class Success(val trendData: ConsumptionTrendData) : ConsumptionGraphUiState()
    data class Error(val message: String) : ConsumptionGraphUiState()
}

