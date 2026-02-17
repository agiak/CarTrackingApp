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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
    onDefaultCarSet: () -> Unit = {},
    viewModel: CarDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

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

                                // Set as Default button
                                androidx.compose.material3.OutlinedButton(
                                    onClick = {
                                        viewModel.setDefaultCar()
                                        onDefaultCarSet()
                                    },
                                    enabled = !state.statistics.car.isDefault,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        if (state.statistics.car.isDefault)
                                            stringResource(R.string.default_car)
                                        else
                                            stringResource(R.string.set_as_default)
                                    )
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

                        // Set as Default button
                        item {
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    viewModel.setDefaultCar()
                                    onDefaultCarSet()
                                },
                                enabled = !state.statistics.car.isDefault,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (state.statistics.car.isDefault)
                                        stringResource(R.string.default_car)
                                    else
                                        stringResource(R.string.set_as_default)
                                )
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
