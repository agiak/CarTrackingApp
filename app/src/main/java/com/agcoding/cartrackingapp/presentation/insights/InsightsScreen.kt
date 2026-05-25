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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.insights.components.AnomalyCard
import com.agcoding.cartrackingapp.presentation.insights.components.AnomalyFilterChips

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

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.insights_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.insights_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.insights_error_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SuccessState(
    anomalies: List<Anomaly>,
    selectedFilter: AnomalyType?,
    onFilterSelected: (AnomalyType?) -> Unit,
    onClearFilter: () -> Unit,
    onAnomalyClick: (Anomaly) -> Unit,
    onAddToTrip: (Long, Long) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary header
        item {
            Column {
                Text(
                    text = stringResource(R.string.insights_summary, anomalies.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.insights_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Filter chips
        item {
            AnomalyFilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected,
                onClearFilter = onClearFilter,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Anomaly cards
        items(
            items = anomalies,
            key = { it.id }
        ) { anomaly ->
            AnomalyCard(
                anomaly = anomaly,
                onClick = { onAnomalyClick(anomaly) },
                onAddToTrip = onAddToTrip,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

