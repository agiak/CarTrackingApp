package com.agcoding.cartrackingapp.presentation.yearlycomparison

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text("Yearly Comparison") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is YearlyComparisonUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is YearlyComparisonUiState.InsufficientData -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
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
                            text = "Insufficient Data",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You need data from at least 2 different years to use yearly comparison",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
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
                    onYear1Click = { viewModel.showYear1Selector() },
                    onYear2Click = { viewModel.showYear2Selector() },
                    onYear1Selected = { viewModel.selectYear1(it) },
                    onYear2Selected = { viewModel.selectYear2(it) },
                    onDismissYear1Selector = { viewModel.hideYear1Selector() },
                    onDismissYear2Selector = { viewModel.hideYear2Selector() },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is YearlyComparisonUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun YearlyComparisonContent(
    data: YearlyComparisonData,
    availableYears: List<com.agcoding.cartrackingapp.domain.model.AvailableYear>,
    selectedYear1: Int?,
    selectedYear2: Int?,
    showYear1Selector: Boolean,
    showYear2Selector: Boolean,
    onYear1Click: () -> Unit,
    onYear2Click: () -> Unit,
    onYear1Selected: (Int) -> Unit,
    onYear2Selected: (Int) -> Unit,
    onDismissYear1Selector: () -> Unit,
    onDismissYear2Selector: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Year Selectors
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

        // Comparison Metrics
        items(data.metrics) { metric ->
            ComparisonMetricCard(metric = metric)
        }

        // Monthly Comparison Chart
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Monthly Trends",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            MonthlyComparisonChart(data = data)
        }
    }
}

@Composable
private fun YearSelectorRow(
    year1: Int,
    year2: Int,
    availableYears: List<com.agcoding.cartrackingapp.domain.model.AvailableYear>,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Year 1 Selector
            Box {
                YearSelector(
                    year = year1,
                    label = "Base Year",
                    onClick = onYear1Click,
                    isPrimary = true
                )

                DropdownMenu(
                    expanded = showYear1Selector,
                    onDismissRequest = onDismissYear1Selector
                ) {
                    availableYears.forEach { yearData ->
                        DropdownMenuItem(
                            text = { Text(yearData.year.toString()) },
                            onClick = { onYear1Selected(yearData.year) },
                            enabled = yearData.year != year2
                        )
                    }
                }
            }

            // VS Icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )

            // Year 2 Selector
            Box {
                YearSelector(
                    year = year2,
                    label = "Comparison Year",
                    onClick = onYear2Click,
                    isPrimary = false
                )

                DropdownMenu(
                    expanded = showYear2Selector,
                    onDismissRequest = onDismissYear2Selector
                ) {
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

@Composable
private fun YearSelector(
    year: Int,
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isPrimary) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = year.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
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
            // Metric Name
            Text(
                text = metric.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Values Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Year 1 Value
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = metric.year1FormattedValue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Change Indicator
                ChangeIndicator(
                    isIncrease = metric.isIncrease,
                    isImprovement = metric.isImprovement,
                    percentageChange = metric.percentageChange,
                    absoluteDifference = metric.absoluteDifference,
                    unit = metric.unit
                )

                // Year 2 Value
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
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
        isImprovement && !isIncrease -> Color(0xFF34C759) // Green for improvement (decrease)
        isImprovement && isIncrease -> Color(0xFF34C759) // Green for improvement (increase - not typical)
        isIncrease -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f) // Subtle warning for cost increase
        else -> Color(0xFF34C759) // Green for decrease
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
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
                text = "Monthly Cost Breakdown",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Month headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Month",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = data.year1Data.year.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
                Text(
                    text = data.year2Data.year.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly rows (showing quarters for brevity)
            val quarters = listOf(
                "Q1" to listOf(0, 1, 2),
                "Q2" to listOf(3, 4, 5),
                "Q3" to listOf(6, 7, 8),
                "Q4" to listOf(9, 10, 11)
            )

            quarters.forEach { (quarterName, monthIndices) ->
                val year1Total = monthIndices.sumOf {
                    data.year1Data.monthlyCosts.getOrNull(it)?.totalCost ?: 0.0
                }
                val year2Total = monthIndices.sumOf {
                    data.year2Data.monthlyCosts.getOrNull(it)?.totalCost ?: 0.0
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = quarterName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "€${String.format(Locale.getDefault(), "%.0f", year1Total)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                    Text(
                        text = "€${String.format(Locale.getDefault(), "%.0f", year2Total)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        }
    }
}

