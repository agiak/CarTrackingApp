@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.carstatistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.cardetails.components.PeriodStatisticsCard
import com.agcoding.cartrackingapp.presentation.components.DateFilterSheet
import com.agcoding.cartrackingapp.presentation.components.ExpenseItemCard
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.cartransactions.CarTransactionsViewModel
import com.agcoding.cartrackingapp.presentation.transactions.SortBottomSheet
import com.agcoding.cartrackingapp.presentation.transactions.TransactionTypeFilterSheet
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData

/**
 * Statistics for one car, reached by tapping its card in the per-car breakdown on the
 * statistics screen.
 *
 * Leads with the totals for the selected year/month — picked through the same shared
 * date filter used everywhere — and follows with that period's transactions, which can
 * additionally be filtered by type and re-sorted.
 */
@Composable
fun CarStatisticsScreen(
    onNavigateBack: () -> Unit,
    onRefillClick: (Long) -> Unit = {},
    onExpenseClick: (Long) -> Unit = {},
    viewModel: CarTransactionsViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val listFilter by viewModel.listFilter.collectAsStateWithLifecycle()
    val periodStatistics by viewModel.periodStatistics.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val car by viewModel.car.collectAsStateWithLifecycle()

    var showDateSheet by remember { mutableStateOf(false) }
    var showTypeFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = {
                    Column {
                        Text(
                            text = car?.name ?: stringResource(R.string.car_statistics_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val plate = car?.licensePlate.orEmpty()
                        if (plate.isNotBlank()) {
                            Text(
                                text = plate,
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
                    IconButton(onClick = { showTypeFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter),
                            tint = if (listFilter.hasTypeFilter) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.sort_by),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Totals for the selected period — the date chip in its header opens the
            // shared year/month picker.
            item(key = "period_stats") {
                PeriodStatisticsCard(
                    statistics = periodStatistics,
                    onDateFilterClick = { showDateSheet = true }
                )
            }

            item(key = "transactions_header") {
                Text(
                    text = stringResource(R.string.transactions_section_header, transactions.size),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (transactions.isEmpty()) {
                item(key = "transactions_empty") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_transactions_for_filters),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = transactions,
                    key = { entry -> "${entry.transaction.type}_${entry.transaction.id}" }
                ) { entry ->
                    when (entry) {
                        is TransactionWithData.RefillTransaction -> RefillItemCard(
                            refill = entry.refill,
                            carName = null,
                            onClick = { onRefillClick(entry.refill.id) }
                        )

                        is TransactionWithData.ExpenseTransaction -> ExpenseItemCard(
                            expense = entry.expense,
                            carName = null,
                            onClick = { onExpenseClick(entry.expense.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDateSheet) {
        DateFilterSheet(
            selected = listFilter.dateFilter,
            availableYears = availableYears,
            onFilterChange = viewModel::setDateFilter,
            onDismiss = { showDateSheet = false }
        )
    }

    if (showTypeFilterSheet) {
        TransactionTypeFilterSheet(
            filter = listFilter,
            onToggleRefills = viewModel::toggleRefillFilter,
            onToggleExpenses = viewModel::toggleExpenseFilter,
            onDismiss = { showTypeFilterSheet = false }
        )
    }

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = listFilter.sortOption,
            onSortSelected = viewModel::setSortOption,
            onDismiss = { showSortSheet = false }
        )
    }
}
