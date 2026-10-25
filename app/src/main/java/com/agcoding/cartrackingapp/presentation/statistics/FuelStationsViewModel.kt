package com.agcoding.cartrackingapp.presentation.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.StationPeriod
import com.agcoding.cartrackingapp.domain.model.StationSummary
import com.agcoding.cartrackingapp.domain.model.TopRefill
import com.agcoding.cartrackingapp.domain.model.TopRefillSort
import com.agcoding.cartrackingapp.domain.model.window
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetFuelStationStatsUseCase
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetTopRefillsUseCase
import com.agcoding.cartrackingapp.util.safeDivide
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which half of the Fuel stations screen is showing. */
enum class FuelStationsTab { STATIONS, TOP_REFILLS }

/**
 * What the two tabs need, including the filters that produced it.
 *
 * The totals are derived from [stations] rather than queried separately: the
 * station list already covers every refill in the window, so a second sum would
 * be a second chance to disagree with the list the user is looking at.
 */
data class FuelStationsUiState(
    val tab: FuelStationsTab = FuelStationsTab.STATIONS,
    val period: StationPeriod = StationPeriod.LAST_YEAR,
    /**
     * The cars ticked in the filter sheet, always explicit — every id when
     * nothing is narrowed, never the empty set. The sheet draws checkboxes
     * from this, and an empty set there would render every box unticked while
     * the screen showed every car.
     */
    val selectedCarIds: Set<Long> = emptySet(),
    val sort: TopRefillSort = TopRefillSort.TOTAL_COST,
    val expandedStationKey: String? = null,
    val expandedRefillId: Long? = null,
    val showCarFilter: Boolean = false,
    val cars: List<Car> = emptyList(),
    val stations: List<StationSummary> = emptyList(),
    val topRefills: List<TopRefill> = emptyList(),
    val isLoading: Boolean = true,
) {
    val totalCost: Double get() = stations.sumOf { it.totalCost }
    val refillCount: Int get() = stations.sumOf { it.refillCount }

    /** Nothing was bought in this window — both tabs say so the same way. */
    val isEmpty: Boolean get() = !isLoading && refillCount == 0

    /** True while the filter names every car, which is the same as naming none. */
    val isAllCars: Boolean
        get() = selectedCarIds.isEmpty() || selectedCarIds.size == cars.size

    fun shareOf(station: StationSummary): Double =
        station.totalCost.safeDivide(totalCost) * 100.0

    /**
     * The cheapest and dearest stations by average price per litre, or null
     * each when the comparison would be meaningless.
     *
     * With one station — or several that happen to have charged the same — the
     * same row would be both the best and the worst, so neither is marked.
     * Stations with no litres recorded are left out rather than ranked at zero.
     */
    private val comparableStations: List<StationSummary>
        get() = stations
            .filter { it.totalLiters > 0.0 }
            .takeIf { priced -> priced.distinctBy { it.averagePricePerLiter }.size > 1 }
            .orEmpty()

    val cheapestStationKey: String?
        get() = comparableStations.minByOrNull { it.averagePricePerLiter }?.key

    val dearestStationKey: String?
        get() = comparableStations.maxByOrNull { it.averagePricePerLiter }?.key
}

