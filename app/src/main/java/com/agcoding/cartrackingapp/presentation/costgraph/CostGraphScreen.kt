package com.agcoding.cartrackingapp.presentation.costgraph

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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.PeriodSelectorSheet
import com.agcoding.cartrackingapp.domain.model.CostCategory
import com.agcoding.cartrackingapp.domain.model.CostItem
import com.agcoding.cartrackingapp.domain.model.CostTrendData
import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.domain.model.MonthlyCost
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.costgraph.components.CostGraphContent
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.presentation.costgraph.components.ErrorState
import com.agcoding.cartrackingapp.presentation.costgraph.components.NoDataState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostGraphScreen(
    onNavigateBack: () -> Unit,
    viewModel: CostGraphViewModel = hiltViewModel()
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
                title = { Text(stringResource(R.string.cost_graph_title)) },
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
                            contentDescription = stringResource(selectedPeriod.labelResId),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is CostGraphUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is CostGraphUiState.NoData -> {
                NoDataState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is CostGraphUiState.Success -> {
                CostGraphContent(
                    trendData = state.trendData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is CostGraphUiState.Error -> {
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

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Cost Graph Screen - Light", showBackground = true, showSystemUi = true)
@Composable
fun PreviewCostGraphScreen() {
    val mockTrendData = CostTrendData(
        monthlyCosts = listOf(
            MonthlyCost("Jan", 2026, 450.0, 300.0, 120.0, 30.0, System.currentTimeMillis() - 5184000000L),
            MonthlyCost("Feb", 2026, 520.0, 350.0, 150.0, 20.0, System.currentTimeMillis() - 2592000000L)
        ),
        totalCost = 970.0,
        averageMonthlyCost = 485.0,
        highestMonthCost = 520.0,
        lowestMonthCost = 450.0,
        costByCategory = listOf(
            CostCategory("Fuel", 650.0, 67.0, 0xFF4CAF50.toInt()),
            CostCategory("Service", 270.0, 27.8, 0xFF2196F3.toInt())
        ),
        recentExpenses = listOf(
            CostItem(1, System.currentTimeMillis(), "Fuel", "Gas", 65.0, "Toyota")
        ),
        dateRange = DateRange(System.currentTimeMillis() - 5184000000L, System.currentTimeMillis(), "Last 2 months")
    )

    CarTrackingAppTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                StyledTopAppBar(
                    title = { Text(stringResource(R.string.cost_graph_title)) }
                )
            }
        ) { paddingValues ->
            CostGraphContent(
                trendData = mockTrendData,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
