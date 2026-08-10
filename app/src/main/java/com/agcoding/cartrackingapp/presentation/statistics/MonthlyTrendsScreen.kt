package com.agcoding.cartrackingapp.presentation.statistics

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.MonthlyTrend
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber

enum class MonthlyTrendsFilter(val labelRes: Int, val months: Int?) {
    LAST_3_MONTHS(R.string.filter_last_3_months, 3),
    LAST_6_MONTHS(R.string.filter_last_6_months, 6),
    LAST_YEAR(R.string.filter_last_year, 12),
    ALL_TIME(R.string.filter_all_time, null)
}

enum class MonthlyTrendsSortBy(val labelRes: Int) {
    TIME(R.string.sort_time),
    COST(R.string.sort_cost),
    DISTANCE(R.string.sort_distance),
    TRANSACTIONS(R.string.sort_transactions)
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
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.monthly_trends_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.app_name)
                        )
                    }
                },
                actions = {
                    // Show filter button only in portrait (split view has sidebar)
                    val configuration = LocalConfiguration.current
                    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
                    val useSplitView = configuration.screenWidthDp >= 600 || isLandscape
                    if (!useSplitView) {
                        val hasActiveFilter = selectedFilter != MonthlyTrendsFilter.ALL_TIME
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = stringResource(R.string.filter_label),
                                tint = if (hasActiveFilter) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is StatisticsUiState.Loading -> { /* Loading state */ }

            is StatisticsUiState.Success -> {
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
                val useSplitView = configuration.screenWidthDp >= 600 || isLandscape

                val allTrends = state.statistics.monthlyTrends
                val filteredTrends = filterTrends(allTrends, selectedFilter)
                val sortedTrends = sortTrends(filteredTrends, selectedSortBy, sortOrder)

                if (useSplitView) {
                    // Split view: sidebar with filter/sort controls + right list
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.35f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Time filter card
                            StyledCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.filter_time_period),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MonthlyTrendsFilter.entries.forEach { filter ->
                                            FilterChip(
                                                selected = selectedFilter == filter,
                                                onClick = { selectedFilter = filter },
                                                label = { Text(stringResource(filter.labelRes)) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }

                            // Sort controls card
                            StyledCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.sort_by_label),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MonthlyTrendsSortBy.entries.forEach { sortBy ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedSortBy = sortBy }
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = stringResource(sortBy.labelRes),
                                                    fontWeight = if (selectedSortBy == sortBy) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (selectedSortBy == sortBy) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (selectedSortBy == sortBy) {
                                                    Icon(
                                                        imageVector = if (sortOrder == SortOrder.DESCENDING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            sortOrder = if (sortOrder == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = if (sortOrder == SortOrder.DESCENDING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            if (sortOrder == SortOrder.DESCENDING) stringResource(R.string.sort_order_descending)
                                            else stringResource(R.string.sort_order_ascending)
                                        )
                                    }
                                }
                            }

                            if (sortedTrends.isNotEmpty()) {
                                FilteredSummaryCard(sortedTrends, selectedFilter)
                            }
                        }

                        if (sortedTrends.isEmpty()) {
                            EmptyTrendsState(modifier = Modifier.weight(0.65f).fillMaxHeight())
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(0.65f).fillMaxHeight(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(sortedTrends) { trend ->
                                    MonthlyTrendItem(trend = trend, onClick = { onMonthClick(trend.month, trend.year) })
                                }
                            }
                        }
                    }
                } else {
                    // Portrait: full-width list, filter in bottom sheet
                    if (sortedTrends.isEmpty()) {
                        EmptyTrendsState(
                            modifier = Modifier.fillMaxSize().padding(paddingValues)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(paddingValues),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { FilteredSummaryCard(sortedTrends, selectedFilter) }
                            items(sortedTrends) { trend ->
                                MonthlyTrendItem(trend = trend, onClick = { onMonthClick(trend.month, trend.year) })
                            }
                        }
                    }
                }
            }

            is StatisticsUiState.Error -> { /* Error state */ }
        }

        // Filter bottom sheet (portrait only)
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState
            ) {
                MonthlyTrendsFilterSheet(
                    selectedFilter = selectedFilter,
                    selectedSortBy = selectedSortBy,
                    sortOrder = sortOrder,
                    onFilterSelected = { selectedFilter = it },
                    onSortBySelected = { selectedSortBy = it },
                    onSortOrderToggled = {
                        sortOrder = if (sortOrder == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
                    },
                    onDone = { showFilterSheet = false }
                )
            }
        }
    }
}

