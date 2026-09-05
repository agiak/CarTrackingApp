package com.agcoding.cartrackingapp.presentation.refillsgraph

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.DateFilterSheet
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefillsGraphScreen(
    onNavigateBack: () -> Unit,
    viewModel: RefillsGraphViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    val availableYears by viewModel.availableYears.collectAsState()
    val showPeriodSelector by viewModel.showPeriodSelector.collectAsState()
    val allCars by viewModel.allCars.collectAsState()
    val selectedCarIds by viewModel.selectedCarIds.collectAsState()
    val showCarFilter by viewModel.showCarFilter.collectAsState()

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.refills_graph_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Car filter button — icon only so long (Greek) labels never
                    // squeeze the app-bar title into a broken vertical stack.
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
                            contentDescription = stringResource(R.string.date_filter_title),
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
            is RefillsGraphUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RefillsGraphUiState.NoData -> {
                NoDataState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is RefillsGraphUiState.Success -> {
                RefillsGraphContent(
                    trendData = state.trendData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is RefillsGraphUiState.Error -> {
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
            DateFilterSheet(
                selected = dateFilter,
                availableYears = availableYears,
                onFilterChange = viewModel::setDateFilter,
                onDismiss = { viewModel.hidePeriodSelector() }
            )
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

@Preview(showBackground = true)
@Composable
private fun RefillsGraphScreenPreview() {
    CarTrackingAppTheme {
        RefillsGraphScreen(
            onNavigateBack = {}
        )
    }
}
