package com.agcoding.cartrackingapp.presentation.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.catch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.insights.components.AnomalyCard
import com.agcoding.cartrackingapp.presentation.insights.components.AnomalyFilterChips
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Main Insights screen showing all detected anomalies.
 *
 * Features:
 * - Displays anomalies in chronological order (newest first)
 * - Filter by anomaly type
 * - Empty state when no anomalies detected
 * - Loading state during computation
 * - Error handling
 * - Navigation to transaction details on card tap
 * - Automatic refresh when transactions change
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onBackClick: () -> Unit,
    onNavigateToRefillDetails: (Long) -> Unit,
    onNavigateToExpenseDetails: (Long) -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    // Observe navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent
            .catch { e -> android.util.Log.e("InsightsScreen", "Navigation event error", e) }
            .collect { event ->
            when (event) {
                is NavigationEvent.NavigateToRefillDetails -> {
                    onNavigateToRefillDetails(event.refillId)
                }
                is NavigationEvent.NavigateToExpenseDetails -> {
                    onNavigateToExpenseDetails(event.expenseId)
                }
                is NavigationEvent.ShowAggregateInfo -> {
                    // Could show a dialog here, for now just ignore
                    // This handles monthly/cost-per-km anomalies without specific transaction
                }
            }
        }
    }

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.insights_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is InsightsUiState.Loading -> {
                    LoadingState()
                }

                is InsightsUiState.Empty -> {
                    EmptyState()
                }

                is InsightsUiState.Success -> {
                    SuccessState(
                        anomalies = state.anomalies,
                        selectedFilter = selectedFilter,
                        onFilterSelected = { viewModel.filterByType(it) },
                        onClearFilter = { viewModel.clearFilter() },
                        onAnomalyClick = { viewModel.onAnomalyClick(it) },
                        onAddToTrip = { refillId, tripId ->
                            viewModel.addRefillToTrip(refillId, tripId)
                        }
                    )
                }

                is InsightsUiState.Error -> {
                    ErrorState(message = state.message)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsScreenPreview() {
    CarTrackingAppTheme {
        InsightsScreen(
            onBackClick = {},
            onNavigateToRefillDetails = {},
            onNavigateToExpenseDetails = {}
        )
    }
}

