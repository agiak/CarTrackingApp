package com.agcoding.cartrackingapp.presentation.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.presentation.insights.components.AnomalyCard
import com.agcoding.cartrackingapp.presentation.insights.components.AnomalyFilterChips
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.time.LocalDate

@Composable
internal fun SuccessState(
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

        // No match for the active filter. Shown inline, below the chips, so the
        // user can pick another type or clear the filter without leaving.
        if (anomalies.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.insights_no_results_for_filter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onClearFilter) {
                        Text(stringResource(R.string.insights_show_all))
                    }
                }
            }
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

@Preview(showBackground = true)
@Composable
private fun SuccessStatePreview() {
    CarTrackingAppTheme {
        SuccessState(
            anomalies = listOf(
                Anomaly(
                    id = "1",
                    carId = 1L,
                    type = AnomalyType.FUEL_PRICE_SPIKE,
                    severity = AnomalySeverity.HIGH,
                    title = "Sample",
                    description = "Sample",
                    detectedAt = LocalDate.now(),
                    relatedTransactionId = 123L
                )
            ),
            selectedFilter = null,
            onFilterSelected = {},
            onClearFilter = {},
            onAnomalyClick = {},
            onAddToTrip = { _, _ -> }
        )
    }
}
