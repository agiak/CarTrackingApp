package com.agcoding.cartrackingapp.presentation.refillhistory

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.ActiveFilter
import com.agcoding.cartrackingapp.presentation.components.ActiveFiltersRow
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.refillhistory.components.RefillHistoryContent
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class RefillSortOption(@StringRes val labelRes: Int) {
    MOST_RECENT(R.string.most_recent),
    OLDEST(R.string.oldest),
    MOST_EXPENSIVE(R.string.most_expensive),
    LEAST_EXPENSIVE(R.string.least_expensive),
    BEST_CONSUMPTION(R.string.best_consumption),
    WORST_CONSUMPTION(R.string.worst_consumption)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefillHistoryScreen(
    onNavigateBack: () -> Unit,
    onRefillClick: (Long) -> Unit,
    onCreateTripClick: () -> Unit = {},
    viewModel: RefillHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedRefillIds by viewModel.selectedRefillIds.collectAsState()
    val availableTrips by viewModel.availableTrips.collectAsState()
    val refillTripNames by viewModel.refillTripNames.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showAddToTripDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val hasNonDefaultSort = selectedSort != RefillSortOption.MOST_RECENT
    val hasDateFilter = startDate != null || endDate != null

    val activeFilters: List<ActiveFilter> = buildList {
        if (hasNonDefaultSort) add(ActiveFilter(
            id = "sort",
            label = stringResource(selectedSort.labelRes),
            onRemove = { viewModel.setSortOption(RefillSortOption.MOST_RECENT) }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                // Selection mode top bar
                StyledTopAppBar(
                    title = { Text(stringResource(R.string.selection_mode_count, selectedRefillIds.size)) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear_selection_cd)
                            )
                        }
                    },
                    actions = {
                        // Delete selected
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            enabled = selectedRefillIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_selected_cd),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        // New trip from selection
                        TextButton(
                            onClick = {
                                viewModel.clearSelection()
                                onCreateTripClick()
                            },
                            enabled = selectedRefillIds.isNotEmpty()
                        ) {
                            Text(stringResource(R.string.new_trip))
                        }
                        // Add to existing trip
                        TextButton(
                            onClick = { showAddToTripDialog = true },
                            enabled = selectedRefillIds.isNotEmpty()
                        ) {
                            Text(stringResource(R.string.add_to_existing_trip))
                        }
                    }
                )
            } else {
                // Normal top bar
                StyledTopAppBar(
                    title = { Text(stringResource(R.string.refill_history_title)) },
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
                        Box {
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
                                RefillSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(option.labelRes),
                                                fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedSort == option)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOption(option)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is RefillHistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RefillHistoryUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ActiveFiltersRow(
                        activeFilters = activeFilters,
                        onClearAll = if (activeFilters.size > 1) {
                            {
                                viewModel.setSortOption(RefillSortOption.MOST_RECENT)
                                viewModel.clearDateFilter()
                            }
                        } else null
                    )

                    // Refills list
                    RefillHistoryContent(
                        carName = state.carName,
                        refills = state.refills,
                        selectedSort = selectedSort,
                        onRefillClick = { refillId ->
                            if (isSelectionMode) {
                                viewModel.toggleRefillSelection(refillId)
                            } else {
                                onRefillClick(refillId)
                            }
                        },
                        onRefillLongClick = { refillId ->
                            viewModel.onRefillLongPress(refillId)
                        },
                        isSelectionMode = isSelectionMode,
                        selectedRefillIds = selectedRefillIds,
                        refillTripNames = refillTripNames,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            is RefillHistoryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is RefillHistoryUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_refills_yet),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.add_first_refill),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is RefillHistoryUiState.EmptyFilter -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ActiveFiltersRow(
                        activeFilters = activeFilters,
                        onClearAll = if (activeFilters.size > 1) {
                            {
                                viewModel.setSortOption(RefillSortOption.MOST_RECENT)
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
                                text = stringResource(R.string.no_refills_for_filter),
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
        }
    }

    // Date range picker
    if (showDateRangePicker) {
        val rangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate,
            initialSelectedEndDateMillis = endDate
        )
        // A DateRangePicker needs a bounded height to lay out its month grid, so it
        // must live in a full-screen dialog with the picker taking the remaining
        // space (weight(1f)). Nesting it in DatePickerDialog (sized for a single
        // date) collapses the calendar to just its header.
        Dialog(
            onDismissRequest = { showDateRangePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showDateRangePicker = false }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.cancel)
                            )
                        }
                        TextButton(
                            onClick = {
                                viewModel.setStartDate(rangePickerState.selectedStartDateMillis)
                                viewModel.setEndDate(rangePickerState.selectedEndDateMillis)
                                showDateRangePicker = false
                            },
                            enabled = rangePickerState.selectedStartDateMillis != null
                        ) { Text(stringResource(R.string.apply)) }
                    }
                    // The stock headline ("Start date - End date") wraps one
                    // character per line in Greek once the mode-toggle icon takes
                    // its share of the width. Use a compact single-line headline
                    // showing the actual range, and drop the toggle — this dialog
                    // already has its own Close / Apply row.
                    DateRangePicker(
                        state = rangePickerState,
                        modifier = Modifier.weight(1f),
                        title = {
                            Text(
                                text = stringResource(R.string.select_time_period),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                            )
                        },
                        headline = {
                            val start = rangePickerState.selectedStartDateMillis
                            val end = rangePickerState.selectedEndDateMillis
                            Text(
                                text = when {
                                    start != null && end != null ->
                                        "${dateFormat.format(Date(start))} – ${dateFormat.format(Date(end))}"
                                    start != null -> dateFormat.format(Date(start))
                                    else -> stringResource(R.string.select_date)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                            )
                        },
                        showModeToggle = false
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(R.string.delete_refill_title)) },
            text = { Text(stringResource(R.string.delete_refill_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteSelectedRefills(
                            onSuccess = { count ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Deleted $count refill(s)")
                                }
                            },
                            onError = { error ->
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            }
                        )
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Add to Trip Dialog
    if (showAddToTripDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddToTripDialog = false },
            title = { Text(stringResource(R.string.add_to_existing_trip)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.select_trip_for_refills, selectedRefillIds.size))

                    if (availableTrips.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_trips_available),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableTrips.forEach { trip ->
                                androidx.compose.material3.OutlinedButton(
                                    onClick = {
                                        viewModel.addSelectedToTrip(
                                            tripId = trip.id,
                                            onSuccess = {
                                                showAddToTripDialog = false
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        "Added ${selectedRefillIds.size} refill(s) to ${trip.name}"
                                                    )
                                                }
                                            },
                                            onError = { error ->
                                                showAddToTripDialog = false
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(error)
                                                }
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = trip.name,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (trip.description != null) {
                                            Text(
                                                text = trip.description,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${trip.refills.size} refills",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Create Trip button - always visible, on the left
                    TextButton(
                        onClick = {
                            showAddToTripDialog = false
                            viewModel.clearSelection()
                            onCreateTripClick()
                        }
                    ) {
                        Text(stringResource(R.string.create_trip))
                    }
                    // Cancel button
                    TextButton(onClick = { showAddToTripDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            },
            dismissButton = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RefillHistoryScreenPreview() {
    CarTrackingAppTheme {
        RefillHistoryScreen(
            onNavigateBack = {},
            onRefillClick = {},
            onCreateTripClick = {}
        )
    }
}
