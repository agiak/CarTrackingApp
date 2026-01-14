package com.agcoding.cartrackingapp.presentation.refillsgraph

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.RefillsTrendData
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetRefillsTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefillsGraphViewModel @Inject constructor(
    private val getRefillsTrendUseCase: GetRefillsTrendUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long? = savedStateHandle.get<String>("carId")?.toLongOrNull()

    private val _uiState = MutableStateFlow<RefillsGraphUiState>(RefillsGraphUiState.Loading)
    val uiState: StateFlow<RefillsGraphUiState> = _uiState.asStateFlow()

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
            _uiState.value = RefillsGraphUiState.Loading

            getRefillsTrendUseCase(
                carId = carId,
                period = _selectedPeriod.value
            )
                .catch { e ->
                    _uiState.value = RefillsGraphUiState.Error(
                        e.message ?: "Unknown error"
                    )
                }
                .collect { trendData ->
                    _uiState.value = if (trendData == null) {
                        RefillsGraphUiState.NoData
                    } else {
                        RefillsGraphUiState.Success(trendData)
                    }
                }
        }
    }

    fun retry() {
        loadTrendData()
    }
}

sealed class RefillsGraphUiState {
    object Loading : RefillsGraphUiState()
    object NoData : RefillsGraphUiState()
    data class Success(val trendData: RefillsTrendData) : RefillsGraphUiState()
    data class Error(val message: String) : RefillsGraphUiState()
}

