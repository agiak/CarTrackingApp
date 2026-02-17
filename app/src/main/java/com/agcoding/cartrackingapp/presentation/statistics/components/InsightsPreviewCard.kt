package com.agcoding.cartrackingapp.presentation.statistics.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.usecase.insights.GetAllAnomaliesUseCase
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Preview card showing count of anomalies detected in current month.
 * Displayed in Statistics screen.
 *
 * Clicking navigates to full Insights screen.
 */
@Composable
fun InsightsPreviewCard(
    onNavigateToInsights: () -> Unit,
    modifier: Modifier = Modifier,
    getAllAnomaliesUseCase: GetAllAnomaliesUseCase = hiltViewModel<InsightsPreviewViewModel>().getAllAnomaliesUseCase
) {
    var anomalyCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Load anomaly count
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.Default) {
            try {
                val anomalies = getAllAnomaliesUseCase.getCurrentMonthAnomalies()
                anomalyCount = anomalies.size
            } catch (_: Exception) {
                anomalyCount = 0
            } finally {
                isLoading = false
            }
        }
    }

    StyledCard(
        modifier = modifier.clickable(onClick = onNavigateToInsights)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                // Text content
                Column {
                    Text(
                        text = stringResource(R.string.insights_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (isLoading) {
                        Text(
                            text = stringResource(R.string.loading),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = if (anomalyCount > 0) {
                                stringResource(R.string.insights_preview_card_title, anomalyCount)
                            } else {
                                stringResource(R.string.insights_preview_card_no_anomalies)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (anomalyCount > 0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Arrow icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Lightweight ViewModel just to inject GetAllAnomaliesUseCase
@dagger.hilt.android.lifecycle.HiltViewModel
class InsightsPreviewViewModel @javax.inject.Inject constructor(
    val getAllAnomaliesUseCase: GetAllAnomaliesUseCase
) : androidx.lifecycle.ViewModel()

