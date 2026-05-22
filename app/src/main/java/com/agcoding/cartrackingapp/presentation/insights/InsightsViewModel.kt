package com.agcoding.cartrackingapp.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.usecase.insights.GetAllAnomaliesUseCase
import com.agcoding.cartrackingapp.shared.domain.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for the Insights screen.
 *
 * Responsibilities:
 * - Load transaction data from repositories
 * - Execute anomaly detection use cases on background thread
 * - Expose UI state via StateFlow
 * - Handle loading and empty states
 * - Reactively recompute anomalies when transactions change
 * - Emit navigation events for anomaly card taps
 */
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getAllAnomaliesUseCase: GetAllAnomaliesUseCase,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val addRefillsToTripUseCase: com.agcoding.cartrackingapp.domain.usecase.trip.AddRefillsToTripUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InsightsUiState>(InsightsUiState.Loading)
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow<AnomalyType?>(null)
    val selectedFilter: StateFlow<AnomalyType?> = _selectedFilter.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private var _allAnomalies: List<Anomaly> = emptyList()
    private var _currentCarId: Long? = null

    init {
        observeTransactionChanges()
        loadAnomalies()
    }

    /**
     * Observes transaction changes and automatically recomputes anomalies.
     * Uses combine to react to changes in both refills and expenses.
     */
    private fun observeTransactionChanges() {
        viewModelScope.launch {
            combine(
                refillRepository.getAllRefills(),
                expenseRepository.getAllExpenses()
            ) { refills, expenses ->
                // Trigger recomputation when either changes
                refills to expenses
            }.collect {
                // Only recompute if we're not in initial loading state
                if (_uiState.value !is InsightsUiState.Loading) {
                    recomputeAnomalies(_currentCarId)
                }
            }
        }
    }

    /**
     * Load anomalies for all cars or specific car.
     * Computation runs on Dispatchers.Default to avoid blocking main thread.
     */
    fun loadAnomalies(carId: Long? = null) {
        _currentCarId = carId
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _uiState.value = InsightsUiState.Loading

                // Execute anomaly detection (CPU-intensive operation)
                val anomalies = getAllAnomaliesUseCase(carId)
                _allAnomalies = anomalies

                // Update UI state
                _uiState.value = if (anomalies.isEmpty()) {
                    InsightsUiState.Empty
                } else {
                    InsightsUiState.Success(applyFilter(anomalies))
                }
            } catch (e: Exception) {
                _uiState.value = InsightsUiState.Error(
                    e.message ?: "Failed to load insights"
                )
            }
        }
    }

    /**
     * Recompute anomalies in background without showing loading state.
     * Used when transactions change to automatically update insights.
     */
    private suspend fun recomputeAnomalies(carId: Long?) {
        withContext(Dispatchers.Default) {
            try {
                // Execute anomaly detection (CPU-intensive operation)
                val anomalies = getAllAnomaliesUseCase(carId)
                _allAnomalies = anomalies

                // Update UI state
                _uiState.value = if (anomalies.isEmpty()) {
                    InsightsUiState.Empty
                } else {
                    InsightsUiState.Success(applyFilter(anomalies))
                }
            } catch (e: Exception) {
                _uiState.value = InsightsUiState.Error(
                    e.message ?: "Failed to load insights"
                )
            }
        }
    }

    /**
     * Handle anomaly card click.
     * Emits navigation event if anomaly has related transaction.
     */
    fun onAnomalyClick(anomaly: Anomaly) {
        viewModelScope.launch {
            val transactionId = anomaly.relatedTransactionId ?: return@launch

            // Determine transaction type based on anomaly type
            val event = when (anomaly.type) {
                AnomalyType.FUEL_PRICE_SPIKE,
                AnomalyType.CONSUMPTION_SPIKE -> {
                    NavigationEvent.NavigateToRefillDetails(transactionId)
                }
                AnomalyType.MAINTENANCE_OUTLIER -> {
                    NavigationEvent.NavigateToExpenseDetails(transactionId)
                }
                AnomalyType.MISSING_TRIP_REFILL -> {
                    // For missing trip refills, navigate to refill details
                    NavigationEvent.NavigateToRefillDetails(transactionId)
                }
                // Monthly and cost/km anomalies may not have a specific transaction
                AnomalyType.MONTHLY_SPENDING_INCREASE,
                AnomalyType.COST_PER_KM_DEVIATION -> {
                    // Show info dialog instead
                    NavigationEvent.ShowAggregateInfo(anomaly.description)
                }
            }

            _navigationEvent.emit(event)
        }
    }

    /**
     * Filter anomalies by type.
     */
    fun filterByType(type: AnomalyType?) {
        _selectedFilter.value = type

        if (_allAnomalies.isEmpty()) return

        val filtered = applyFilter(_allAnomalies)
        _uiState.value = if (filtered.isEmpty()) {
            InsightsUiState.Empty
        } else {
            InsightsUiState.Success(filtered)
        }
    }

    /**
     * Clear filter and show all anomalies.
     */
    fun clearFilter() {
        filterByType(null)
    }

    /**
     * Refresh anomalies (recompute from latest data).
     */
    fun refresh(carId: Long? = null) {
        loadAnomalies(carId)
    }

    /**
     * Add a refill to a trip (for MISSING_TRIP_REFILL anomalies).
     * This will trigger automatic refresh via transaction observation.
     */
    fun addRefillToTrip(refillId: Long, tripId: Long) {
        viewModelScope.launch {
            addRefillsToTripUseCase(tripId, listOf(refillId))
        }
    }

    private fun applyFilter(anomalies: List<Anomaly>): List<Anomaly> {
        val filter = _selectedFilter.value ?: return anomalies
        return anomalies.filter { it.type == filter }
    }
}

/**
 * UI state for Insights screen.
 */
sealed class InsightsUiState {
    data object Loading : InsightsUiState()
    data object Empty : InsightsUiState()
    data class Success(val anomalies: List<Anomaly>) : InsightsUiState()
    data class Error(val message: String) : InsightsUiState()
}

/**
 * Navigation events emitted by InsightsViewModel.
 */
sealed class NavigationEvent {
    data class NavigateToRefillDetails(val refillId: Long) : NavigationEvent()
    data class NavigateToExpenseDetails(val expenseId: Long) : NavigationEvent()
    data class ShowAggregateInfo(val message: String) : NavigationEvent()
}

