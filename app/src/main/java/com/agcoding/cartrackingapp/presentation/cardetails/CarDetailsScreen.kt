package com.agcoding.cartrackingapp.presentation.cardetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.ExpenseItemCard
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledCard
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                val configuration = LocalConfiguration.current
                val screenWidthDp = configuration.screenWidthDp
                val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
                val useSplitView = screenWidthDp >= 600 || isLandscape

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

                            // Action buttons
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    androidx.compose.material3.OutlinedButton(
                                        onClick = onEditCarClick,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.edit_car))
                                    }

                                    androidx.compose.material3.OutlinedButton(
                                        onClick = { viewModel.showDeleteDialog() },
                                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.delete))
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    androidx.compose.material3.OutlinedButton(
                                        onClick = onAddExpenseClick,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.add_expense))
                                    }

                                    androidx.compose.material3.OutlinedButton(
                                        onClick = onAddRefillClick,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalGasStation,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.add_refill))
                                    }
                                }
                            }

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
                            contentPadding = PaddingValues(bottom = 16.dp),
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
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Car header with icon
                        item {
                            CarHeaderCard(state.statistics)
                        }

                        // Edit and Delete buttons
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = onEditCarClick,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.edit_car))
                                }

                                androidx.compose.material3.OutlinedButton(
                                    onClick = { viewModel.showDeleteDialog() },
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.delete))
                                }
                            }
                        }

                        // Expense and Refill action buttons
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = onAddExpenseClick,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.add_expense))
                                }

                                androidx.compose.material3.OutlinedButton(
                                    onClick = onAddRefillClick,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.add_refill))
                                }
                            }
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
                                carName = null, // Don't show car name in car details screen
                                onClick = { onRefillClick(refill.id) }
                            )
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
private fun CarHeaderCard(statistics: com.agcoding.cartrackingapp.domain.model.CarStatistics) {
    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bigger car icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StyledCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        border = null
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .padding(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Car info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = statistics.car.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statistics.car.licensePlate,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Separator line
            Spacer(modifier = Modifier.height(16.dp))
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Current odometer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.current_odometer),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${String.format("%,d", statistics.car.currentOdometer.toInt())} km",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun QuickStatsGrid(statistics: com.agcoding.cartrackingapp.domain.model.CarStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            label = stringResource(R.string.avg_consumption_short),
            value = if (statistics.averageConsumption > 0) {
                String.format("%.1f", statistics.averageConsumption)
            } else "-",
            unit = "L/100km",
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            icon = Icons.Default.Route,
            label = stringResource(R.string.distance),
            value = String.format("%,d", statistics.totalDistance.toInt()),
            unit = "km",
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            icon = Icons.Default.AttachMoney,
            label = "Cost/km",
            value = if (statistics.costPerKilometer > 0) {
                String.format("€%.2f", statistics.costPerKilometer)
            } else "€0.00",
            unit = "",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier
            .height(160.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StyledCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        border = null
                    ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TotalSpendingCard(statistics: com.agcoding.cartrackingapp.domain.model.CarStatistics) {
    val totalSpending = statistics.totalCost + statistics.serviceExpensesCost + statistics.otherExpensesCost

    StyledCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.total_spending),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "€${String.format("%.2f", totalSpending)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpendingBreakdown(
                    label = stringResource(R.string.fuel),
                    amount = statistics.totalCost
                )
                SpendingBreakdown(
                    label = stringResource(R.string.service),
                    amount = statistics.serviceExpensesCost
                )
                SpendingBreakdown(
                    label = stringResource(R.string.other),
                    amount = statistics.otherExpensesCost
                )
            }
        }
    }
}

@Composable
private fun SpendingBreakdown(label: String, amount: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "€${String.format("%.2f", amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun IncompleteInformationBanner(
    car: com.agcoding.cartrackingapp.domain.model.Car,
    onAddInformationClick: () -> Unit
) {
    StyledCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddInformationClick),
        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StyledCard(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        border = null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.complete_car_information),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Missing fields list
            val missingFields = buildList {
                if (car.insuranceExpirationDate == null) add(stringResource(R.string.insurance_expiration))
                if (car.kteoExpirationDate == null) add(stringResource(R.string.kteo_expiration))
                if (car.emissionsCardExpirationDate == null) add(stringResource(R.string.emissions_card_expiration))
                if (car.roadTaxAmount == null) add(stringResource(R.string.road_tax_amount))
                if (car.roadTaxDueDate == null) add(stringResource(R.string.road_tax_due_date))
                if (car.lastServiceDate == null) add(stringResource(R.string.last_service))
                if (car.lastTireChangeDate == null) add(stringResource(R.string.last_tire_change))
                if (car.tireBrand.isNullOrBlank()) add(stringResource(R.string.tire_brand_model))
                if (car.tireDimensions.isNullOrBlank()) add(stringResource(R.string.tire_dimensions))
                if (car.tireInstallationDate == null) add(stringResource(R.string.tire_installation_date))
            }

            Text(
                text = stringResource(R.string.missing_fields_label, missingFields.size),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = missingFields.take(5).joinToString(", "),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )

            if (missingFields.size > 5) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.and_more_fields, missingFields.size - 5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TabsSection(statistics: com.agcoding.cartrackingapp.domain.model.CarStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabButton(
            text = "${stringResource(R.string.refills)} (${statistics.totalRefills})",
            isSelected = true,
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "${stringResource(R.string.services)} (${statistics.serviceExpenseCount})",
            isSelected = false,
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "${stringResource(R.string.other)} (${statistics.otherExpenseCount})",
            isSelected = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier,
        containerColor = if (isSelected)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = if (isSelected)
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier
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
                text = stringResource(R.string.extra_information),
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
                            text = stringResource(R.string.add_extra_information),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.add_extra_information_desc),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.add_info),
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
                            label = stringResource(R.string.insurance_expiration),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Legal & Compliance
                    car.kteoExpirationDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.kteo_expiration),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    car.emissionsCardExpirationDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.emissions_card_expiration),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    car.roadTaxAmount?.let { amount ->
                        ExtraInfoItem(
                            label = stringResource(R.string.road_tax_amount),
                            value = "€${String.format("%.2f", amount)}"
                        )
                    }

                    car.roadTaxDueDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.road_tax_due_date),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Maintenance History
                    car.lastServiceDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.last_service),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    car.lastTireChangeDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.last_tire_change),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Tires Information
                    if (!car.tireBrand.isNullOrBlank()) {
                        ExtraInfoItem(
                            label = stringResource(R.string.tire_brand_model),
                            value = car.tireBrand
                        )
                    }

                    if (!car.tireDimensions.isNullOrBlank()) {
                        ExtraInfoItem(
                            label = stringResource(R.string.tire_dimensions),
                            value = car.tireDimensions
                        )
                    }

                    car.tireInstallationDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.tire_installation_date),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Legacy field (for backward compatibility)
                    if (!car.tyreSize.isNullOrBlank()) {
                        ExtraInfoItem(
                            label = stringResource(R.string.tyre_size_legacy),
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

