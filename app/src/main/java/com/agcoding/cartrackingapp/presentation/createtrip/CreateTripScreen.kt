package com.agcoding.cartrackingapp.presentation.createtrip

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateTripViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = androidx.compose.material3.SnackbarHostState()
    var showSortMenu by remember { mutableStateOf(false) }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StyledTopAppBar(
                title = { Text("Create Trip") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isCreating
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.createTrip(onSuccess = onNavigateBack)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isCreating
                    ) {
                        if (uiState.isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Create Trip")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Trip Name
                item {
                    OutlinedTextField(
                        value = uiState.tripName,
                        onValueChange = viewModel::onTripNameChanged,
                        label = { Text("Trip Name *") },
                        placeholder = { Text("e.g., Summer Vacation 2026") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        singleLine = true
                    )
                }

                // Trip Description
                item {
                    OutlinedTextField(
                        value = uiState.tripDescription,
                        onValueChange = viewModel::onTripDescriptionChanged,
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Add notes about this trip...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }

                // Selection Header with Sort
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Select Refills *",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.selectionError != null) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (uiState.selectionError != null) {
                                    Text(
                                        text = uiState.selectionError!!,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Text(
                                    text = "${uiState.selectedRefillIds.size} selected",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Sort dropdown
                            Box {
                                OutlinedButton(
                                    onClick = { showSortMenu = true },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = when (uiState.sortOption) {
                                            com.agcoding.cartrackingapp.presentation.createtrip.RefillSortOption.MOST_RECENT -> "Newest"
                                            com.agcoding.cartrackingapp.presentation.createtrip.RefillSortOption.OLDEST -> "Oldest"
                                        },
                                        fontSize = 14.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Sort"
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Newest First",
                                                fontWeight = if (uiState.sortOption == com.agcoding.cartrackingapp.presentation.createtrip.RefillSortOption.MOST_RECENT)
                                                    FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOption(com.agcoding.cartrackingapp.presentation.createtrip.RefillSortOption.MOST_RECENT)
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Oldest First",
                                                fontWeight = if (uiState.sortOption == com.agcoding.cartrackingapp.presentation.createtrip.RefillSortOption.OLDEST)
                                                    FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOption(com.agcoding.cartrackingapp.presentation.createtrip.RefillSortOption.OLDEST)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Available Refills List
                if (uiState.availableRefills.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No available refills to add to a trip",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(uiState.availableRefills) { refill ->
                        SelectableRefillCard(
                            refill = refill,
                            isSelected = refill.id in uiState.selectedRefillIds,
                            onToggleSelection = { viewModel.toggleRefillSelection(refill.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableRefillCard(
    refill: FuelRefill,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelection),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(refill.timestamp)),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%.2f L • %.2f km • €%.2f",
                        refill.litersAdded,
                        refill.tripDistance,
                        refill.amountPaid),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() }
            )
        }
    }
}

