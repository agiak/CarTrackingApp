package com.agcoding.cartrackingapp.presentation.cardetails

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    viewModel: CarDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Car Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Car") },
                                onClick = {
                                    showMenu = false
                                    onEditCarClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Car") },
                                onClick = {
                                    showMenu = false
                                    viewModel.showDeleteDialog()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            com.agcoding.cartrackingapp.presentation.expense.ExpandableFabMenu(
                onRefillClick = onAddRefillClick,
                onExpenseClick = onAddExpenseClick
            )
        }
    ) { paddingValues ->
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Car info header
                    item {
                        CarInfoCard(state.statistics)
                    }

                    // Statistics cards
                    item {
                        StatisticsGrid(state.statistics)
                    }

                    // Extra Info Section
                    item {
                        ExtraInfoSection(
                            car = state.statistics.car,
                            onEditClick = onEditCarClick
                        )
                    }

                    // Recent refills header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Refill History",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "See all",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable(onClick = onViewAllRefillsClick)
                            )
                        }
                    }

                    items(state.statistics.recentRefills) { refill ->
                        RefillCard(
                            refill = refill,
                            onClick = { onRefillClick(refill.id) }
                        )
                    }

                    // Expenses header
                    if (state.statistics.recentExpenses.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Expenses",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "See all",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable(onClick = onViewAllExpensesClick)
                                )
                            }
                        }

                        items(state.statistics.recentExpenses) { expense ->
                            ExpenseCard(
                                expense = expense,
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
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Delete Car") },
            text = { Text("Are you sure you want to delete this car? All refill records will also be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCar(onSuccess = onNavigateBack)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CarInfoCard(statistics: com.agcoding.cartrackingapp.domain.model.CarStatistics) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = statistics.car.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = statistics.car.licensePlate,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatisticsGrid(statistics: com.agcoding.cartrackingapp.domain.model.CarStatistics) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row - separate cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "Avg. Consumption",
                value = if (statistics.averageConsumption > 0) {
                    "${String.format("%.1f", statistics.averageConsumption)} L/100km"
                } else "N/A",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = Icons.Default.AttachMoney,
                label = "Total Cost",
                value = "€${String.format("%.2f", statistics.totalCost)}",
                modifier = Modifier.weight(1f)
            )
        }

        // Second row - separate cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Default.Route,
                label = "Total Distance",
                value = "${String.format("%.0f", statistics.totalDistance)} km",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = Icons.Default.Speed,
                label = "Odometer",
                value = "${String.format("%.0f", statistics.car.currentOdometer)} km",
                modifier = Modifier.weight(1f)
            )
        }

        // Third row - Cost per Kilometer (full width)
        val costPerKm = if (statistics.totalDistance > 0) {
            statistics.totalCost / statistics.totalDistance
        } else 0.0

        MetricCard(
            icon = Icons.Default.AttachMoney,
            label = "Cost per Kilometer",
            value = if (statistics.totalDistance > 0) {
                "€${String.format("%.3f", costPerKm)}/km"
            } else "N/A",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RefillCard(
    refill: FuelRefill,
    onClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Date and amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = dateFormat.format(Date(refill.timestamp)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "€${String.format("%.2f", refill.amountPaid)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Refill details row
            val pricePerLiter = refill.amountPaid / refill.litersAdded
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format("%.1f", refill.litersAdded)} L • €${String.format("%.3f", pricePerLiter)}/L • ${String.format("%.0f", refill.tripDistance)} km • ${String.format("%.1f", refill.fuelConsumption)} L/100km",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExtraInfoSection(
    car: com.agcoding.cartrackingapp.domain.model.Car,
    onEditClick: () -> Unit
) {
    val hasExtraInfo = car.insuranceExpirationDate != null ||
            car.kteoExpirationDate != null ||
            car.emissionsCardExpirationDate != null ||
            car.roadTaxAmount != null ||
            car.roadTaxDueDate != null ||
            car.lastServiceDate != null ||
            car.lastTireChangeDate != null ||
            !car.tireBrand.isNullOrBlank() ||
            !car.tireDimensions.isNullOrBlank() ||
            car.tireInstallationDate != null ||
            !car.tyreSize.isNullOrBlank()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Extra Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (!hasExtraInfo) {
            // Banner when no extra info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEditClick),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            text = "Add Extra Information",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add tires, insurance, KTEO, service dates, and more",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Add info",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            // Display extra info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

                    // Insurance Information
                    car.insuranceExpirationDate?.let { date ->
                        ExtraInfoItem(
                            label = "Insurance Expiration",
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Legal & Compliance
                    car.kteoExpirationDate?.let { date ->
                        ExtraInfoItem(
                            label = "KTEO Expiration",
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    car.emissionsCardExpirationDate?.let { date ->
                        ExtraInfoItem(
                            label = "Emissions Card Expiration",
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    car.roadTaxAmount?.let { amount ->
                        ExtraInfoItem(
                            label = "Road Tax Amount",
                            value = "€${String.format("%.2f", amount)}"
                        )
                    }

                    car.roadTaxDueDate?.let { date ->
                        ExtraInfoItem(
                            label = "Road Tax Due Date",
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Maintenance History
                    car.lastServiceDate?.let { date ->
                        ExtraInfoItem(
                            label = "Last Service",
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    car.lastTireChangeDate?.let { date ->
                        ExtraInfoItem(
                            label = "Last Tire Change",
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Tires Information
                    if (!car.tireBrand.isNullOrBlank()) {
                        ExtraInfoItem(
                            label = "Tire Brand / Model",
                            value = car.tireBrand
                        )
                    }

                    if (!car.tireDimensions.isNullOrBlank()) {
                        ExtraInfoItem(
                            label = "Tire Dimensions",
                            value = car.tireDimensions
                        )
                    }

                    car.tireInstallationDate?.let { date ->
                        ExtraInfoItem(
                            label = "Tire Installation Date",
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Legacy field (for backward compatibility)
                    if (!car.tyreSize.isNullOrBlank()) {
                        ExtraInfoItem(
                            label = "Tyre Size (Legacy)",
                            value = car.tyreSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtraInfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ExpenseCard(
    expense: com.agcoding.cartrackingapp.domain.model.Expense,
    onClick: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    text = expense.category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormat.format(Date(expense.timestamp)),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (expense.notes != null) {
                    Text(
                        text = expense.notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2
                    )
                }
            }

            Text(
                text = "€${String.format("%.2f", expense.amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

