package com.agcoding.cartrackingapp.presentation.carlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.usecase.expense.ReminderInfo
import com.agcoding.cartrackingapp.presentation.carlist.components.AddCarDialog
import com.agcoding.cartrackingapp.presentation.carlist.components.CarCard
import com.agcoding.cartrackingapp.presentation.carlist.components.EmptyState
import com.agcoding.cartrackingapp.presentation.carlist.components.ErrorState
import com.agcoding.cartrackingapp.presentation.carlist.components.PermissionsBanner
import com.agcoding.cartrackingapp.presentation.carlist.components.ReminderBanner
import com.agcoding.cartrackingapp.presentation.components.HeaderIconButton
import com.agcoding.cartrackingapp.presentation.components.ScreenHeader
import com.agcoding.cartrackingapp.presentation.components.SuccessOverlay
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.util.formatNumber

@Composable
fun CarListScreen(
    onCarClick: (Long) -> Unit,
    onStatisticsClick: () -> Unit,
    onAddRefillClick: (Long) -> Unit,
    onAddServiceClick: (Long) -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToPermissionsSettings: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    viewModel: CarListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddCarDialog by viewModel.showAddCarDialog.collectAsState()
    val showPermissionBanner by viewModel.showPermissionBanner.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Re-check permissions when the screen resumes (e.g. returning from system settings)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionState()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    // Shows the app-wide success confirmation after a car is added.
    var showSuccess by remember { mutableStateOf(false) }

    CarListContent(
        uiState = uiState,
        showPermissionBanner = showPermissionBanner,
        hasReminders = (uiState as? CarListUiState.Success)?.reminderInfo != null,
        onCarClick = onCarClick,
        onAddRefillClick = onAddRefillClick,
        onAddServiceClick = onAddServiceClick,
        onNavigateToReminders = onNavigateToReminders,
        onNavigateToPermissionsSettings = onNavigateToPermissionsSettings,
        onNavigateToProfile = onNavigateToProfile,
        onAddCarClick = { viewModel.showAddCarDialog() },
        onDismissBanner = { viewModel.dismissBannerForToday() },
        onDismissPermissionBanner = { viewModel.dismissPermissionBanner() },
        snackbarHostState = snackbarHostState,
    )

    if (showAddCarDialog) {
        AddCarDialog(
            onDismiss = { viewModel.hideAddCarDialog() },
            onConfirm = { name, plate, odometer ->
                viewModel.addCar(name, plate, odometer) { showSuccess = true }
            }
        )
    }

    SuccessOverlay(
        visible = showSuccess,
        message = stringResource(R.string.car_added_successfully),
        onFinished = { showSuccess = false }
    )
}

/**
 * Everything the screen draws, with no ViewModel in sight so the previews can
 * reach it.
 *
 * The title is an item in the list rather than a top app bar. On a phone the
 * cars are the screen; a pinned bar would spend 56dp saying "My Cars" to
 * somebody who is looking straight at their cars. Scrolled away, that height
 * belongs to the content, and the title is still the first thing you see when
 * you arrive or scroll back.
 */
