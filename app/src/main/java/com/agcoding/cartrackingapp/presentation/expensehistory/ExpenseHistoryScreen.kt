package com.agcoding.cartrackingapp.presentation.expensehistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.ActiveFilter
import com.agcoding.cartrackingapp.presentation.components.ActiveFiltersRow
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.expensehistory.components.ExpenseHistoryContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHistoryScreen(
    onNavigateBack: () -> Unit,
    onExpenseClick: (Long) -> Unit,
    viewModel: com.agcoding.cartrackingapp.presentation.expensehistory.ExpenseHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val hasDateFilter = startDate != null || endDate != null
    val hasNonDefaultSort = sortOption != ExpenseSortOption.MOST_RECENT
    val hasAnyFilter = hasNonDefaultSort || hasDateFilter

    val activeFilters: List<ActiveFilter> = buildList {
        if (hasNonDefaultSort) add(ActiveFilter(
            id = "sort",
            label = stringResource(sortOption.displayNameResId),
            onRemove = { viewModel.setSortOption(ExpenseSortOption.MOST_RECENT) }
        ))
        if (startDate != null) add(ActiveFilter(
            id = "start",
            label = stringResource(R.string.date_filter_from_label, dateFormat.format(Date(startDate!!))),
            onRemove = { viewModel.setStartDate(null) }
        ))
        if (endDate != null) add(ActiveFilter(
            id = "end",
            label = stringResource(R.string.date_filter_to_label, dateFormat.format(Date(endDate!!))),
            onRemove = { viewModel.setEndDate(null) }
        ))
    }

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.expense_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Date range filter button
                    BadgedBox(
                        badge = {
                            if (hasDateFilter) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                    ) {
                        IconButton(onClick = { showDateRangePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = stringResource(R.string.filter_by_date_cd),
                                tint = if (hasDateFilter) MaterialTheme.colorScheme.secondary
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = stringResource(R.string.sort),
                                tint = if (hasNonDefaultSort) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        ExpenseSortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.displayNameResId)) },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    showSortMenu = false
                                },
                                trailingIcon = if (sortOption == option) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ExpenseHistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ExpenseHistoryUiState.Success -> {
                val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
                val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
                val useSplitView = isTablet || isLandscape

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Active filter chips (sort + date range)
                    ActiveFiltersRow(
                        activeFilters = activeFilters,
                        onClearAll = if (activeFilters.size > 1) {
                            {
                                viewModel.setSortOption(ExpenseSortOption.MOST_RECENT)
                                viewModel.clearDateFilter()
                            }
                        } else null
                    )

                    // Expense list
                    ExpenseHistoryContent(
                        expenses = state.expenses,
                        availableCategories = state.availableCategories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = viewModel::setSelectedCategory,
                        sortOptionName = stringResource(sortOption.displayNameResId),
                        onExpenseClick = onExpenseClick,
                        useSplitView = useSplitView,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            is ExpenseHistoryUiState.EmptyFilter -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ActiveFiltersRow(
                        activeFilters = activeFilters,
                        onClearAll = if (activeFilters.size > 1) {
                            {
                                viewModel.setSortOption(ExpenseSortOption.MOST_RECENT)
                                viewModel.clearDateFilter()
                            }
                        } else null
                    )
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.no_expenses_for_filter),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.adjust_or_clear_filter),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            is ExpenseHistoryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Date range picker
    if (showDateRangePicker) {
        val rangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate,
            initialSelectedEndDateMillis = endDate
        )
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setStartDate(rangePickerState.selectedStartDateMillis)
                        viewModel.setEndDate(rangePickerState.selectedEndDateMillis)
                        showDateRangePicker = false
                    },
                    enabled = rangePickerState.selectedStartDateMillis != null
                ) { Text(stringResource(R.string.apply)) }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DateRangePicker(
                state = rangePickerState,
                modifier = Modifier.fillMaxHeight(0.85f)
            )
        }
    }
}

