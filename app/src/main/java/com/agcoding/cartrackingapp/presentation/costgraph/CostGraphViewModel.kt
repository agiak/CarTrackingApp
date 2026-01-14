package com.agcoding.cartrackingapp.presentation.costgraph

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.CostTrendData
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetCostTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CostGraphViewModel @Inject constructor(
    private val getCostTrendUseCase: GetCostTrendUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long? = savedStateHandle.get<String>("carId")?.toLongOrNull()

    private val _uiState = MutableStateFlow<CostGraphUiState>(CostGraphUiState.Loading)
    val uiState: StateFlow<CostGraphUiState> = _uiState.asStateFlow()

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
            _uiState.value = CostGraphUiState.Loading

            getCostTrendUseCase(
                carId = carId,
                period = _selectedPeriod.value
            )
                .catch { e ->
                    _uiState.value = CostGraphUiState.Error(
                        e.message ?: "Unknown error"
                    )
                }
                .collect { trendData ->
                    _uiState.value = if (trendData == null) {
                        CostGraphUiState.NoData
                    } else {
                        CostGraphUiState.Success(trendData)
                    }
                }
        }
    }

    fun retry() {
        loadTrendData()
    }
}

sealed class CostGraphUiState {
    object Loading : CostGraphUiState()
    object NoData : CostGraphUiState()
    data class Success(val trendData: CostTrendData) : CostGraphUiState()
    data class Error(val message: String) : CostGraphUiState()
}