@Composable
private fun MonthlyTrendsFilterSheet(
    selectedFilter: MonthlyTrendsFilter,
    selectedSortBy: MonthlyTrendsSortBy,
    sortOrder: SortOrder,
    onFilterSelected: (MonthlyTrendsFilter) -> Unit,
    onSortBySelected: (MonthlyTrendsSortBy) -> Unit,
    onSortOrderToggled: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.filter_label),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onDone) {
                Text(stringResource(R.string.done))
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Time Period section
        Text(
            text = stringResource(R.string.filter_time_period),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        MonthlyTrendsFilter.entries.forEach { filter ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = filter == selectedFilter,
                    onClick = { onFilterSelected(filter) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(filter.labelRes),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Sort By section
        Text(
            text = stringResource(R.string.sort_by_label),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        MonthlyTrendsSortBy.entries.forEach { sortBy ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSortBySelected(sortBy) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = sortBy == selectedSortBy,
                    onClick = { onSortBySelected(sortBy) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(sortBy.labelRes),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (sortBy == selectedSortBy) {
                    Icon(
                        imageVector = if (sortOrder == SortOrder.DESCENDING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Sort Order section
        Text(
            text = stringResource(R.string.sort_order_label),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        SortOrder.entries.forEach { order ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (order != sortOrder) onSortOrderToggled() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = order == sortOrder,
                    onClick = { if (order != sortOrder) onSortOrderToggled() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (order == SortOrder.DESCENDING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (order == sortOrder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (order == SortOrder.DESCENDING) stringResource(R.string.sort_order_descending)
                           else stringResource(R.string.sort_order_ascending),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun EmptyTrendsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_data_available),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.add_refills_or_expenses),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun filterTrends(trends: List<MonthlyTrend>, filter: MonthlyTrendsFilter): List<MonthlyTrend> {
    return when (filter.months) {
        null -> trends
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

    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = stringResource(R.string.summary_label_format, stringResource(filter.labelRes)),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.total_spending), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = totalCombinedCost.formatMoney(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryItem(label = stringResource(R.string.fuel_cost), value = totalRefillCost.formatMoney())
                SummaryItem(label = stringResource(R.string.expenses_cost), value = totalExpenseCost.formatMoney())
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryItem(label = stringResource(R.string.total_fuel), value = "${totalLiters.formatNumber(1)} L")
                SummaryItem(label = stringResource(R.string.distance_label), value = "${totalDistance.formatNumber(0)} km")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryItem(label = stringResource(R.string.avg_consumption), value = "${avgConsumption.formatNumber(1)} L/100km")
                SummaryItem(label = stringResource(R.string.transactions_label), value = "${totalRefills + totalExpenses}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.refills_count_format, totalRefills) + ", " +
                       stringResource(R.string.expenses_count_format, totalExpenses) + " in ${trends.size} " +
                       stringResource(R.string.transactions_label).lowercase(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MonthlyTrendItem(
    trend: MonthlyTrend,
    onClick: () -> Unit
) {
    StyledCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${trend.monthName} ${trend.year}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = trend.totalCombinedCost.formatMoney(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "${stringResource(R.string.fuel)}: ${trend.totalCost.formatMoney()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                if (trend.expenseCost > 0) {
                    Text(text = "${stringResource(R.string.expenses)}: ${trend.expenseCost.formatMoney()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.refills_count_format, trend.refillCount), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${trend.totalLiters.formatNumber(1)} L", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${trend.totalDistance.formatNumber(0)} km", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (trend.averageConsumption > 0) {
                    Text(text = "${trend.averageConsumption.formatNumber(1)} L/100km", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (trend.expenseCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stringResource(R.string.expenses_count_format, trend.expenseCount), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
