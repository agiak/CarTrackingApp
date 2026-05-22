package com.agcoding.cartrackingapp.presentation.cardetails

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.cardetails.components.CarHeaderCard
import com.agcoding.cartrackingapp.presentation.cardetails.components.IncompleteInformationBanner
import com.agcoding.cartrackingapp.presentation.cardetails.components.QuickStatsGrid
import com.agcoding.cartrackingapp.presentation.cardetails.components.TotalSpendingCard
import com.agcoding.cartrackingapp.presentation.components.ExpenseItemCard
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailsScreen(
    onNavigateBack: () -> Unit,
    onAddRefillClick: () -> Unit,
    onAddExpenseClick: () -> Unit = {},
    onRefillClick: (Long) -> Unit = {},
    onExpenseClick: (Long) -> Unit = {},
    onEditCarClick: () -> Unit = {},
    onViewAllRefillsClick: () -> Unit = {},
    onViewAllExpensesClick: () -> Unit = {},
    onViewAllTripsClick: () -> Unit = {},
    onTripClick: (Long) -> Unit = {},
    onCreateTripClick: () -> Unit = {},
    onDefaultCarSet: () -> Unit = {},
    onViewAttachments: () -> Unit = {},
    viewModel: CarDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val tripCount by viewModel.tripCount.collectAsState()
    val recentTrips by viewModel.recentTrips.collectAsState()
    val refillTripNames by viewModel.refillTripNames.collectAsState()

    var showOverflowMenu by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    val isDefault = (uiState as? CarDetailsUiState.Success)?.statistics?.car?.isDefault == true

    BackHandler(enabled = fabExpanded) { fabExpanded = false }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = { },
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
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit_car)) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = { showOverflowMenu = false; onEditCarClick() }
                            )
                            if (!isDefault) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.set_as_default)) },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.setDefaultCar()
                                        onDefaultCarSet()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.view_attachments)) },
                                leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null) },
                                onClick = { showOverflowMenu = false; onViewAttachments() }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = { showOverflowMenu = false; viewModel.showDeleteDialog() }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            SpeedDialFab(
                expanded = fabExpanded,
                onToggle = { fabExpanded = !fabExpanded },
                onAddRefill = { fabExpanded = false; onAddRefillClick() },
                onAddExpense = { fabExpanded = false; onAddExpenseClick() }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is CarDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is CarDetailsUiState.Success -> {
                val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
                val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
                val useSplitView = isTablet || isLandscape

                if (useSplitView) {
                    // Split view for tablets and landscape
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left side: Car info and stats (scrollable)
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CarHeaderCard(state.statistics)

                            // Incomplete information banner
                            val car = state.statistics.car
                            val hasMissingInfo = car.insuranceExpirationDate == null ||
                                    car.kteoExpirationDate == null ||
                                    car.emissionsCardExpirationDate == null ||
                                    car.roadTaxAmount == null ||
                                    car.roadTaxDueDate == null ||
                                    car.lastServiceDate == null ||
                                    car.lastTireChangeDate == null ||
                                    car.tireBrand.isNullOrBlank() ||
                                    car.tireDimensions.isNullOrBlank() ||
                                    car.tireInstallationDate == null

                            if (hasMissingInfo) {
                                IncompleteInformationBanner(
                                    car = car,
                                    onAddInformationClick = onEditCarClick
                                )
                            }

                            QuickStatsGrid(state.statistics)
                            TotalSpendingCard(state.statistics)
                        }

                        // Right side: Lists (scrollable)
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight(),
                            contentPadding = PaddingValues(bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Refills section
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Refills (${state.statistics.totalRefills})",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (state.statistics.totalRefills > 3) {
                                        Text(
                                            text = stringResource(R.string.see_all),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable(onClick = onViewAllRefillsClick)
                                        )
                                    }
                                }
                            }

                            items(state.statistics.recentRefills.take(3)) { refill ->
                                RefillItemCard(
                                    refill = refill,
                                    carName = null,
                                    onClick = { onRefillClick(refill.id) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Services section
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val totalExpenses = state.statistics.serviceExpenseCount + state.statistics.otherExpenseCount
                                    Text(
                                        text = "Services ($totalExpenses)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (totalExpenses > 3) {
                                        Text(
                                            text = stringResource(R.string.see_all),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable(onClick = onViewAllExpensesClick)
                                        )
                                    }
                                }
                            }

                            items(state.statistics.recentExpenses.take(3)) { expense ->
                                ExpenseItemCard(
                                    expense = expense,
                                    carName = null,
                                    onClick = { onExpenseClick(expense.id) }
                                )
                            }
                        }
                    }
                } else {
                    // Original single column layout for portrait phones
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Car header with icon
                        item {
                            CarHeaderCard(state.statistics)
                        }

                        // Incomplete information banner (show if any optional field is missing)
                        item {
                            val car = state.statistics.car
                            val hasMissingInfo = car.insuranceExpirationDate == null ||
                                    car.kteoExpirationDate == null ||
                                    car.emissionsCardExpirationDate == null ||
                                    car.roadTaxAmount == null ||
                                    car.roadTaxDueDate == null ||
                                    car.lastServiceDate == null ||
                                    car.lastTireChangeDate == null ||
                                    car.tireBrand.isNullOrBlank() ||
                                    car.tireDimensions.isNullOrBlank() ||
                                    car.tireInstallationDate == null

                            if (hasMissingInfo) {
                                IncompleteInformationBanner(
                                    car = car,
                                    onAddInformationClick = onEditCarClick
                                )
                            }
                        }

                        // Quick stats grid
                        item {
                            QuickStatsGrid(state.statistics)
                        }

                        // Total spending card
                        item {
                            TotalSpendingCard(state.statistics)
                        }

                        // Refills header with "See All"
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Refills (${state.statistics.totalRefills})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (state.statistics.totalRefills > 3) {
                                    Text(
                                        text = stringResource(R.string.see_all),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable(onClick = onViewAllRefillsClick)
                                    )
                                }
                            }
                        }

                        // Recent refills
                        items(state.statistics.recentRefills.take(3)) { refill ->
                            RefillItemCard(
                                refill = refill,
                                carName = null,
                                onClick = { onRefillClick(refill.id) },
                                tripName = refillTripNames[refill.id]
                            )
                        }

                        // Trips section (always show) - MOVED HERE AFTER REFILLS
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Trips ($tripCount)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // + button to create trip
                                    androidx.compose.material3.IconButton(
                                        onClick = onCreateTripClick,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Add,
                                            contentDescription = "Create Trip",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // See all button (only if there are trips)
                                    if (tripCount > 0) {
                                        Text(
                                            text = stringResource(R.string.see_all),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable(onClick = onViewAllTripsClick)
                                        )
                                    }
                                }
                            }
                        }

                        // Recent trips or empty message
                        if (tripCount > 0) {
                            items(recentTrips.take(5)) { trip ->
                                TripCard(
                                    trip = trip,
                                    onClick = { onTripClick(trip.id) }
                                )
                            }
                        } else {
                            item {
                                androidx.compose.material3.Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No trips yet. Create one!",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Expenses header with "See All"
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val totalExpenses = state.statistics.serviceExpenseCount + state.statistics.otherExpenseCount
                                Text(
                                    text = "Services ($totalExpenses)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (totalExpenses > 3) {
                                    Text(
                                        text = stringResource(R.string.see_all),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable(onClick = onViewAllExpensesClick)
                                    )
                                }
                            }
                        }

                        // Recent expenses
                        items(state.statistics.recentExpenses.take(3)) { expense ->
                            ExpenseItemCard(
                                expense = expense,
                                carName = null, // Don't show car name in car details screen
                                onClick = { onExpenseClick(expense.id) }
                            )
                        }
                    }
                }
            }

            is CarDetailsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }
        }

        // Scrim overlay — closes FAB when tapping outside
        AnimatedVisibility(
            visible = fabExpanded,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { fabExpanded = false }
            )
        }
        } // closes outer Box
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text(stringResource(R.string.delete_car)) },
            text = { Text(stringResource(R.string.delete_car_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCar(onSuccess = onNavigateBack)
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
}

@Composable
fun TripCard(
    trip: com.agcoding.cartrackingapp.domain.model.Trip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (trip.description != null && trip.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trip.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${trip.refills.size} refills",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SpeedDialFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddRefill: () -> Unit,
    onAddExpense: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mini items — slide up from below with fade
        AnimatedVisibility(
            visible = expanded,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(250)
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(150))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniSpeedDialItem(
                    label = "Add Expense",
                    icon = Icons.Default.Receipt,
                    onClick = onAddExpense
                )
                MiniSpeedDialItem(
                    label = "Add Refill",
                    icon = Icons.Default.LocalGasStation,
                    onClick = onAddRefill
                )
            }
        }

        // Main FAB — "+" rotates to "×" when expanded
        val rotation by animateFloatAsState(
            targetValue = if (expanded) 45f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "fabRotation"
        )
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "Close" else "Add action",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun MiniSpeedDialItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Label chip
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            tonalElevation = 3.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        // Small FAB
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
    }
}

