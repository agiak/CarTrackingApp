package com.agcoding.cartrackingapp.presentation.yearlycomparison

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.AvailableYear
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.ComparisonMetric
import com.agcoding.cartrackingapp.domain.model.YearlyComparisonData
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlyComparisonScreen(
    onNavigateBack: () -> Unit,
    viewModel: YearlyComparisonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableYears by viewModel.availableYears.collectAsState()
    val selectedYear1 by viewModel.selectedYear1.collectAsState()
    val selectedYear2 by viewModel.selectedYear2.collectAsState()
    val showYear1Selector by viewModel.showYear1Selector.collectAsState()
    val showYear2Selector by viewModel.showYear2Selector.collectAsState()
    val availableCars by viewModel.availableCars.collectAsState()
    val selectedCarIds by viewModel.selectedCarIds.collectAsState()

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.yearly_comparison_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.yearly_comparison_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        // AnimatedContent crossfade — no flash when toggling cars or years
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it::class },
            label = "yearly_comparison_state",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { state ->
            when (state) {
                is YearlyComparisonUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is YearlyComparisonUiState.InsufficientData -> {
                    Column(Modifier.fillMaxSize()) {
                        CarFilterChips(
                            cars = availableCars,
                            selectedCarIds = selectedCarIds,
                            onCarToggled = { viewModel.toggleCar(it) },
                            onAllSelected = { viewModel.clearCarFilter() },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.yearly_comparison_insufficient_data_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.yearly_comparison_insufficient_data_message),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                is YearlyComparisonUiState.Success -> {
                    YearlyComparisonContent(
                        data = state.data,
                        availableYears = availableYears,
                        selectedYear1 = selectedYear1,
                        selectedYear2 = selectedYear2,
                        showYear1Selector = showYear1Selector,
                        showYear2Selector = showYear2Selector,
                        availableCars = availableCars,
                        selectedCarIds = selectedCarIds,
                        onYear1Click = { viewModel.showYear1Selector() },
                        onYear2Click = { viewModel.showYear2Selector() },
                        onYear1Selected = { viewModel.selectYear1(it) },
                        onYear2Selected = { viewModel.selectYear2(it) },
                        onDismissYear1Selector = { viewModel.hideYear1Selector() },
                        onDismissYear2Selector = { viewModel.hideYear2Selector() },
                        onCarToggled = { viewModel.toggleCar(it) },
                        onAllSelected = { viewModel.clearCarFilter() }
                    )
                }

                is YearlyComparisonUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.yearly_comparison_error, state.message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarFilterChips(
    cars: List<Car>,
    selectedCarIds: Set<Long>,
    onCarToggled: (Long) -> Unit,
    onAllSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (cars.size <= 1) return
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selectedCarIds.isEmpty(),
            onClick = onAllSelected,
            label = { Text(stringResource(R.string.filter_all)) },
            leadingIcon = if (selectedCarIds.isEmpty()) {
                { Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
            } else null
        )
        cars.forEach { car ->
            val isSelected = car.id in selectedCarIds
            FilterChip(
                selected = isSelected,
                onClick = { onCarToggled(car.id) },
                label = { Text(car.name) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else null
            )
        }
    }
}

@Composable
private fun YearlyComparisonContent(
    data: YearlyComparisonData,
    availableYears: List<AvailableYear>,
    selectedYear1: Int?,
    selectedYear2: Int?,
    showYear1Selector: Boolean,
    showYear2Selector: Boolean,
    availableCars: List<Car>,
    selectedCarIds: Set<Long>,
    onYear1Click: () -> Unit,
    onYear2Click: () -> Unit,
    onYear1Selected: (Int) -> Unit,
    onYear2Selected: (Int) -> Unit,
    onDismissYear1Selector: () -> Unit,
    onDismissYear2Selector: () -> Unit,
    onCarToggled: (Long) -> Unit,
    onAllSelected: () -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val useSplitView = configuration.screenWidthDp >= 600 || isLandscape

    if (useSplitView) {
        Column(modifier = Modifier.fillMaxSize()) {
            CarFilterChips(
                cars = availableCars,
                selectedCarIds = selectedCarIds,
                onCarToggled = onCarToggled,
                onAllSelected = onAllSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        YearSelectorRow(
                            year1 = selectedYear1 ?: 0,
                            year2 = selectedYear2 ?: 0,
                            availableYears = availableYears,
                            showYear1Selector = showYear1Selector,
                            showYear2Selector = showYear2Selector,
                            onYear1Click = onYear1Click,
                            onYear2Click = onYear2Click,
                            onYear1Selected = onYear1Selected,
                            onYear2Selected = onYear2Selected,
                            onDismissYear1Selector = onDismissYear1Selector,
                            onDismissYear2Selector = onDismissYear2Selector
                        )
                    }
                    items(data.metrics) { metric ->
                        ComparisonMetricCard(metric = metric)
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.yearly_comparison_monthly_trends),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    item { MonthlyComparisonChart(data = data) }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            CarFilterChips(
                cars = availableCars,
                selectedCarIds = selectedCarIds,
                onCarToggled = onCarToggled,
                onAllSelected = onAllSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    YearSelectorRow(
                        year1 = selectedYear1 ?: 0,
                        year2 = selectedYear2 ?: 0,
                        availableYears = availableYears,
                        showYear1Selector = showYear1Selector,
                        showYear2Selector = showYear2Selector,
                        onYear1Click = onYear1Click,
                        onYear2Click = onYear2Click,
                        onYear1Selected = onYear1Selected,
                        onYear2Selected = onYear2Selected,
                        onDismissYear1Selector = onDismissYear1Selector,
                        onDismissYear2Selector = onDismissYear2Selector
                    )
                }
                items(data.metrics) { metric ->
                    ComparisonMetricCard(metric = metric)
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.yearly_comparison_monthly_trends),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                item { MonthlyComparisonChart(data = data) }
            }
        }
    }
}

@Composable
private fun YearSelectorRow(
    year1: Int,
    year2: Int,
    availableYears: List<AvailableYear>,
    showYear1Selector: Boolean,
    showYear2Selector: Boolean,
    onYear1Click: () -> Unit,
    onYear2Click: () -> Unit,
    onYear1Selected: (Int) -> Unit,
    onYear2Selected: (Int) -> Unit,
    onDismissYear1Selector: () -> Unit,
    onDismissYear2Selector: () -> Unit
) {
    StyledCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.yearly_comparison_base_year),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(140.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = stringResource(R.string.yearly_comparison_comparison_year),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(140.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    YearSelectorBox(year = year1, onClick = onYear1Click, isPrimary = true)
                    DropdownMenu(expanded = showYear1Selector, onDismissRequest = onDismissYear1Selector) {
                        availableYears.forEach { yearData ->
                            DropdownMenuItem(
                                text = { Text(yearData.year.toString()) },
                                onClick = { onYear1Selected(yearData.year) },
                                enabled = yearData.year != year2
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )

                Box {
                    YearSelectorBox(year = year2, onClick = onYear2Click, isPrimary = false)
                    DropdownMenu(expanded = showYear2Selector, onDismissRequest = onDismissYear2Selector) {
                        availableYears.forEach { yearData ->
                            DropdownMenuItem(
                                text = { Text(yearData.year.toString()) },
                                onClick = { onYear2Selected(yearData.year) },
                                enabled = yearData.year != year1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearSelectorBox(year: Int, onClick: () -> Unit, isPrimary: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(140.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPrimary) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = year.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer
                   else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun getLocalizedMetricName(metricName: String): String {
    return when (metricName) {
        "Total Cost" -> stringResource(R.string.total_cost)
        "Total Distance" -> stringResource(R.string.distance_label)
        "Average Consumption" -> stringResource(R.string.avg_consumption)
        "Cost per km" -> stringResource(R.string.cost_per_km)
        else -> metricName
    }
}

@Composable
private fun ComparisonMetricCard(metric: ComparisonMetric) {
    StyledCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = getLocalizedMetricName(metric.name),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = metric.year1FormattedValue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                ChangeIndicator(
                    isIncrease = metric.isIncrease,
                    isImprovement = metric.isImprovement,
                    percentageChange = metric.percentageChange,
                    absoluteDifference = metric.absoluteDifference,
                    unit = metric.unit
                )
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = metric.year2FormattedValue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangeIndicator(
    isIncrease: Boolean,
    isImprovement: Boolean,
    percentageChange: Double,
    absoluteDifference: Double,
    unit: String
) {
    val color = when {
        isImprovement -> MaterialTheme.colorScheme.tertiary
        isIncrease -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.tertiary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(
                imageVector = if (isIncrease) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${if (isIncrease) "+" else ""}${String.format("%.1f", percentageChange)}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = "${if (absoluteDifference > 0) "+" else ""}${String.format("%.2f", absoluteDifference)} $unit",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MonthlyComparisonChart(data: YearlyComparisonData) {
    StyledCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.yearly_comparison_monthly_cost_breakdown),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.yearly_comparison_month),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = data.year1Data.year.toString(),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.End
                )
                Text(
                    text = data.year2Data.year.toString(),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.End
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val quarters = listOf(
                "Q1" to listOf(0, 1, 2),
                "Q2" to listOf(3, 4, 5),
                "Q3" to listOf(6, 7, 8),
                "Q4" to listOf(9, 10, 11)
            )
            quarters.forEach { (quarterName, monthIndices) ->
                val year1Total = monthIndices.sumOf { data.year1Data.monthlyCosts.getOrNull(it)?.totalCost ?: 0.0 }
                val year2Total = monthIndices.sumOf { data.year2Data.monthlyCosts.getOrNull(it)?.totalCost ?: 0.0 }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = quarterName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text(
                        text = "€${String.format(Locale.getDefault(), "%.0f", year1Total)}",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.End
                    )
                    Text(
                        text = "€${String.format(Locale.getDefault(), "%.0f", year2Total)}",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
