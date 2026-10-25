package com.agcoding.cartrackingapp.presentation.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.StationPeriod
import com.agcoding.cartrackingapp.domain.model.StationSummary
import com.agcoding.cartrackingapp.domain.model.window
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetFuelStationStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StationRefillsUiState(
    val stationName: String = "",
    val stationAddress: String? = null,
    val refills: List<FuelRefill> = emptyList(),
    val cars: List<Car> = emptyList(),
    val isLoading: Boolean = true,
) {
    fun carNameFor(refill: FuelRefill): String? =
        cars.firstOrNull { it.id == refill.carId }?.name
}

/**
 * One station's refills, under the same period and car filter that produced the
 * station list.
 *
 * The route carries the station's *key* rather than the strings behind it. The
 * database groups on raw text — "Shell", "shell ", "Shell · Kifisias" may all
 * be this one station — and a route that tried to carry that set would have to
 * encode arbitrary user text of unbounded length. Re-deriving the grouping from
 * the same filters costs one extra query and cannot drift from what the
 * previous screen showed, because it is the same code.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StationRefillsViewModel @Inject constructor(
    getFuelStationStats: GetFuelStationStatsUseCase,
    refillRepository: RefillRepository,
    carRepository: CarRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationRefillsUiState())
    val uiState: StateFlow<StationRefillsUiState> = _uiState.asStateFlow()

    init {
        val key = savedStateHandle.stationKey()
        val period = savedStateHandle.stationPeriod()
        val carIds = savedStateHandle.stationCarIds()
        val window = period.window()

        viewModelScope.launch {
            carRepository.getAllCars().collect { cars ->
                _uiState.value = _uiState.value.copy(cars = cars)
            }
        }

        viewModelScope.launch {
            getFuelStationStats(window, carIds)
                .flatMapLatest { stations ->
                    // The station stopped existing while the screen was open —
                    // its last refill was deleted, or edited onto another
                    // name. An empty list is the honest answer.
                    val station = stations.firstOrNull { it.key == key }
                        ?: return@flatMapLatest flowOf(StationView())

                    refillRepository
                        .observeRefillsAtStation(
                            window = window,
                            carIds = carIds,
                            rawNames = station.rawNames,
                        )
                        .map { refills -> StationView(station, refills) }
                }
                .collect { view ->
                    _uiState.value = _uiState.value.copy(
                        stationName = view.station?.name.orEmpty(),
                        stationAddress = view.station?.address,
                        refills = view.refills,
                        isLoading = false,
                    )
                }
        }
    }

}

/** The station and its refills, loaded together so neither can be shown without the other. */
private data class StationView(
    val station: StationSummary? = null,
    val refills: List<FuelRefill> = emptyList(),
)

private fun SavedStateHandle.stationKey(): String =
    get<String>(STATION_KEY_ARG)
        ?.takeIf { it != STATION_KEY_UNKNOWN }
        .orEmpty()

private fun SavedStateHandle.stationPeriod(): StationPeriod =
    get<String>(STATION_PERIOD_ARG)
        ?.let { name -> StationPeriod.entries.firstOrNull { it.name == name } }
        ?: StationPeriod.LAST_YEAR

private fun SavedStateHandle.stationCarIds(): Set<Long> =
    get<String>(STATION_CARS_ARG)
        ?.split(',')
        ?.mapNotNull { it.toLongOrNull() }
        ?.toSet()
        .orEmpty()

const val STATION_KEY_ARG = "stationKey"
const val STATION_PERIOD_ARG = "period"
const val STATION_CARS_ARG = "cars"

/**
 * Stands in for the station with no name at all.
 *
 * Its key is the empty string, which a route cannot carry — an absent argument
 * and an argument that is empty are the same thing to the navigation library.
 */
const val STATION_KEY_UNKNOWN = "__unknown__"
