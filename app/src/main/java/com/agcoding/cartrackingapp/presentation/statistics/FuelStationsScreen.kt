@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.StationPeriod
import com.agcoding.cartrackingapp.domain.model.StationSummary
import com.agcoding.cartrackingapp.domain.model.TopRefillSort
import com.agcoding.cartrackingapp.presentation.components.AppFilterChip
import com.agcoding.cartrackingapp.presentation.components.CarFilterSheet
import com.agcoding.cartrackingapp.presentation.components.FilterChipRow
import com.agcoding.cartrackingapp.presentation.components.SegmentedControl
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.statistics.components.StationRow
import com.agcoding.cartrackingapp.presentation.statistics.components.StationsHeroCard
import com.agcoding.cartrackingapp.presentation.statistics.components.TopRefillRow
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * Where the fuel money went: by station, and by single refill.
 *
 * Two readings of the same records, which is why they are tabs on one screen
 * rather than two entries in the analysis grid that happen to look alike. The
 * stations tab answers "which forecourt is quietly costing me the most"; the
 * top refills tab answers "what was that one enormous bill".
 *
 * Rows expand in place instead of opening a detail screen. The extra figures —
 * litres, average price, odometer — are a glance, not a destination, and a
 * screen that has to be dismissed before the next row can be compared makes
 * comparing rows the slowest thing on it.
 *
 * Which tab opens first comes from the route; see `FuelStationsViewModel`.
 */
@Composable
fun FuelStationsScreen(
    onNavigateBack: () -> Unit,
    /**
     * The period and cars travel with the station: the refill list has to be
     * narrowed the same way the station's total was, or its rows will not add
     * up to the figure that was tapped.
     */
    onStationRefillsClick: (StationSummary, StationPeriod, Set<Long>) -> Unit,
    onRefillClick: (Long) -> Unit,
    viewModel: FuelStationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dimens = LocalAppDimens.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.fuel_stations_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SegmentedControl(
                options = listOf(
                    stringResource(R.string.fuel_stations_tab_stations),
                    stringResource(R.string.fuel_stations_tab_top),
                ),
                selectedIndex = if (state.tab == FuelStationsTab.STATIONS) 0 else 1,
                onSelect = { index ->
                    viewModel.selectTab(
                        if (index == 0) FuelStationsTab.STATIONS else FuelStationsTab.TOP_REFILLS
                    )
                },
                modifier = Modifier.padding(horizontal = dimens.spacing.screenHorizontal),
            )

            Spacer(Modifier.height(14.dp))

            PeriodAndCarChips(
                period = state.period,
                carLabel = carFilterLabel(state),
                onPeriodSelected = viewModel::selectPeriod,
                onCarsClick = viewModel::showCarFilter,
            )

            Spacer(Modifier.height(14.dp))

            when {
                state.isEmpty -> EmptyState()

                state.tab == FuelStationsTab.STATIONS -> StationsTab(
                    state = state,
                    onToggle = viewModel::toggleStation,
                    onShowRefills = { station ->
                        onStationRefillsClick(
                            station,
                            state.period,
                            if (state.isAllCars) emptySet() else state.selectedCarIds,
                        )
                    },
                )

                else -> TopRefillsTab(
                    state = state,
                    onSortSelected = viewModel::selectSort,
                    onToggle = viewModel::toggleRefill,
                    onOpenRefill = onRefillClick,
                )
            }
        }
    }

    if (state.showCarFilter) {
        CarFilterSheet(
            cars = state.cars,
            selectedCarIds = state.selectedCarIds,
            onCarSelectionChanged = viewModel::toggleCarSelection,
            onDismiss = viewModel::hideCarFilter,
            onApply = viewModel::applyCarFilter,
            onSelectAll = { state.cars.forEach { viewModel.toggleCarSelection(it.id, true) } },
            onClearAll = { state.cars.forEach { viewModel.toggleCarSelection(it.id, false) } },
        )
    }
}

/**
 * The period chips plus the car filter, on one scrolling row.
 *
 * The car control is a chip like the others rather than a top-bar action: it
 * narrows the same numbers the period chips narrow, and splitting the two
 * halves of one filter across the screen hid the car half completely.
 */
