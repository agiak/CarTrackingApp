package com.agcoding.cartrackingapp.presentation.carlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.usecase.expense.ReminderInfo
import com.agcoding.cartrackingapp.presentation.carlist.components.AddCarDialog
import com.agcoding.cartrackingapp.presentation.carlist.components.CarCard
import com.agcoding.cartrackingapp.presentation.carlist.components.EmptyState
import com.agcoding.cartrackingapp.presentation.carlist.components.ErrorState
import com.agcoding.cartrackingapp.presentation.carlist.components.ReminderBanner
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListScreen(
    onCarClick: (Long) -> Unit,
    onStatisticsClick: () -> Unit,
    onAddRefillClick: (Long) -> Unit,
    onAddServiceClick: (Long) -> Unit,
    onNavigateToReminders: () -> Unit,
    viewModel: CarListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddCarDialog by viewModel.showAddCarDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.car_list_title)) }
            )
        },
        floatingActionButton = {
            // Only show FAB when there are cars
            if (uiState is CarListUiState.Success) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { viewModel.showAddCarDialog() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.car_list_add_car_cd)
                    )
                }
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is CarListUiState.Empty -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        onAddCarClick = { viewModel.showAddCarDialog() }
                    )
                }

                is CarListUiState.Success -> {
                    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
                    val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()

                    // Determine grid columns based on device type
                    val useGrid = isTablet || isLandscape
                    val gridColumns = when {
                        isTablet -> 3 // Tablets get 3 columns
                        isLandscape -> 2 // Landscape phones get 2 columns
                        else -> 1 // Portrait phones get 1 column
                    }

                    if (useGrid && gridColumns > 1) {
                        // Grid layout for tablets and landscape
                        val gridState = rememberLazyGridState()

                        LaunchedEffect(Unit) {
                            gridState.scrollToItem(0)
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 88.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Reminder Banner - spans full width
                            state.reminderInfo?.let { reminderInfo ->
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(gridColumns) }) {
                                    ReminderBanner(
                                        reminderInfo = reminderInfo,
                                        onClick = onNavigateToReminders,
                                        onDismiss = { viewModel.dismissBannerForToday() }
                                    )
                                }
                            }

                            // Car cards in grid
                            items(
                                items = state.cars,
                                key = { it.id }
                            ) { car ->
                                CarCard(
                                    car = car,
                                    onClick = { onCarClick(car.id) },
                                    onAddRefillClick = { onAddRefillClick(car.id) },
                                    onAddServiceClick = { onAddServiceClick(car.id) }
                                )
                            }
                        }
                    } else {
                        // List layout for portrait phones (original layout)
                        val listState = rememberLazyListState()

                        LaunchedEffect(Unit) {
                            listState.scrollToItem(0)
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Reminder Banner
                            state.reminderInfo?.let { reminderInfo ->
                                item {
                                    ReminderBanner(
                                        reminderInfo = reminderInfo,
                                        onClick = onNavigateToReminders,
                                        onDismiss = { viewModel.dismissBannerForToday() }
                                    )
                                }
                            }

                            items(
                                items = state.cars,
                                key = { it.id }
                            ) { car ->
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
                    ErrorState(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (showAddCarDialog) {
            AddCarDialog(
                onDismiss = { viewModel.hideAddCarDialog() },
                onConfirm = { name, plate, odometer ->
                    viewModel.addCar(name, plate, odometer) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.car_added_successfully),
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                        }
                    }
                }
            )
        }
    }
}

