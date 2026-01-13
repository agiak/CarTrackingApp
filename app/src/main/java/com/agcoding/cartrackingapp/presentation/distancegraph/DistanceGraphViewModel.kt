package com.agcoding.cartrackingapp.presentation.distancegraph

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.DistanceTrendData
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetDistanceTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DistanceGraphViewModel @Inject constructor(
    private val getDistanceTrendUseCase: GetDistanceTrendUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long? = savedStateHandle.get<String>("carId")?.toLongOrNull()

    private val _uiState = MutableStateFlow<DistanceGraphUiState>(DistanceGraphUiState.Loading)
    val uiState: StateFlow<DistanceGraphUiState> = _uiState.asStateFlow()

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
            _uiState.value = DistanceGraphUiState.Loading

            getDistanceTrendUseCase(
                carId = carId,
                period = _selectedPeriod.value
            )
                .catch { e ->
                    _uiState.value = DistanceGraphUiState.Error(
                        e.message ?: ""
                    )
                }
                .collect { trendData ->
                    _uiState.value = if (trendData == null) {
                        DistanceGraphUiState.NoData
                    } else {
                        DistanceGraphUiState.Success(trendData)
                    }
                }
        }
    }

    fun retry() {
        loadTrendData()
    }
}

sealed class DistanceGraphUiState {
    object Loading : DistanceGraphUiState()
    object NoData : DistanceGraphUiState()
    data class Success(val trendData: DistanceTrendData) : DistanceGraphUiState()
    data class Error(val message: String) : DistanceGraphUiState()
}
