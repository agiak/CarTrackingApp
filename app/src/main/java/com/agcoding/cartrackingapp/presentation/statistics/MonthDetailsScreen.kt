package com.agcoding.cartrackingapp.presentation.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.ExpenseItemCard
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.util.formatNumber
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
            StyledTopAppBar(
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
                        // Left side: Summary and Insights (40%)
                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Summary Card
                            MonthSummaryCard(state)

                            // Insights Card
                            MonthInsightsCard(state)
                        }

                        // Right side: Refills and Expenses Lists (60%)
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
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
                } else {
                    // Original single column layout for portrait phones
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
                    text = stringResource(R.string.currency_eur_format, state.totalCost.formatNumber(2)),
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
                    value = stringResource(R.string.currency_eur_format, state.refillsCost.formatNumber(2)),
                    icon = Icons.Default.LocalGasStation,
                    iconTint = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = stringResource(R.string.expenses),
                    value = stringResource(R.string.currency_eur_format, state.expensesCost.formatNumber(2)),
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
                        text = stringResource(R.string.liters_format, state.totalLiters.formatNumber(1)),
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
                        text = stringResource(R.string.kilometers_format, state.totalDistance.formatNumber(0)),
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
                                state.averageConsumption.formatNumber(1)
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
    var showFuelPercentageTooltip by remember { mutableStateOf(false) }

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
                        (state.refillsCost / state.totalLiters).formatNumber(2)
                    )
                )
            }

            // Cost per km
            if (state.totalDistance > 0) {
                InsightRow(
                    label = stringResource(R.string.cost_per_km),
                    value = stringResource(
                        R.string.cost_per_km_format,
                        (state.totalCost / state.totalDistance).formatNumber(3)
                    )
                )
            }

            // Fuel percentage with tooltip
            if (state.totalCost > 0) {
                val fuelPercentage = (state.refillsCost / state.totalCost) * 100
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InsightRow(
                        label = stringResource(R.string.fuel_percentage_of_spending),
                        value = stringResource(R.string.km_per_l_format, fuelPercentage.formatNumber(1)),
                        showInfoIcon = true,
                        onInfoClick = { showFuelPercentageTooltip = !showFuelPercentageTooltip } // Toggle behavior
                    )

                    // Inline tooltip - appears directly below the row
                    if (showFuelPercentageTooltip) {
                        InfoTooltip(
                            message = stringResource(R.string.fuel_percentage_tooltip),
                            onDismiss = { showFuelPercentageTooltip = false }
                        )
                    }
                }
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
                        biggestRefill.amountPaid.formatNumber(2)
                    )
                )
            }

            // Biggest expense
            state.expenses.maxByOrNull { it.amount }?.let { biggestExpense ->
                InsightRow(
                    label = stringResource(R.string.biggest_expense),
                    value = stringResource(
                        R.string.currency_eur_format,
                        biggestExpense.amount.formatNumber(2)
                    )
                )
            }
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String,
    showInfoIcon: Boolean = false,
    onInfoClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showInfoIcon && onInfoClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.info),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onInfoClick
                        ),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoTooltip(
    message: String,
    onDismiss: () -> Unit
) {
    val tooltipColor = MaterialTheme.colorScheme.surfaceContainerHigh
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Arrow pointing UP to the info icon
        Box(
            modifier = Modifier
                .padding(start = 100.dp) // Align with the info icon position
                .size(12.dp, 6.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val path = Path().apply {
                    // Triangle pointing UP
                    moveTo(0f, size.height)
                    lineTo(size.width / 2, 0f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(
                    path = path,
                    color = tooltipColor
                )
            }
        }

        // Tooltip card - compact design
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(6.dp)),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Info icon - smaller
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Message text - more compact
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Close button - smaller
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(14.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        )
                )
            }
        }
    }
}
