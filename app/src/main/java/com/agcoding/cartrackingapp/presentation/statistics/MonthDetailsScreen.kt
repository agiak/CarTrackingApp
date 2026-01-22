package com.agcoding.cartrackingapp.presentation.statistics

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.ExpenseItemCard
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDetailsScreen(
    month: Int,
    year: Int,
    onNavigateBack: () -> Unit,
    onRefillClick: (Long) -> Unit = {},
    onExpenseClick: (Long) -> Unit = {},
    viewModel: MonthDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(month, year) {
        viewModel.loadMonthData(month, year)
    }

    val calendar = Calendar.getInstance().apply { set(year, month, 1) }
    val monthTitlePattern = stringResource(R.string.date_format_full)
    val monthYearPattern = stringResource(R.string.date_format_month_year)
    val monthName = remember(monthTitlePattern, monthYearPattern, month, year) {
        SimpleDateFormat(monthYearPattern, Locale.getDefault()).format(calendar.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(monthName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is MonthDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is MonthDetailsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Card
                    item {
                        MonthSummaryCard(state)
                    }

                    // Insights Card
                    item {
                        MonthInsightsCard(state)
                    }

                    // Refills Section
                    if (state.refills.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.refills_label) + " (${state.refills.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        items(state.refills) { refill ->
                            RefillItemCard(
                                refill = refill,
                                carName = state.carNames[refill.carId] ?: stringResource(R.string.unknown_car),
                                onClick = { onRefillClick(refill.id) }
                            )
                        }
                    }

                    // Expenses Section
                    if (state.expenses.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.expenses_label) + " (${state.expenses.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        items(state.expenses) { expense ->
                            ExpenseItemCard(
                                expense = expense,
                                carName = state.carNames[expense.carId] ?: stringResource(R.string.unknown_car),
                                onClick = { onExpenseClick(expense.id) }
                            )
                        }
                    }

                    // Empty state
                    if (state.refills.isEmpty() && state.expenses.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.no_transactions_this_month),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            is MonthDetailsUiState.Error -> {
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
}

@Composable
private fun MonthSummaryCard(state: MonthDetailsUiState.Success) {
    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.monthly_summary),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total spending
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.total_spending),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.currency_eur_format, String.format("%.2f", state.totalCost)),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = stringResource(R.string.fuel),
                    value = stringResource(R.string.currency_eur_format, String.format("%.2f", state.refillsCost)),
                    icon = Icons.Default.LocalGasStation,
                    iconTint = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = stringResource(R.string.expenses),
                    value = stringResource(R.string.currency_eur_format, String.format("%.2f", state.expensesCost)),
                    icon = Icons.Default.Receipt,
                    iconTint = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.liters_label),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.liters_format, String.format("%.1f", state.totalLiters)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.distance),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.kilometers_format, String.format("%.0f", state.totalDistance)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.consumption_label),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (state.averageConsumption > 0)
                            stringResource(
                                R.string.consumption_l_per_100km_format,
                                String.format("%.1f", state.averageConsumption)
                            )
                        else stringResource(R.string.not_available),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MonthInsightsCard(state: MonthDetailsUiState.Success) {
    StyledCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.insights_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Avg price per liter
            if (state.totalLiters > 0) {
                InsightRow(
                    label = stringResource(R.string.average_price_per_liter),
                    value = stringResource(
                        R.string.currency_eur_format,
                        String.format("%.3f", state.refillsCost / state.totalLiters)
                    )
                )
            }

            // Cost per km
            if (state.totalDistance > 0) {
                InsightRow(
                    label = stringResource(R.string.cost_per_km),
                    value = stringResource(
                        R.string.cost_per_km_format,
                        String.format("%.3f", state.totalCost / state.totalDistance)
                    )
                )
            }

            // Fuel percentage
            if (state.totalCost > 0) {
                val fuelPercentage = (state.refillsCost / state.totalCost) * 100
                InsightRow(
                    label = stringResource(R.string.fuel_percentage_of_spending),
                    value = stringResource(R.string.km_per_l_format, String.format("%.1f", fuelPercentage))
                )
            }

            // Cars used
            if (state.carNames.isNotEmpty()) {
                InsightRow(
                    label = stringResource(R.string.vehicles_with_activity),
                    value = "${state.carNames.size}"
                )
            }

            // Biggest refill
            state.refills.maxByOrNull { it.amountPaid }?.let { biggestRefill ->
                InsightRow(
                    label = stringResource(R.string.biggest_refill),
                    value = stringResource(
                        R.string.currency_eur_format,
                        String.format("%.2f", biggestRefill.amountPaid)
                    )
                )
            }

            // Biggest expense
            state.expenses.maxByOrNull { it.amount }?.let { biggestExpense ->
                InsightRow(
                    label = stringResource(R.string.biggest_expense),
                    value = stringResource(
                        R.string.currency_eur_format,
                        String.format("%.2f", biggestExpense.amount)
                    )
                )
            }
        }
    }
}

@Composable
private fun InsightRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

