package com.agcoding.cartrackingapp.presentation.refilldetails

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.AppConfirmDialog
import com.agcoding.cartrackingapp.presentation.components.SuccessOverlay
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.components.TonalButton
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.presentation.refilldetails.components.RefillDetailsContent
import com.agcoding.cartrackingapp.presentation.refilldetails.components.NewTripSheet
import com.agcoding.cartrackingapp.presentation.refilldetails.components.RefillTripCard
import com.agcoding.cartrackingapp.presentation.refilldetails.components.TripCreatedSheet
import com.agcoding.cartrackingapp.presentation.refilldetails.components.TripPickerSheet
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefillDetailsScreen(
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit = {},
    viewModel: RefillDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    var showDeleted by remember { mutableStateOf(false) }
    val trips by viewModel.trips.collectAsState()
    val currentTrip by viewModel.currentTrip.collectAsState()
    val tripMessage by viewModel.tripMessage.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showTripPicker by remember { mutableStateOf(false) }
    var showNewTripSheet by remember { mutableStateOf(false) }
    var createdTripName by remember { mutableStateOf<String?>(null) }

    // Report the outcome of a trip action once, then clear it so it cannot repeat on
    // the next recomposition. A newly created trip gets the success sheet instead of a
    // snackbar — one confirmation per action, never both.
    LaunchedEffect(tripMessage) {
        when (val message = tripMessage) {
            null -> return@LaunchedEffect
            is TripActionMessage.Created -> createdTripName = message.tripName
            is TripActionMessage.Assigned -> snackbarHostState.showSnackbar(
                message = context.getString(R.string.refill_added_to_trip, message.tripName),
                duration = SnackbarDuration.Short
            )

            TripActionMessage.Removed -> snackbarHostState.showSnackbar(
                message = context.getString(R.string.refill_removed_from_trip),
                duration = SnackbarDuration.Short
            )

            TripActionMessage.Failed -> snackbarHostState.showSnackbar(
                message = context.getString(R.string.refill_trip_update_failed),
                duration = SnackbarDuration.Short
            )
        }
        viewModel.clearTripMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.refill_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }
                    IconButton(onClick = { viewModel.showDeleteDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val state = uiState) {
            is RefillDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is RefillDetailsUiState.Success -> {
                RefillDetailsContent(
                    details = state.details,
                    addressString = state.addressString,
                    context = context,
                    onLocationClick = { latitude, longitude ->
                        openGoogleMaps(context, latitude, longitude)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    tripSection = {
                        RefillTripCard(
                            tripName = currentTrip?.name,
                            onClick = { showTripPicker = true }
                        )
                    }
                )
            }
            is RefillDetailsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColorScheme.current.statusError
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    TonalButton(
                        label = stringResource(R.string.retry),
                        onClick = viewModel::retry
                    )
                }
            }
        }

        if (showTripPicker) {
            TripPickerSheet(
                trips = trips,
                currentTripId = currentTrip?.id,
                onTripSelected = { trip ->
                    showTripPicker = false
                    viewModel.assignToTrip(trip)
                },
                onCreateNewTrip = {
                    showTripPicker = false
                    showNewTripSheet = true
                },
                onRemoveFromTrip = {
                    showTripPicker = false
                    viewModel.removeFromTrip()
                },
                onDismiss = { showTripPicker = false }
            )
        }

        if (showNewTripSheet) {
            NewTripSheet(
                onCreate = { name, description ->
                    showNewTripSheet = false
                    viewModel.createTripAndAssign(name, description)
                },
                onDismiss = { showNewTripSheet = false }
            )
        }

        createdTripName?.let { tripName ->
            TripCreatedSheet(
                tripName = tripName,
                onFinished = { createdTripName = null }
            )
        }

        if (showDeleteDialog) {
            AppConfirmDialog(
                title = stringResource(R.string.delete_refill_title),
                message = stringResource(R.string.delete_refill_confirm),
                confirmLabel = stringResource(R.string.delete),
                dismissLabel = stringResource(R.string.cancel),
                icon = Icons.Default.Delete,
                destructive = true,
                // Deletes on confirm, rather than after an undo snackbar has
                // run its course. That snackbar was shown by this screen and
                // awaited in this screen's scope, so going back — which is
                // what anyone does once a dialog says the refill is gone —
                // cancelled the wait and the refill was never deleted at all.
                onConfirm = {
                    viewModel.deleteRefill(
                        onSuccess = { showDeleted = true },
                        onError = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.refill_delete_failed)
                                )
                            }
                        },
                    )
                },
                onDismiss = { viewModel.hideDeleteDialog() }
            )
        }
    }

    // Outside the Scaffold on purpose: it has to cover the top bar too. Edit
    // and back stay tappable under an overlay that only fills the content
    // area, and this one is on its way out of the screen.
    SuccessOverlay(
        visible = showDeleted,
        message = stringResource(R.string.refill_deleted),
        onFinished = onNavigateBack,
    )
}

private fun openGoogleMaps(context: Context, latitude: Double, longitude: Double) {
    val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")

    // Check if Google Maps is installed
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        // Fallback to browser if Google Maps is not installed
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
        )
        context.startActivity(browserIntent)
    }
}

@Preview(showBackground = true)
@Composable
private fun RefillDetailsScreenPreview() {
    CarTrackingAppTheme {
        RefillDetailsScreen(
            onNavigateBack = {},
            onEditClick = {}
        )
    }
}