@Composable
private fun CarListContent(
    uiState: CarListUiState,
    showPermissionBanner: Boolean,
    hasReminders: Boolean,
    onCarClick: (Long) -> Unit,
    onAddRefillClick: (Long) -> Unit,
    onAddServiceClick: (Long) -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToPermissionsSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onAddCarClick: () -> Unit,
    onDismissBanner: () -> Unit,
    onDismissPermissionBanner: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
) {
    val dimens = LocalAppDimens.current
    val margin = dimens.spacing.screenHorizontal

    val cars = (uiState as? CarListUiState.Success)?.cars.orEmpty()
    val meta = if (cars.isEmpty()) {
        null
    } else {
        val vehicles = pluralStringResource(
            R.plurals.car_list_vehicles_count, cars.size, cars.size,
        )
        val tracked = stringResource(
            R.string.car_list_meta_tracked,
            cars.sumOf { it.totalDistance }.formatNumber(0),
        )
        "$vehicles · $tracked"
    }

    // The lists already inset their content by the screen margin, so the
    // header is told not to add its own there; the states that are plain
    // Columns get the default.
    val header: @Composable (Dp) -> Unit = { horizontalPadding ->
        ScreenHeader(
            title = stringResource(R.string.car_list_title),
            meta = meta,
            horizontalPadding = horizontalPadding,
            actions = {
                HeaderIconButton(
                    icon = Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.tab_notifications_cd),
                    onClick = onNavigateToReminders,
                    showDot = hasReminders,
                )
                HeaderIconButton(
                    icon = Icons.Default.AccountCircle,
                    contentDescription = stringResource(R.string.tab_profile_cd),
                    onClick = onNavigateToProfile,
                )
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
        floatingActionButton = {
            if (uiState is CarListUiState.Success) {
                ExtendedFloatingActionButton(
                    onClick = onAddCarClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(dimens.radius.fab),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                        )
                    },
                    text = { Text(stringResource(R.string.car_list_add_car)) },
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is CarListUiState.Loading -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        header(margin)
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }

                is CarListUiState.Empty -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        header(margin)
                        if (showPermissionBanner) {
                            PermissionsBanner(
                                onClick = onNavigateToPermissionsSettings,
                                onDismiss = onDismissPermissionBanner,
                                modifier = Modifier.padding(horizontal = margin, vertical = 8.dp)
                            )
                        }
                        EmptyState(
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterHorizontally),
                            onAddCarClick = onAddCarClick
                        )
                    }
                }

                is CarListUiState.Success -> {
                    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
                    val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
                    val gridColumns = when {
                        isTablet -> 3
                        isLandscape -> 2
                        else -> 1
                    }
                    // The FAB is an extended one now, so it is both taller and
                    // wider than the old circle; the list has to clear it or
                    // the last car sits permanently half-covered.
                    val contentPadding = PaddingValues(
                        start = margin,
                        end = margin,
                        top = 0.dp,
                        bottom = dimens.spacing.fabSafeBottom,
                    )

                    if (gridColumns > 1) {
                        val gridState = rememberLazyGridState()
                        LaunchedEffect(Unit) { gridState.scrollToItem(0) }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = contentPadding,
                            horizontalArrangement = Arrangement.spacedBy(dimens.spacing.cardGap),
                            verticalArrangement = Arrangement.spacedBy(dimens.spacing.cardGap)
                        ) {
                            item(span = { GridItemSpan(gridColumns) }) { header(0.dp) }

                            if (showPermissionBanner) {
                                item(span = { GridItemSpan(gridColumns) }) {
                                    PermissionsBanner(
                                        onClick = onNavigateToPermissionsSettings,
                                        onDismiss = onDismissPermissionBanner
                                    )
                                }
                            }

                            state.reminderInfo?.let { reminderInfo ->
                                item(span = { GridItemSpan(gridColumns) }) {
                                    ReminderBanner(
                                        reminderInfo = reminderInfo,
                                        onClick = onNavigateToReminders,
                                        onDismiss = onDismissBanner
                                    )
                                }
                            }

                            items(items = state.cars, key = { it.id }) { car ->
                                CarCard(
                                    car = car,
                                    onClick = { onCarClick(car.id) },
                                    onAddRefillClick = { onAddRefillClick(car.id) },
                                    onAddServiceClick = { onAddServiceClick(car.id) }
                                )
                            }
                        }
                    } else {
                        val listState = rememberLazyListState()
                        LaunchedEffect(Unit) { listState.scrollToItem(0) }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = contentPadding,
                            verticalArrangement = Arrangement.spacedBy(dimens.spacing.cardGap)
                        ) {
                            item { header(0.dp) }

                            if (showPermissionBanner) {
                                item {
                                    PermissionsBanner(
                                        onClick = onNavigateToPermissionsSettings,
                                        onDismiss = onDismissPermissionBanner
                                    )
                                }
                            }

                            state.reminderInfo?.let { reminderInfo ->
                                item {
                                    ReminderBanner(
                                        reminderInfo = reminderInfo,
                                        onClick = onNavigateToReminders,
                                        onDismiss = onDismissBanner
                                    )
                                }
                            }

                            items(items = state.cars, key = { it.id }) { car ->
                                CarCard(
                                    car = car,
                                    onClick = { onCarClick(car.id) },
                                    onAddRefillClick = { onAddRefillClick(car.id) },
                                    onAddServiceClick = { onAddServiceClick(car.id) }
                                )
                            }
                        }
                    }
                }

                is CarListUiState.Error -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        header(margin)
                        ErrorState(
                            message = state.message,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

// ── previews ─────────────────────────────────────────────────────────────

private fun previewCars() = listOf(
    Car(
        id = 1, name = "Toyota Corolla", licensePlate = "ΙΖΧ-4821",
        currentOdometer = 145678.0, initialOdometer = 20000.0, averageConsumption = 6.5,
        totalRefills = 501, totalCost = 15432.50, totalDistance = 125678.0, isDefault = true,
    ),
    Car(
        id = 2, name = "VW Golf", licensePlate = "ΑΒΕ-1190",
        currentOdometer = 243286.0, initialOdometer = 180000.0, averageConsumption = 7.8,
        totalRefills = 472, totalCost = 21980.0, totalDistance = 63286.0,
    ),
    Car(
        id = 3, name = "Tesla Model 3", licensePlate = "",
        currentOdometer = 5000.0, initialOdometer = 5000.0, averageConsumption = 0.0,
        totalRefills = 0, totalCost = 0.0, totalDistance = 0.0,
    ),
)

@Composable
private fun PreviewScreen(uiState: CarListUiState, permissionBanner: Boolean = false) {
    CarListContent(
        uiState = uiState,
        showPermissionBanner = permissionBanner,
        hasReminders = (uiState as? CarListUiState.Success)?.reminderInfo != null,
        onCarClick = {}, onAddRefillClick = {}, onAddServiceClick = {},
        onNavigateToReminders = {}, onNavigateToPermissionsSettings = {},
        onNavigateToProfile = {}, onAddCarClick = {},
        onDismissBanner = {}, onDismissPermissionBanner = {},
    )
}

@Preview(name = "My Cars — dark", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCarListDark() {
    CarTrackingAppTheme(darkTheme = true) {
        PreviewScreen(
            CarListUiState.Success(
                cars = previewCars(),
                reminderInfo = ReminderInfo(dateBasedCount = 1, mileageBasedCount = 1),
            )
        )
    }
}

@Preview(name = "My Cars — light", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCarListLight() {
    CarTrackingAppTheme(darkTheme = false) {
        PreviewScreen(CarListUiState.Success(cars = previewCars(), reminderInfo = null))
    }
}

@Preview(name = "My Cars — empty", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCarListEmpty() {
    CarTrackingAppTheme(darkTheme = true) {
        PreviewScreen(CarListUiState.Empty, permissionBanner = true)
    }
}

@Preview(name = "My Cars — loading", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCarListLoading() {
    CarTrackingAppTheme(darkTheme = true) {
        PreviewScreen(CarListUiState.Loading)
    }
}
