@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.periodcomparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.PeriodComparison
import com.agcoding.cartrackingapp.domain.model.PeriodStatistics
import com.agcoding.cartrackingapp.presentation.components.DateFilterSheet
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/** Which of the two periods a picker is editing. */
internal enum class ComparisonSide { PRIMARY, SECONDARY }

/**
 * Two periods of one car's history, side by side.
 *
 * Both periods are chosen with the same year/month picker used everywhere else, so a
 * period can be one or more whole years, or specific months inside them. Each metric
 * shows its value in both periods plus the difference between them.
 */
@Composable
fun PeriodComparisonScreen(
    onNavigateBack: () -> Unit,
    viewModel: PeriodComparisonViewModel = hiltViewModel()
) {
    val comparison by viewModel.comparison.collectAsStateWithLifecycle()
    val primaryFilter by viewModel.primaryFilter.collectAsStateWithLifecycle()
    val secondaryFilter by viewModel.secondaryFilter.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val car by viewModel.car.collectAsStateWithLifecycle()

    var editingSide by remember { mutableStateOf<ComparisonSide?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.period_comparison_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val name = car?.name.orEmpty()
                        if (name.isNotBlank()) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::swapPeriods) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = stringResource(R.string.period_comparison_swap)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        PeriodComparisonList(
            comparison = comparison,
            primaryFilter = primaryFilter,
            secondaryFilter = secondaryFilter,
            onEditPrimary = { editingSide = ComparisonSide.PRIMARY },
            onEditSecondary = { editingSide = ComparisonSide.SECONDARY },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }

    // One sheet, pointed at whichever period the user tapped.
    editingSide?.let { side ->
        DateFilterSheet(
            selected = if (side == ComparisonSide.PRIMARY) primaryFilter else secondaryFilter,
            availableYears = availableYears,
            onFilterChange = { filter ->
                when (side) {
                    ComparisonSide.PRIMARY -> viewModel.setPrimaryFilter(filter)
                    ComparisonSide.SECONDARY -> viewModel.setSecondaryFilter(filter)
                }
            },
            onDismiss = { editingSide = null }
        )
    }
}

@Composable
private fun PeriodComparisonList(
    comparison: PeriodComparison,
    primaryFilter: DateFilter,
    secondaryFilter: DateFilter,
    onEditPrimary: () -> Unit,
    onEditSecondary: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        periodComparisonItems(
            comparison = comparison,
            primaryFilter = primaryFilter,
            secondaryFilter = secondaryFilter,
            onEditPrimary = onEditPrimary,
            onEditSecondary = onEditSecondary
        )
    }
}

// ============================================
// Preview Composables
// ============================================

private val sampleComparison = PeriodComparison(
    primary = PeriodStatistics(
        filter = DateFilter.of(year = 2025),
        totalCost = 1842.5,
        fuelCost = 1412.5,
        expensesCost = 430.0,
        totalDistance = 12400.0,
        totalLiters = 848.4,
        averageConsumption = 6.84,
        averagePricePerLiter = 1.66,
        costPerKilometer = 0.149,
        refillCount = 19,
        expenseCount = 4
    ),
    secondary = PeriodStatistics(
        filter = DateFilter.of(year = 2024),
        totalCost = 1610.0,
        fuelCost = 1280.0,
        expensesCost = 330.0,
        totalDistance = 11100.0,
        totalLiters = 792.0,
        averageConsumption = 7.13,
        averagePricePerLiter = 1.62,
        costPerKilometer = 0.145,
        refillCount = 17,
        expenseCount = 3
    )
)

@Preview(name = "Period comparison", showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun PreviewPeriodComparison() {
    CarTrackingAppTheme(darkTheme = false) {
        PeriodComparisonList(
            comparison = sampleComparison,
            primaryFilter = DateFilter.of(year = 2025),
            secondaryFilter = DateFilter.of(year = 2024),
            onEditPrimary = {},
            onEditSecondary = {}
        )
    }
}

@Preview(name = "Period comparison - dark", showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun PreviewPeriodComparisonDark() {
    CarTrackingAppTheme(darkTheme = true) {
        PeriodComparisonList(
            comparison = sampleComparison,
            primaryFilter = DateFilter(years = setOf(2025), months = setOf(3, 4)),
            secondaryFilter = DateFilter(years = setOf(2024), months = setOf(3, 4)),
            onEditPrimary = {},
            onEditSecondary = {}
        )
    }
}

@Preview(name = "Period comparison - empty", showBackground = true, widthDp = 400)
@Composable
private fun PreviewPeriodComparisonEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        PeriodComparisonList(
            comparison = PeriodComparison(),
            primaryFilter = DateFilter.of(year = 2025),
            secondaryFilter = DateFilter.of(year = 2024),
            onEditPrimary = {},
            onEditSecondary = {}
        )
    }
}