@Composable
private fun PeriodAndCarChips(
    period: StationPeriod,
    carLabel: String,
    onPeriodSelected: (StationPeriod) -> Unit,
    onCarsClick: () -> Unit,
) {
    FilterChipRow {
        AppFilterChip(
            label = stringResource(R.string.period_last_6_months),
            selected = period == StationPeriod.LAST_6_MONTHS,
            onClick = { onPeriodSelected(StationPeriod.LAST_6_MONTHS) },
        )
        AppFilterChip(
            label = stringResource(R.string.period_1_year),
            selected = period == StationPeriod.LAST_YEAR,
            onClick = { onPeriodSelected(StationPeriod.LAST_YEAR) },
        )
        AppFilterChip(
            label = stringResource(R.string.date_filter_all_time),
            selected = period == StationPeriod.ALL_TIME,
            onClick = { onPeriodSelected(StationPeriod.ALL_TIME) },
        )
        // Never drawn as selected: it opens a sheet rather than setting a
        // value, and a filled chip next to the period chips would read as a
        // fourth period.
        AppFilterChip(
            label = carLabel,
            selected = false,
            onClick = onCarsClick,
        )
    }
}

/** "All cars", the one car's name, or how many — whichever the chip can say in a word. */
@Composable
private fun carFilterLabel(state: FuelStationsUiState): String = when {
    state.isAllCars -> stringResource(R.string.all_cars)
    state.selectedCarIds.size == 1 ->
        state.cars.firstOrNull { it.id == state.selectedCarIds.first() }?.name
            ?: stringResource(R.string.all_cars)
    else -> stringResource(R.string.period_comparison_cars_selected, state.selectedCarIds.size)
}

@Composable
private fun StationsTab(
    state: FuelStationsUiState,
    onToggle: (String) -> Unit,
    onShowRefills: (StationSummary) -> Unit,
) {
    val dimens = LocalAppDimens.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = dimens.spacing.screenHorizontal,
            end = dimens.spacing.screenHorizontal,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            StationsHeroCard(
                totalCost = state.totalCost,
                stationCount = state.stations.size,
                refillCount = state.refillCount,
                stations = state.stations,
            )
        }

        itemsIndexed(
            items = state.stations,
            key = { _, station -> station.key },
        ) { _, station ->
            StationRow(
                station = station,
                share = state.shareOf(station),
                expanded = state.expandedStationKey == station.key,
                isCheapest = state.cheapestStationKey == station.key,
                isDearest = state.dearestStationKey == station.key,
                onClick = { onToggle(station.key) },
                onShowRefills = { onShowRefills(station) },
            )
        }
    }
}

@Composable
private fun TopRefillsTab(
    state: FuelStationsUiState,
    onSortSelected: (TopRefillSort) -> Unit,
    onToggle: (Long) -> Unit,
    onOpenRefill: (Long) -> Unit,
) {
    val dimens = LocalAppDimens.current
    Column(modifier = Modifier.fillMaxSize()) {
        FilterChipRow {
            AppFilterChip(
                label = stringResource(R.string.top_refills_sort_cost),
                selected = state.sort == TopRefillSort.TOTAL_COST,
                onClick = { onSortSelected(TopRefillSort.TOTAL_COST) },
            )
            AppFilterChip(
                label = stringResource(R.string.top_refills_sort_price),
                selected = state.sort == TopRefillSort.PRICE_PER_LITER,
                onClick = { onSortSelected(TopRefillSort.PRICE_PER_LITER) },
            )
            AppFilterChip(
                label = stringResource(R.string.top_refills_sort_liters),
                selected = state.sort == TopRefillSort.LITERS,
                onClick = { onSortSelected(TopRefillSort.LITERS) },
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = dimens.spacing.screenHorizontal,
                end = dimens.spacing.screenHorizontal,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(
                items = state.topRefills,
                key = { _, entry -> entry.refill.id },
            ) { index, entry ->
                TopRefillRow(
                    entry = entry,
                    rank = index + 1,
                    sort = state.sort,
                    expanded = state.expandedRefillId == entry.refill.id,
                    onClick = { onToggle(entry.refill.id) },
                    onOpen = { onOpenRefill(entry.refill.id) },
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    val scheme = LocalAppColorScheme.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.LocalGasStation,
                contentDescription = null,
                tint = scheme.contentDisabled,
                modifier = Modifier.size(44.dp),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.fuel_stations_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.contentSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