/** The filter choices that send a new query to the database. */
private data class StationQuery(
    val period: StationPeriod,
    /** Empty means every car; see `RefillRepository`. */
    val carIds: Set<Long>,
    val sort: TopRefillSort,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FuelStationsViewModel @Inject constructor(
    private val getFuelStationStats: GetFuelStationStatsUseCase,
    private val getTopRefills: GetTopRefillsUseCase,
    carRepository: CarRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FuelStationsUiState(tab = savedStateHandle.initialTab())
    )
    val uiState: StateFlow<FuelStationsUiState> = _uiState.asStateFlow()

    private val query = MutableStateFlow(
        StationQuery(
            period = StationPeriod.LAST_YEAR,
            carIds = emptySet(),
            sort = TopRefillSort.TOTAL_COST,
        )
    )

    init {
        viewModelScope.launch {
            carRepository.getAllCars().collect { cars ->
                val state = _uiState.value
                _uiState.value = state.copy(
                    cars = cars,
                    // Until the user narrows anything, the sheet should show
                    // every car ticked rather than an empty list of choices.
                    selectedCarIds = if (state.isAllCars) {
                        cars.map { it.id }.toSet()
                    } else {
                        state.selectedCarIds
                    },
                )
            }
        }

        viewModelScope.launch {
            query
                .distinctUntilChanged()
                .flatMapLatest { q ->
                    // The window is resolved per query rather than once at
                    // construction: a screen left open overnight would
                    // otherwise still measure "six months" from yesterday.
                    val window = q.period.window()
                    combine(
                        getFuelStationStats(window, q.carIds),
                        getTopRefills(window, q.carIds, q.sort),
                    ) { stations, top -> stations to top }
                }
                .collect { (stations, top) ->
                    _uiState.value = _uiState.value.copy(
                        stations = stations,
                        topRefills = top,
                        isLoading = false,
                    )
                }
        }
    }

    fun selectTab(tab: FuelStationsTab) {
        // Collapsing on the way out means coming back to a tab shows it as a
        // list again, not as whatever row happened to be open last time.
        _uiState.value = _uiState.value.copy(
            tab = tab,
            expandedStationKey = null,
            expandedRefillId = null,
        )
    }

    fun selectPeriod(period: StationPeriod) {
        _uiState.value = _uiState.value.copy(
            period = period,
            expandedStationKey = null,
            expandedRefillId = null,
            isLoading = true,
        )
        query.value = query.value.copy(period = period)
    }

    fun selectSort(sort: TopRefillSort) {
        _uiState.value = _uiState.value.copy(sort = sort, expandedRefillId = null)
        query.value = query.value.copy(sort = sort)
    }

    fun showCarFilter() {
        _uiState.value = _uiState.value.copy(showCarFilter = true)
    }

    fun hideCarFilter() {
        _uiState.value = _uiState.value.copy(showCarFilter = false)
    }

    fun toggleCarSelection(carId: Long, selected: Boolean) {
        val current = _uiState.value.selectedCarIds
        _uiState.value = _uiState.value.copy(
            selectedCarIds = if (selected) current + carId else current - carId,
        )
    }

    /**
     * Commits what the sheet has been editing.
     *
     * "Every car ticked" and "none ticked" both reach the query as the empty
     * set, so the database is never asked for an explicit list of all the cars
     * — and clearing the filter cannot be mistaken for asking for no cars.
     */
    fun applyCarFilter() {
        val state = _uiState.value
        _uiState.value = state.copy(
            showCarFilter = false,
            expandedStationKey = null,
            expandedRefillId = null,
            isLoading = true,
        )
        query.value = query.value.copy(
            carIds = if (state.isAllCars) emptySet() else state.selectedCarIds,
        )
    }

    fun toggleStation(key: String) {
        val current = _uiState.value.expandedStationKey
        _uiState.value = _uiState.value.copy(expandedStationKey = if (current == key) null else key)
    }

    fun toggleRefill(id: Long) {
        val current = _uiState.value.expandedRefillId
        _uiState.value = _uiState.value.copy(expandedRefillId = if (current == id) null else id)
    }
}

/**
 * The tab the entry tile asked for.
 *
 * Read from the route rather than passed down as a composable parameter so it
 * survives a rotation: a `LaunchedEffect` that re-applied it would drag the
 * user back to the Stations tab every time the screen was recreated.
 */
private fun SavedStateHandle.initialTab(): FuelStationsTab =
    get<String>(FUEL_STATIONS_TAB_ARG)
        ?.let { name -> FuelStationsTab.entries.firstOrNull { it.name == name } }
        ?: FuelStationsTab.STATIONS

/** Route argument name, shared with the navigation graph. */
const val FUEL_STATIONS_TAB_ARG = "tab"
