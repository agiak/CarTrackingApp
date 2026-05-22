package com.agcoding.cartrackingapp.presentation.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.ActiveFilter
import com.agcoding.cartrackingapp.presentation.components.ActiveFiltersRow
import com.agcoding.cartrackingapp.presentation.components.ExpenseItemCard
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.transactions.model.Transaction
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onTransactionClick: (Transaction) -> Unit,
    onAddRefill: (carId: Long) -> Unit = {},
    onAddExpense: (carId: Long) -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val cars by viewModel.cars.collectAsStateWithLifecycle()
    val hasAnyTransactions by viewModel.hasAnyTransactions.collectAsStateWithLifecycle()
    val defaultCarId by viewModel.defaultCarId.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
    val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
    val useSplitView = isTablet || isLandscape

    // Helper function to create active filter chips
    val getActiveFilters: @Composable () -> List<ActiveFilter> = {
        val filters = mutableListOf<ActiveFilter>()

        // Sort option - show if not default (DATE_NEWEST)
        if (sortOption != SortOption.DATE_NEWEST) {
            val sortLabel = when (sortOption) {
                SortOption.DATE_NEWEST -> "" // Default, won't be shown
                SortOption.DATE_OLDEST -> stringResource(R.string.sort_date_oldest)
                SortOption.COST_HIGHEST -> stringResource(R.string.sort_cost_highest)
                SortOption.COST_LOWEST -> stringResource(R.string.sort_cost_lowest)
            }
            filters.add(ActiveFilter("sort", sortLabel) {
                viewModel.setSortOption(SortOption.DATE_NEWEST) // Reset to default
            })
        }

        // Type filters - only add if not both selected
        if (!filter.showRefills && filter.showExpenses) {
            filters.add(ActiveFilter("expenses", stringResource(R.string.services)) {
                viewModel.toggleExpenseFilter()
            })
        } else if (filter.showRefills && !filter.showExpenses) {
            filters.add(ActiveFilter("refills", stringResource(R.string.refills)) {
                viewModel.toggleRefillFilter()
            })
        }

        // Car filters
        filter.selectedCarIds.forEach { carId ->
            val car = cars.find { it.id == carId }
            if (car != null) {
                filters.add(ActiveFilter("car_$carId", car.name) {
                    viewModel.toggleCarSelection(carId)
                })
            }
        }

        filters
    }

    val activeFilters = getActiveFilters()
    val hasActiveFilters = activeFilters.isNotEmpty()
    val hasNonDefaultSort = sortOption != SortOption.DATE_NEWEST

    // Calculate active filter count excluding sort (for filter button badge)
    val filterOnlyCount = activeFilters.count { it.id != "sort" }
    val hasNonSortFilters = filterOnlyCount > 0

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.nav_transactions)) },
                actions = {
                    if (transactions.isNotEmpty()) {
                        // Sort button with badge indicator
                        BadgedBox(
                            badge = {
                                if (hasNonDefaultSort) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        ) {
                            IconButton(onClick = { showSortSheet = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = stringResource(R.string.sort),
                                    tint = if (hasNonDefaultSort) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }

                        // Filter button with badge indicator
                        BadgedBox(
                            badge = {
                                if (hasNonSortFilters) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(
                                            text = filterOnlyCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = stringResource(R.string.filter),
                                    tint = if (hasNonSortFilters) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            if (!hasAnyTransactions) R.string.no_transactions_yet
                            else R.string.no_transactions_found
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!hasAnyTransactions && defaultCarId != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = { defaultCarId?.let { onAddRefill(it) } }) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(stringResource(R.string.refill_widget_label))
                        }
                        OutlinedButton(onClick = { defaultCarId?.let { onAddExpense(it) } }) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(stringResource(R.string.expense_widget_label))
                        }
                    }
                }
            }
        } else if (useSplitView) {
            // Split view for tablets and landscape
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left side: Filters and sort (35%)
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Transaction type filter
                    StyledCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.transaction_type),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = filter.showRefills,
                                    onClick = viewModel::toggleRefillFilter,
                                    label = { Text(stringResource(R.string.refills)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocalGasStation,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                FilterChip(
                                    selected = filter.showExpenses,
                                    onClick = viewModel::toggleExpenseFilter,
                                    label = { Text(stringResource(R.string.services)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Car filter
                    if (cars.isNotEmpty()) {
                        StyledCard {
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
                                        text = stringResource(R.string.filter_by_car),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (filter.selectedCarIds.isNotEmpty()) {
                                        Text(
                                            text = stringResource(R.string.clear),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable { viewModel.clearCarFilter() }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    cars.forEach { car ->
                                        FilterChip(
                                            selected = car.id in filter.selectedCarIds,
                                            onClick = { viewModel.toggleCarSelection(car.id) },
                                            label = { Text(car.name) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sort options
                    StyledCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.sort_by),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            SortOption(
                                text = stringResource(R.string.sort_date_newest),
                                isSelected = sortOption == SortOption.DATE_NEWEST,
                                onClick = { viewModel.setSortOption(SortOption.DATE_NEWEST) }
                            )

                            SortOption(
                                text = stringResource(R.string.sort_date_oldest),
                                isSelected = sortOption == SortOption.DATE_OLDEST,
                                onClick = { viewModel.setSortOption(SortOption.DATE_OLDEST) }
                            )

                            SortOption(
                                text = stringResource(R.string.sort_cost_highest),
                                isSelected = sortOption == SortOption.COST_HIGHEST,
                                onClick = { viewModel.setSortOption(SortOption.COST_HIGHEST) }
                            )

                            SortOption(
                                text = stringResource(R.string.sort_cost_lowest),
                                isSelected = sortOption == SortOption.COST_LOWEST,
                                onClick = { viewModel.setSortOption(SortOption.COST_LOWEST) }
                            )
                        }
                    }
                }

                // Right side: Transactions list (65%)
                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                ) {
                    // Active filters row at the top
                    ActiveFiltersRow(
                        activeFilters = activeFilters,
                        onClearAll = if (activeFilters.size > 1) { { viewModel.clearAllFilters() } } else null,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Transactions list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    items(
                        items = transactions,
                        key = { "${it.transaction.type.name}_${it.transaction.id}" }
                    ) { transactionData ->
                        when (transactionData) {
                            is TransactionWithData.RefillTransaction -> {
                                RefillItemCard(
                                    refill = transactionData.refill,
                                    carName = transactionData.carName,
                                    onClick = { onTransactionClick(transactionData.transaction) }
                                )
                            }
                            is TransactionWithData.ExpenseTransaction -> {
                                ExpenseItemCard(
                                    expense = transactionData.expense,
                                    carName = transactionData.carName,
                                    onClick = { onTransactionClick(transactionData.transaction) }
                                )
                            }
                        }
                    }
                    }
                }
            }
        } else {
            // Original single column layout for portrait phones
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Active filters row
                ActiveFiltersRow(
                    activeFilters = activeFilters,
                    onClearAll = if (activeFilters.size > 1) { { viewModel.clearAllFilters() } } else null
                )

                // Transactions list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                items(
                    items = transactions,
                    key = { "${it.transaction.type.name}_${it.transaction.id}" }
                ) { transactionData ->
                    when (transactionData) {
                        is TransactionWithData.RefillTransaction -> {
                            RefillItemCard(
                                refill = transactionData.refill,
                                carName = transactionData.carName,
                                onClick = { onTransactionClick(transactionData.transaction) }
                            )
                        }
                        is TransactionWithData.ExpenseTransaction -> {
                            ExpenseItemCard(
                                expense = transactionData.expense,
                                carName = transactionData.carName,
                                onClick = { onTransactionClick(transactionData.transaction) }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Sort bottom sheet (only for portrait phones)
    if (showSortSheet && !useSplitView) {
        SortBottomSheet(
            currentSort = sortOption,
            onSortSelected = {
                viewModel.setSortOption(it)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }

    // Filter bottom sheet (only for portrait phones)
    if (showFilterSheet && !useSplitView) {
        FilterBottomSheet(
            filter = filter,
            cars = cars,
            onToggleRefills = viewModel::toggleRefillFilter,
            onToggleExpenses = viewModel::toggleExpenseFilter,
            onToggleCarSelection = viewModel::toggleCarSelection,
            onClearCarFilter = viewModel::clearCarFilter,
            onDismiss = { showFilterSheet = false }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortBottomSheet(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.sort_by),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SortOption(
                text = stringResource(R.string.sort_date_newest),
                isSelected = currentSort == SortOption.DATE_NEWEST,
                onClick = {
                    scope.launch {
                        onSortSelected(SortOption.DATE_NEWEST)
                        sheetState.hide()
                    }
                }
            )

            SortOption(
                text = stringResource(R.string.sort_date_oldest),
                isSelected = currentSort == SortOption.DATE_OLDEST,
                onClick = {
                    scope.launch {
                        onSortSelected(SortOption.DATE_OLDEST)
                        sheetState.hide()
                    }
                }
            )

            SortOption(
                text = stringResource(R.string.sort_cost_highest),
                isSelected = currentSort == SortOption.COST_HIGHEST,
                onClick = {
                    scope.launch {
                        onSortSelected(SortOption.COST_HIGHEST)
                        sheetState.hide()
                    }
                }
            )

            SortOption(
                text = stringResource(R.string.sort_cost_lowest),
                isSelected = currentSort == SortOption.COST_LOWEST,
                onClick = {
                    scope.launch {
                        onSortSelected(SortOption.COST_LOWEST)
                        sheetState.hide()
                    }
                }
            )
        }
    }
}

@Composable
private fun SortOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterBottomSheet(
    filter: TransactionFilter,
    cars: List<com.agcoding.cartrackingapp.domain.model.Car>,
    onToggleRefills: () -> Unit,
    onToggleExpenses: () -> Unit,
    onToggleCarSelection: (Long) -> Unit,
    onClearCarFilter: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.filter_transactions),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Transaction type filter
            Text(
                text = stringResource(R.string.transaction_type),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter.showRefills,
                    onClick = onToggleRefills,
                    label = { Text(stringResource(R.string.refills)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                FilterChip(
                    selected = filter.showExpenses,
                    onClick = onToggleExpenses,
                    label = { Text(stringResource(R.string.services)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Car filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filter_by_car),
                    style = MaterialTheme.typography.titleMedium
                )

                if (filter.selectedCarIds.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.clear),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onClearCarFilter() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cars.forEach { car ->
                    FilterChip(
                        selected = car.id in filter.selectedCarIds,
                        onClick = { onToggleCarSelection(car.id) },
                        label = { Text(car.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }
    }
}
