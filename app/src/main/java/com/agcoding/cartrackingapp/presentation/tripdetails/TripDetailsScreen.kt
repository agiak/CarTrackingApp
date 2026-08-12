package com.agcoding.cartrackingapp.presentation.tripdetails

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber
import com.agcoding.cartrackingapp.domain.model.TripStatistics
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    onNavigateBack: () -> Unit,
    onRefillClick: (Long) -> Unit,
    viewModel: TripDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showAddRefillsDialog by viewModel.showAddRefillsDialog.collectAsState()
    val availableRefills by viewModel.availableRefills.collectAsState()
    val selectedRefillIds by viewModel.selectedRefillIds.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var refillToRemoveId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.trip_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showDeleteDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_trip_cd),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TripDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is TripDetailsUiState.Success -> {
                TripDetailsContent(
                    tripStatistics = state.tripStatistics,
                    onRefillClick = onRefillClick,
                    onAddRefills = { viewModel.showAddRefillsDialog() },
                    onRemoveRefill = { refillId ->
                        refillToRemoveId = refillId
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is TripDetailsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text(stringResource(R.string.delete_trip)) },
            text = { Text(stringResource(R.string.delete_trip_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTrip(onSuccess = onNavigateBack)
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Remove Refill confirmation dialog
    refillToRemoveId?.let { refillId ->
        AlertDialog(
            onDismissRequest = { refillToRemoveId = null },
            title = { Text(stringResource(R.string.remove_refill_from_trip_title)) },
            text = { Text(stringResource(R.string.remove_refill_from_trip_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    refillToRemoveId = null
                    viewModel.removeRefill(
                        refillId = refillId,
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.refill_removed_from_trip))
                            }
                        },
                        onError = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                    )
                }) {
                    Text(stringResource(R.string.remove), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { refillToRemoveId = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Add Refills Dialog
    if (showAddRefillsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddRefillsDialog() },
            title = { Text(stringResource(R.string.add_refills_to_trip_title)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (availableRefills.isEmpty()) {
                        Text(stringResource(R.string.no_available_refills_message))
                    } else {
                        Text(stringResource(R.string.select_refills_to_add))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableRefills) { refill ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = refill.id in selectedRefillIds,
                                        onCheckedChange = { viewModel.toggleRefillSelection(refill.id) }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(refill.timestamp)),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${refill.litersAdded.formatNumber(1)} L • ${refill.tripDistance.formatNumber(0)} km",
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
                if (availableRefills.isNotEmpty()) {
                    Button(
                        onClick = {
                            viewModel.addSelectedRefills(
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(context.getString(R.string.refills_added_to_trip))
                                    }
                                },
                                onError = { error ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(error)
                                    }
                                }
                            )
                        },
                        enabled = selectedRefillIds.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.add_count_format, selectedRefillIds.size))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddRefillsDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TripDetailsScreenPreview() {
    CarTrackingAppTheme {
        TripDetailsScreen(
            onNavigateBack = {},
            onRefillClick = {}
        )
    }
}

