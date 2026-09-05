@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.cartransactions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
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
import com.agcoding.cartrackingapp.presentation.components.DateFilterButton
import com.agcoding.cartrackingapp.presentation.components.DateFilterSheet
import com.agcoding.cartrackingapp.presentation.components.ExpenseItemCard
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.transactions.SortBottomSheet
import com.agcoding.cartrackingapp.presentation.transactions.TransactionTypeFilterSheet
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData
import com.agcoding.cartrackingapp.util.formatMoney

/**
 * Every refill and expense for one car, with the same controls as the car details
 * screen: type filter, sorting, and the shared year/month date filter.
 */
@Composable
fun CarTransactionsScreen(
    onNavigateBack: () -> Unit,
    onRefillClick: (Long) -> Unit = {},
    onExpenseClick: (Long) -> Unit = {},
    viewModel: CarTransactionsViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val listFilter by viewModel.listFilter.collectAsStateWithLifecycle()
    val periodStatistics by viewModel.periodStatistics.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val carName by viewModel.carName.collectAsStateWithLifecycle()

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
                            text = stringResource(R.string.all_transactions),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (carName.isNotBlank()) {
                            Text(
                                text = carName,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date filter — identical control to the one on car details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateFilterButton(
                    filter = listFilter.dateFilter,
                    onClick = { showDateSheet = true }
                )
            }

            // Totals for the selected period
            StyledCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.transactions_section_header,
                            periodStatistics.transactionCount
                        ),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = periodStatistics.totalCost.formatMoney(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_transactions_for_filters),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
