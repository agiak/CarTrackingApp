package com.agcoding.cartrackingapp.presentation.consumptiongraph

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.PeriodSelectorSheet
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.consumptiongraph.components.ConsumptionGraphContent
import com.agcoding.cartrackingapp.presentation.consumptiongraph.components.ErrorState
import com.agcoding.cartrackingapp.presentation.consumptiongraph.components.NoDataState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionGraphScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConsumptionGraphViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val showPeriodSelector by viewModel.showPeriodSelector.collectAsState()
    val allCars by viewModel.allCars.collectAsState()
    val selectedCarIds by viewModel.selectedCarIds.collectAsState()
    val showCarFilter by viewModel.showCarFilter.collectAsState()

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.consumption_graph_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Car filter button
                    if (allCars.size > 1) {
                        IconButton(onClick = { viewModel.showCarFilter() }) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = stringResource(R.string.all_cars),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Period selector button
                    IconButton(onClick = { viewModel.showPeriodSelector() }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = stringResource(selectedPeriod.labelResId),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val state = uiState) {
            is ConsumptionGraphUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ConsumptionGraphUiState.NoData -> {
                NoDataState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is ConsumptionGraphUiState.Success -> {
                ConsumptionGraphContent(
                    trendData = state.trendData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is ConsumptionGraphUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = viewModel::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }

        // Period Selector Bottom Sheet
        if (showPeriodSelector) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hidePeriodSelector() }
            ) {
                PeriodSelectorSheet(
                    title = stringResource(R.string.select_time_period),
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { period ->
                        viewModel.selectPeriod(period)
                    }
                )
            }
        }

        // Car Filter Bottom Sheet
        if (showCarFilter) {
            com.agcoding.cartrackingapp.presentation.components.CarFilterSheet(
                cars = allCars,
                selectedCarIds = selectedCarIds,
                onCarSelectionChanged = { carId, selected ->
                    viewModel.toggleCarSelection(carId, selected)
                },
                onDismiss = { viewModel.hideCarFilter() },
                onApply = { viewModel.applyCarFilter() }
            )
        }
    }
}
