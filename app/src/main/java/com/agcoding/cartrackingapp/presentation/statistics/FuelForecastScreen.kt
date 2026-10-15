@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.AppFilterChip
import com.agcoding.cartrackingapp.presentation.components.FilterChipRow
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.statistics.components.FuelForecastSection
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * What next month is likely to cost.
 *
 * One car at a time, picked with a chip. The screen used to stack every car's
 * forecast vertically with a paragraph about confidence scores above them all,
 * so with three cars the third prediction was a long way below the fold and
 * the explanation was read before anything it explained.
 *
 * The confidence paragraph is gone: each forecast now carries its own
 * confidence bar, which says the same thing in the place where it applies.
 */
@Composable
fun FuelForecastScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val forecastState by viewModel.forecastUiState.collectAsState()
    val forecastingEnabled by viewModel.forecastingEnabled.collectAsState()
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    // Keyed by id rather than index so the selection survives a car being
    // added or removed while the screen is open.
    var selectedCarId by remember { mutableLongStateOf(-1L) }
    val selected = forecastState.firstOrNull { it.carId == selectedCarId }
        ?: forecastState.firstOrNull()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.fuel_forecast_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
        ) {
            when {
                !forecastingEnabled -> ForecastNotice(
                    icon = Icons.Default.OnlinePrediction,
                    message = stringResource(R.string.forecasting_disabled_message)
                )

                selected == null -> ForecastNotice(
                    icon = Icons.Default.Info,
                    message = stringResource(R.string.forecast_no_data_message)
                )

                else -> Column(
                    modifier = Modifier
                        .then(
                            if (isTablet) Modifier.fillMaxWidth(0.7f) else Modifier.fillMaxWidth()
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 28.dp)
                ) {
                    Text(
                        text = stringResource(R.string.fuel_forecast_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.contentSecondary,
                        modifier = Modifier
                            .padding(horizontal = dimens.spacing.screenHorizontal)
                            .padding(top = 4.dp, bottom = 14.dp)
                    )

                    // One car has nothing to choose between.
                    if (forecastState.size > 1) {
                        FilterChipRow {
                            forecastState.forEach { forecast ->
                                AppFilterChip(
                                    label = forecast.carName,
                                    selected = forecast.carId == selected.carId,
                                    onClick = { selectedCarId = forecast.carId }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    FuelForecastSection(
                        forecast = selected,
                        modifier = Modifier.padding(
                            horizontal = dimens.spacing.screenHorizontal
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastNotice(icon: ImageVector, message: String) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(dimens.radius.tile))
                    .background(scheme.actionPrimary.copy(alpha = 0.14f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.actionPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.contentSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "Forecast — nothing to show", showBackground = true, widthDp = 390, heightDp = 600)
@Composable
private fun PreviewForecastNotice() {
    CarTrackingAppTheme(darkTheme = true) {
        Row(modifier = Modifier.width(390.dp)) {
            ForecastNotice(
                icon = Icons.Default.Info,
                message = "No forecast data available.\n\n" +
                    "Add at least 3 months of fuel refills to see predictions."
            )
        }
    }
}
