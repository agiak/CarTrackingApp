package com.agcoding.cartrackingapp.presentation.distancegraph

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
import androidx.compose.material3.ModalBottomSheet
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
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.PeriodSelectorSheet
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistanceGraphScreen(
    onNavigateBack: () -> Unit,
    viewModel: DistanceGraphViewModel
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
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Car filter button
                    if (allCars.size > 1) {
                        TextButton(
                            onClick = { viewModel.showCarFilter() },
                            modifier = Modifier.width(160.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val selectedCars = allCars.filter { selectedCarIds.contains(it.id) }
                            Text(
                                text = when {
                                    selectedCars.isEmpty() -> stringResource(R.string.all_cars)
                                    selectedCars.size == 1 -> selectedCars[0].name
                                    else -> selectedCars.joinToString(", ") { it.name }
                                },
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Period selector button
                    TextButton(
                        onClick = { viewModel.showPeriodSelector() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(selectedPeriod.labelResId),
                            fontSize = 14.sp
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
            is DistanceGraphUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DistanceGraphUiState.NoData -> {
                NoDataState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is DistanceGraphUiState.Success -> {
                DistanceGraphContent(
                    trendData = state.trendData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is DistanceGraphUiState.Error -> {
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
                    title = stringResource(R.string.distance_graph_select_period_title),
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