/**
 * Stateless version of CarListScreen content for previews
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarListScreenContent(
    uiState: CarListUiState,
    showAddCarDialog: Boolean = false,
    onCarClick: (Long) -> Unit = {},
    onAddRefillClick: (Long) -> Unit = {},
    onAddServiceClick: (Long) -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onAddCarClick: () -> Unit = {},
    onDismissBanner: () -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onConfirmAddCar: (String, String, String) -> Unit = { _, _, _ -> }
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.car_list_title)) }
            )
        },
        floatingActionButton = {
            // Only show FAB when there are cars
            if (uiState is CarListUiState.Success) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = onAddCarClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.car_list_add_car_cd)
                    )
                }
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is CarListUiState.Empty -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        onAddCarClick = onAddCarClick
                    )
                }

                is CarListUiState.Success -> {
                    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
                    val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()

                    // Determine grid columns based on device type
                    val useGrid = isTablet || isLandscape
                    val gridColumns = when {
                        isTablet -> 3 // Tablets get 3 columns
                        isLandscape -> 2 // Landscape phones get 2 columns
                        else -> 1 // Portrait phones get 1 column
                    }

                    if (useGrid && gridColumns > 1) {
                        // Grid layout for tablets and landscape
                        val gridState = rememberLazyGridState()

                        LaunchedEffect(Unit) {
                            gridState.scrollToItem(0)
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 88.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Reminder Banner - spans full width
                            state.reminderInfo?.let { reminderInfo ->
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(gridColumns) }) {
                                    ReminderBanner(
                                        reminderInfo = reminderInfo,
                                        onClick = onNavigateToReminders,
                                        onDismiss = onDismissBanner
                                    )
                                }
                            }

                            // Car cards in grid
                            items(
                                items = state.cars,
                                key = { it.id }
                            ) { car ->
                                CarCard(
                                    car = car,
                                    onClick = { onCarClick(car.id) },
                                    onAddRefillClick = { onAddRefillClick(car.id) },
                                    onAddServiceClick = { onAddServiceClick(car.id) }
                                )
                            }
                        }
                    } else {
                        // List layout for portrait phones (original layout)
                        val listState = rememberLazyListState()

                        LaunchedEffect(Unit) {
                            listState.scrollToItem(0)
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Reminder Banner
                            state.reminderInfo?.let { reminderInfo ->
                                item {
                                    ReminderBanner(
                                        reminderInfo = reminderInfo,
                                        onClick = onNavigateToReminders,
                                        onDismiss = onDismissBanner
                                    )
                                }
                            }

                            items(
                                items = state.cars,
                                key = { it.id }
                            ) { car ->
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
                    ErrorState(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (showAddCarDialog) {
            AddCarDialog(
                onDismiss = onDismissDialog,
                onConfirm = onConfirmAddCar
            )
        }
    }
}

// ============================================
// Preview Data Provider
// ============================================

private class CarListUiStateProvider : PreviewParameterProvider<CarListUiState> {
    override val values: Sequence<CarListUiState> = sequenceOf(
        CarListUiState.Loading,
        CarListUiState.Empty,
        CarListUiState.Success(
            cars = listOf(
                Car(
                    id = 1,
                    name = "Toyota Corolla",
                    licensePlate = "ABC-1234",
                    currentOdometer = 45000.0,
                    initialOdometer = 0.0,
                    averageConsumption = 6.5,
                    totalRefills = 120,
                    totalCost = 8500.0,
                    totalDistance = 45000.0
                )
            ),
            reminderInfo = null
        ),
        CarListUiState.Success(
            cars = listOf(
                Car(
                    id = 1,
                    name = "Toyota Corolla",
                    licensePlate = "ABC-1234",
                    currentOdometer = 45000.0,
                    initialOdometer = 0.0,
                    averageConsumption = 6.5,
                    totalRefills = 120,
                    totalCost = 8500.0,
                    totalDistance = 45000.0
                ),
                Car(
                    id = 2,
                    name = "Honda Civic",
                    licensePlate = "XYZ-5678",
                    currentOdometer = 32000.0,
                    initialOdometer = 0.0,
                    averageConsumption = 7.2,
                    totalRefills = 85,
                    totalCost = 6200.0,
                    totalDistance = 32000.0
                )
            ),
            reminderInfo = ReminderInfo(
                dateBasedCount = 1,
                mileageBasedCount = 2
            )
        ),
        CarListUiState.Error("Failed to load cars. Please check your connection.")
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Loading State", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCarListScreenLoading() {
    CarTrackingAppTheme(darkTheme = false) {
        CarListScreenContent(
            uiState = CarListUiState.Loading
        )
    }
}

@Preview(name = "Empty State", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCarListScreenEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        CarListScreenContent(
            uiState = CarListUiState.Empty
        )
    }
}

@Preview(name = "Single Car - No Reminders", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCarListScreenSingleCar() {
    CarTrackingAppTheme(darkTheme = false) {
        CarListScreenContent(
            uiState = CarListUiState.Success(
                cars = listOf(
                    Car(
                        id = 1,
                        name = "Toyota Corolla",
                        licensePlate = "ABC-1234",
                        currentOdometer = 45000.0,
                        initialOdometer = 0.0,
                        averageConsumption = 6.5,
                        totalRefills = 120,
                        totalCost = 8500.0,
                        totalDistance = 45000.0
                    )
                ),
                reminderInfo = null
            )
        )
    }
}

@Preview(name = "Multiple Cars with Reminders", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCarListScreenMultipleCars() {
    CarTrackingAppTheme(darkTheme = false) {
        CarListScreenContent(
            uiState = CarListUiState.Success(
                cars = listOf(
                    Car(
                        id = 1,
                        name = "Toyota Corolla",
                        licensePlate = "ABC-1234",
                        currentOdometer = 45000.0,
                        initialOdometer = 0.0,
                        averageConsumption = 6.5,
                        totalRefills = 120,
                        totalCost = 8500.0,
                        totalDistance = 45000.0
                    ),
                    Car(
                        id = 2,
                        name = "Honda Civic",
                        licensePlate = "XYZ-5678",
                        currentOdometer = 32000.0,
                        initialOdometer = 0.0,
                        averageConsumption = 7.2,
                        totalRefills = 85,
                        totalCost = 6200.0,
                        totalDistance = 32000.0
                    ),
                    Car(
                        id = 3,
                        name = "BMW 320i",
                        licensePlate = "DEF-9012",
                        currentOdometer = 58000.0,
                        initialOdometer = 0.0,
                        averageConsumption = 8.1,
                        totalRefills = 145,
                        totalCost = 11200.0,
                        totalDistance = 58000.0
                    )
                ),
                reminderInfo = ReminderInfo(
                    dateBasedCount = 1,
                    mileageBasedCount = 2
                )
            )
        )
    }
}

@Preview(name = "Error State", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCarListScreenError() {
    CarTrackingAppTheme(darkTheme = false) {
        CarListScreenContent(
            uiState = CarListUiState.Error("Failed to load cars. Please check your connection.")
        )
    }
}

@Preview(name = "Dark Theme - Multiple Cars", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCarListScreenDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CarListScreenContent(
            uiState = CarListUiState.Success(
                cars = listOf(
                    Car(
                        id = 1,
                        name = "Toyota Corolla",
                        licensePlate = "ABC-1234",
                        currentOdometer = 45000.0,
                        initialOdometer = 0.0,
                        averageConsumption = 6.5,
                        totalRefills = 120,
                        totalCost = 8500.0,
                        totalDistance = 45000.0
                    ),
                    Car(
                        id = 2,
                        name = "Honda Civic",
                        licensePlate = "XYZ-5678",
                        currentOdometer = 32000.0,
                        initialOdometer = 0.0,
                        averageConsumption = 7.2,
                        totalRefills = 85,
                        totalCost = 6200.0,
                        totalDistance = 32000.0
                    )
                ),
                reminderInfo = ReminderInfo(
                    dateBasedCount = 2,
                    mileageBasedCount = 1
                )
            )
        )
    }
}

@Preview(
    name = "Tablet - Landscape",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape"
)
@Composable
private fun PreviewCarListScreenTabletLandscape() {
    CarTrackingAppTheme(darkTheme = false) {
        CarListScreenContent(
            uiState = CarListUiState.Success(
                cars = listOf(
                    Car(
                        id = 1,
                        name = "Toyota Corolla",
                        licensePlate = "ABC-1234",
                        currentOdometer = 45000.0,
                        initialOdometer = 0.0,
                        averageConsumption = 6.5,
                        totalRefills = 120,
                        totalCost = 8500.0,
                        totalDistance = 45000.0
                    ),
                    Car(
                        id = 2,
                        name = "Honda Civic",
                        licensePlate = "XYZ-5678",
                        currentOdometer = 32000.0,
                        initialOdometer = 0.0,
                        averageConsumption = 7.2,
                        totalRefills = 85,
                        totalCost = 6200.0,
                        totalDistance = 32000.0
                    ),
                    Car(
                        id = 3,
                        name = "BMW 320i",
                        licensePlate = "DEF-9012",
                        currentOdometer = 58000.0,
                        initialOdometer = 0.0,
                        averageConsumption = 8.1,
                        totalRefills = 145,
                        totalCost = 11200.0,
                        totalDistance = 58000.0
                    )
                ),
                reminderInfo = null
            )
        )
    }
}
