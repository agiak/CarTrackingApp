package com.agcoding.cartrackingapp.presentation.carcomparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar

/**
 * Car Comparison Screen - Compare cars based on cost, consumption, and maintenance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarComparisonScreen(
    onNavigateBack: () -> Unit,
    viewModel: CarComparisonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val comparisonMode by viewModel.comparisonMode.collectAsState()

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.car_comparison_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Comparison mode selector
            if (uiState !is CarComparisonUiState.InsufficientCars) {
                ComparisonModeSelector(
                    selectedMode = comparisonMode,
                    onModeSelected = viewModel::setComparisonMode,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Content based on state
            when (val state = uiState) {
                is CarComparisonUiState.Loading -> {
                    LoadingContent()
                }
                is CarComparisonUiState.InsufficientCars -> {
                    InsufficientCarsContent()
                }
                is CarComparisonUiState.InsufficientData -> {
                    InsufficientDataContent()
                }
                is CarComparisonUiState.Error -> {
                    ErrorContent(message = state.message, onRetry = viewModel::retry)
                }
                is CarComparisonUiState.SelectingCars -> {
                    CarSelectorContent(viewModel = viewModel)
                }
                is CarComparisonUiState.TwoCarsComparison -> {
                    TwoCarsComparisonContent(result = state.result, viewModel = viewModel)
                }
                is CarComparisonUiState.AllCarsComparison -> {
                    AllCarsComparisonContent(result = state.result)
                }
            }
        }
    }
}

@Composable
private fun ComparisonModeSelector(
    selectedMode: ComparisonMode,
    onModeSelected: (ComparisonMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedMode == ComparisonMode.ALL_CARS,
            onClick = { onModeSelected(ComparisonMode.ALL_CARS) },
            label = { Text(stringResource(R.string.car_comparison_all_cars)) },
            leadingIcon = if (selectedMode == ComparisonMode.ALL_CARS) {
                { Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp)) }
            } else null,
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = selectedMode == ComparisonMode.TWO_CARS,
            onClick = { onModeSelected(ComparisonMode.TWO_CARS) },
            label = { Text(stringResource(R.string.car_comparison_two_cars)) },
            leadingIcon = if (selectedMode == ComparisonMode.TWO_CARS) {
                { Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp)) }
            } else null,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun InsufficientCarsContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.CompareArrows,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.car_comparison_insufficient_cars_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.car_comparison_insufficient_cars_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsufficientDataContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.CompareArrows,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.car_comparison_insufficient_data_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.car_comparison_insufficient_data_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.error_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

