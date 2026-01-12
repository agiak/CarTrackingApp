package com.agcoding.cartrackingapp.presentation.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.domain.model.MonthlyTrend

enum class MonthlyTrendsFilter(val label: String, val months: Int?) {
    LAST_3_MONTHS("Last 3 months", 3),
    LAST_6_MONTHS("Last 6 months", 6),
    LAST_YEAR("Last year", 12),
    ALL_TIME("All time", null)
}

enum class MonthlyTrendsSortBy(val label: String) {
    TIME("Time"),
    COST("Cost"),
    DISTANCE("Distance"),
    TRANSACTIONS("Transactions")
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyTrendsScreen(
    onNavigateBack: () -> Unit,
    onMonthClick: (month: Int, year: Int) -> Unit = { _, _ -> },
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(MonthlyTrendsFilter.ALL_TIME) }
    var selectedSortBy by remember { mutableStateOf(MonthlyTrendsSortBy.TIME) }
    var sortOrder by remember { mutableStateOf(SortOrder.DESCENDING) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Trends") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is StatisticsUiState.Loading -> {
                // Loading state
            }

            is StatisticsUiState.Success -> {
                val allTrends = state.statistics.monthlyTrends
                val filteredTrends = filterTrends(allTrends, selectedFilter)
                val sortedTrends = sortTrends(filteredTrends, selectedSortBy, sortOrder)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Time filter chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(MonthlyTrendsFilter.entries) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // Sort controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort by",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sort by dropdown
                            androidx.compose.foundation.layout.Box {
                                OutlinedButton(
                                    onClick = { showSortMenu = true },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(selectedSortBy.label)
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    MonthlyTrendsSortBy.entries.forEach { sortBy ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = sortBy.label,
                                                    fontWeight = if (selectedSortBy == sortBy)
                                                        FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (selectedSortBy == sortBy)
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                selectedSortBy = sortBy
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Sort order toggle
                            IconButton(
                                onClick = {
                                    sortOrder = if (sortOrder == SortOrder.ASCENDING)
                                        SortOrder.DESCENDING else SortOrder.ASCENDING
                                }
                            ) {
                                Icon(
                                    imageVector = if (sortOrder == SortOrder.DESCENDING)
                                        Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = if (sortOrder == SortOrder.DESCENDING)
                                        "Descending" else "Ascending",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Scrollable content with summary and items
                    if (sortedTrends.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No data available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Add some refills or expenses to see monthly trends",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Summary card as first item (scrolls with list)
                            item {
                                FilteredSummaryCard(sortedTrends, selectedFilter)
                            }

                            // Monthly trend items
                            items(sortedTrends) { trend ->
                                MonthlyTrendItem(
                                    trend = trend,
                                    onClick = { onMonthClick(trend.month, trend.year) }
                                )
                            }
                        }
                    }
                }
            }

            is StatisticsUiState.Error -> {
                // Error state
            }
        }
    }
}

private fun filterTrends(trends: List<MonthlyTrend>, filter: MonthlyTrendsFilter): List<MonthlyTrend> {
    return when (filter.months) {
        null -> trends // All time
        else -> trends.take(filter.months)
    }
}


private fun sortTrends(
    trends: List<MonthlyTrend>,
    sortBy: MonthlyTrendsSortBy,
    order: SortOrder
): List<MonthlyTrend> {
    val sorted = when (sortBy) {
        MonthlyTrendsSortBy.TIME -> trends.sortedBy { it.year * 100 + it.month }
        MonthlyTrendsSortBy.COST -> trends.sortedBy { it.totalCombinedCost }
        MonthlyTrendsSortBy.DISTANCE -> trends.sortedBy { it.totalDistance }
        MonthlyTrendsSortBy.TRANSACTIONS -> trends.sortedBy { it.refillCount + it.expenseCount }
    }

    return if (order == SortOrder.DESCENDING) sorted.reversed() else sorted
}

@Composable
private fun FilteredSummaryCard(trends: List<MonthlyTrend>, filter: MonthlyTrendsFilter) {
    val totalRefillCost = trends.sumOf { it.totalCost }
    val totalExpenseCost = trends.sumOf { it.expenseCost }
    val totalCombinedCost = trends.sumOf { it.totalCombinedCost }
    val totalLiters = trends.sumOf { it.totalLiters }
    val totalDistance = trends.sumOf { it.totalDistance }
    val avgConsumption = if (totalDistance > 0) (totalLiters / totalDistance) * 100 else 0.0
    val totalRefills = trends.sumOf { it.refillCount }
    val totalExpenses = trends.sumOf { it.expenseCount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${filter.label} Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total combined cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Spending",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "€${String.format("%.2f", totalCombinedCost)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cost breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(label = "Fuel Cost", value = "€${String.format("%.2f", totalRefillCost)}")
                SummaryItem(label = "Expenses", value = "€${String.format("%.2f", totalExpenseCost)}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(label = "Total Fuel", value = "${String.format("%.1f", totalLiters)} L")
                SummaryItem(label = "Distance", value = "${String.format("%.0f", totalDistance)} km")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(label = "Avg Consumption", value = "${String.format("%.1f", avgConsumption)} L/100km")
                SummaryItem(label = "Transactions", value = "${totalRefills + totalExpenses}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$totalRefills refills, $totalExpenses expenses in ${trends.size} months",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MonthlyTrendItem(
    trend: MonthlyTrend,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${trend.monthName} ${trend.year}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "€${String.format("%.2f", trend.totalCombinedCost)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fuel vs Expenses breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fuel: €${String.format("%.2f", trend.totalCost)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                if (trend.expenseCost > 0) {
                    Text(
                        text = "Expenses: €${String.format("%.2f", trend.expenseCost)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${trend.refillCount} refills",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format("%.1f", trend.totalLiters)} L",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format("%.0f", trend.totalDistance)} km",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (trend.averageConsumption > 0) {
                    Text(
                        text = "${String.format("%.1f", trend.averageConsumption)} L/100km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (trend.expenseCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${trend.expenseCount} expense${if (trend.expenseCount > 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

