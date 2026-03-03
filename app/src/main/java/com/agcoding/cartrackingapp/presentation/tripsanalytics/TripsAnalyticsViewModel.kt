package com.agcoding.cartrackingapp.presentation.tripsanalytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.TripStatistics
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class TripSortOption {
    MOST_RECENT,
    MOST_COSTLY,
    LEAST_COSTLY,
    HIGHEST_FUEL_CONSUMPTION,
    LOWEST_FUEL_CONSUMPTION,
    LONGEST_DISTANCE,
    SHORTEST_DISTANCE
}

data class TripHighlights(
    val mostCostly: TripStatistics?,
    val cheapest: TripStatistics?,
    val longestDistance: TripStatistics?,
    val shortestDistance: TripStatistics?,
    val mostFuelEfficient: TripStatistics?,
    val leastFuelEfficient: TripStatistics?
)

sealed class TripsAnalyticsUiState {
    object Loading : TripsAnalyticsUiState()
    object Empty : TripsAnalyticsUiState()
    data class Success(
        val highlights: TripHighlights,
        val sortedTrips: List<TripStatistics>,
        val activeSortOption: TripSortOption
    ) : TripsAnalyticsUiState()
    data class Error(val message: String) : TripsAnalyticsUiState()
}

@HiltViewModel
class TripsAnalyticsViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TripsAnalyticsUiState>(TripsAnalyticsUiState.Loading)
    val uiState: StateFlow<TripsAnalyticsUiState> = _uiState.asStateFlow()

    private val _sortOption = MutableStateFlow(TripSortOption.MOST_RECENT)
    val sortOption: StateFlow<TripSortOption> = _sortOption.asStateFlow()

    /** Raw computed statistics for all trips – reused when sort changes */
    private var allTripStatistics: List<TripStatistics> = emptyList()

    init {
        loadAllTrips()
    }

    private fun loadAllTrips() {
        viewModelScope.launch {
            tripRepository.getAllTrips()
                .catch { e ->
                    _uiState.value = TripsAnalyticsUiState.Error(e.message ?: "Failed to load trips")
                }
                .collect { trips ->
                    if (trips.isEmpty()) {
                        allTripStatistics = emptyList()
                        _uiState.value = TripsAnalyticsUiState.Empty
                        return@collect
                    }

                    // Compute statistics on background dispatcher
                    val stats = withContext(Dispatchers.Default) {
                        trips.map { trip -> computeStatistics(trip) }
                    }
                    allTripStatistics = stats
                    applySort(_sortOption.value)
                }
        }
    }

    fun setSortOption(option: TripSortOption) {
        _sortOption.value = option
        applySort(option)
    }

    private fun applySort(option: TripSortOption) {
        if (allTripStatistics.isEmpty()) return

        val sorted = when (option) {
            TripSortOption.MOST_RECENT ->
                allTripStatistics.sortedByDescending { it.startDate ?: it.trip.createdAt }
            TripSortOption.MOST_COSTLY ->
                allTripStatistics.sortedByDescending { it.totalCost }
            TripSortOption.LEAST_COSTLY ->
                allTripStatistics.sortedBy { it.totalCost }
            TripSortOption.HIGHEST_FUEL_CONSUMPTION ->
                allTripStatistics.sortedByDescending { it.averageConsumption }
            TripSortOption.LOWEST_FUEL_CONSUMPTION ->
                allTripStatistics.filter { it.averageConsumption > 0 }
                    .sortedBy { it.averageConsumption } +
                        allTripStatistics.filter { it.averageConsumption <= 0 }
            TripSortOption.LONGEST_DISTANCE ->
                allTripStatistics.sortedByDescending { it.totalDistance }
            TripSortOption.SHORTEST_DISTANCE ->
                allTripStatistics.sortedBy { it.totalDistance }
        }

        val highlights = computeHighlights(allTripStatistics)

        _uiState.value = TripsAnalyticsUiState.Success(
            highlights = highlights,
            sortedTrips = sorted,
            activeSortOption = option
        )
    }

    private fun computeHighlights(stats: List<TripStatistics>): TripHighlights {
        val validConsumption = stats.filter { it.averageConsumption > 0 }
        return TripHighlights(
            mostCostly = stats.maxByOrNull { it.totalCost },
            cheapest = stats.minByOrNull { it.totalCost },
            longestDistance = stats.maxByOrNull { it.totalDistance },
            shortestDistance = stats.minByOrNull { it.totalDistance },
            mostFuelEfficient = validConsumption.minByOrNull { it.averageConsumption },
            leastFuelEfficient = validConsumption.maxByOrNull { it.averageConsumption }
        )
    }

    private fun computeStatistics(
        trip: com.agcoding.cartrackingapp.domain.model.Trip
    ): TripStatistics {
        val refills = trip.refills
        val totalDistance = refills.sumOf { it.tripDistance }
        val totalFuelConsumed = refills.sumOf { it.litersAdded }
        val totalCost = refills.sumOf { it.amountPaid }
        val averageConsumption = if (totalDistance > 0) {
            (totalFuelConsumed / totalDistance) * 100
        } else 0.0
        return TripStatistics(
            trip = trip,
            totalDistance = totalDistance,
            totalFuelConsumed = totalFuelConsumed,
            totalCost = totalCost,
            averageConsumption = averageConsumption,
            refillCount = refills.size,
            startDate = refills.minOfOrNull { it.timestamp },
            endDate = refills.maxOfOrNull { it.timestamp }
        )
    }
}

