package com.agcoding.cartrackingapp.presentation.carcomparison

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.SegmentedControl
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.components.TonalButton
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

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
    // A segmented control, not two filter chips. Chips say "narrow this
    // down" and can both be off; these two are the screen's only states and
    // exactly one of them is always true, which is what a segmented control
    // is for — and it shows the other mode instead of hiding it behind a
    // chip that looks unselected.
    SegmentedControl(
        options = listOf(
            stringResource(R.string.car_comparison_all_cars),
            stringResource(R.string.car_comparison_two_cars),
        ),
        selectedIndex = if (selectedMode == ComparisonMode.ALL_CARS) 0 else 1,
        onSelect = { index ->
            onModeSelected(
                if (index == 0) ComparisonMode.ALL_CARS else ComparisonMode.TWO_CARS
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = LocalAppColorScheme.current.actionPrimary)
    }
}

/**
 * The three dead ends this screen has: too few cars, too little data, and a
 * failure.
 *
 * They were three copies of the same column with different words in it. One
 * composable, three call sites — and now the error gets the same centred
 * layout the other two always had rather than its own slightly different one.
 */
@Composable
private fun ComparisonNotice(
    title: String,
    message: String,
    icon: ImageVector,
    tint: Color,
    action: (@Composable () -> Unit)? = null
) {
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
                    .background(tint.copy(alpha = 0.14f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.contentPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.contentSecondary,
                textAlign = TextAlign.Center
            )
            if (action != null) {
                Spacer(modifier = Modifier.height(22.dp))
                action()
            }
        }
    }
}

@Composable
private fun InsufficientCarsContent() {
    ComparisonNotice(
        title = stringResource(R.string.car_comparison_insufficient_cars_title),
        message = stringResource(R.string.car_comparison_insufficient_cars_message),
        icon = Icons.Default.CompareArrows,
        tint = LocalAppColorScheme.current.actionPrimary
    )
}

@Composable
private fun InsufficientDataContent() {
    ComparisonNotice(
        title = stringResource(R.string.car_comparison_insufficient_data_title),
        message = stringResource(R.string.car_comparison_insufficient_data_message),
        icon = Icons.Default.CompareArrows,
        tint = LocalAppColorScheme.current.actionPrimary
    )
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    ComparisonNotice(
        title = stringResource(R.string.error_title),
        message = message,
        icon = Icons.Default.ErrorOutline,
        tint = LocalAppColorScheme.current.statusError,
        action = {
            TonalButton(
                label = stringResource(R.string.retry),
                onClick = onRetry
            )
        }
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Mode selector - all cars", showBackground = true, widthDp = 400)
@Composable
private fun PreviewComparisonModeSelectorAll() {
    CarTrackingAppTheme(darkTheme = false) {
        ComparisonModeSelector(
            selectedMode = ComparisonMode.ALL_CARS,
            onModeSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Mode selector - two cars", showBackground = true, widthDp = 400)
@Composable
private fun PreviewComparisonModeSelectorTwo() {
    CarTrackingAppTheme(darkTheme = true) {
        ComparisonModeSelector(
            selectedMode = ComparisonMode.TWO_CARS,
            onModeSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Loading", showBackground = true, widthDp = 400, heightDp = 300)
@Composable
private fun PreviewLoadingContent() {
    CarTrackingAppTheme(darkTheme = false) { LoadingContent() }
}

@Preview(name = "Too few cars", showBackground = true, widthDp = 400, heightDp = 300)
@Composable
private fun PreviewInsufficientCarsContent() {
    CarTrackingAppTheme(darkTheme = false) { InsufficientCarsContent() }
}

@Preview(name = "Too little data", showBackground = true, widthDp = 400, heightDp = 300)
@Composable
private fun PreviewInsufficientDataContent() {
    CarTrackingAppTheme(darkTheme = false) { InsufficientDataContent() }
}

@Preview(name = "Error", showBackground = true, widthDp = 400, heightDp = 300)
@Composable
private fun PreviewErrorContent() {
    CarTrackingAppTheme(darkTheme = false) {
        ErrorContent(message = "Could not load the comparison", onRetry = {})
    }
}
