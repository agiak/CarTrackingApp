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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.ActiveFilter
import com.agcoding.cartrackingapp.presentation.components.ActiveFiltersRow
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.refillhistory.components.RefillHistoryContent
import kotlinx.coroutines.launch

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

    var showSortMenu by remember { mutableStateOf(false) }
    var showAddToTripDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Check if sort is non-default
    val hasNonDefaultSort = selectedSort != RefillSortOption.MOST_RECENT

    // Create active sort chip
    val getActiveSortChip: @Composable () -> List<ActiveFilter> = {
        if (hasNonDefaultSort) {
            listOf(
                ActiveFilter(
                    id = "sort",
                    label = stringResource(selectedSort.labelRes),
                    onRemove = { viewModel.setSortOption(RefillSortOption.MOST_RECENT) }
                )
            )
        } else {
            emptyList()
        }
    }

    val activeSortChips = getActiveSortChip()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                // Selection mode top bar
                StyledTopAppBar(
                    title = { Text("${selectedRefillIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear selection"
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { showAddToTripDialog = true },
                            enabled = selectedRefillIds.isNotEmpty()
                        ) {
                            Text("ADD TO TRIP")
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
                    // Active sort chip
                    ActiveFiltersRow(
                        activeFilters = activeSortChips,
                        onClearAll = null // No clear all needed for single chip
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
                    Text(state.message)
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
        }
    }

    // Add to Trip Dialog
    if (showAddToTripDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddToTripDialog = false },
            title = { Text("Add to Trip") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select a trip to add ${selectedRefillIds.size} refill(s):")

                    if (availableTrips.isEmpty()) {
                        Text(
                            text = "No trips available.",
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
                        Text("Create Trip")
                    }
                    // Cancel button
                    TextButton(onClick = { showAddToTripDialog = false }) {
                        Text("Cancel")
                    }
                }
            },
            dismissButton = {}
        )
    }
}
