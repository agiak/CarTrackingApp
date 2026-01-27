package com.agcoding.cartrackingapp.presentation.refillhistory

import androidx.annotation.StringRes
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledCard

enum class RefillSortOption(@StringRes val labelRes: Int) {
    MOST_RECENT(R.string.most_recent),
    OLDEST(R.string.oldest),
    MOST_EXPENSIVE(R.string.most_expensive),
    LEAST_EXPENSIVE(R.string.least_expensive),
    BEST_CONSUMPTION(R.string.best_consumption),
    WORST_CONSUMPTION(R.string.worst_consumption)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefillHistoryScreen(
    onNavigateBack: () -> Unit,
    onRefillClick: (Long) -> Unit,
    viewModel: RefillHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.refill_history_title)) },
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
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = stringResource(R.string.sort)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            RefillSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(option.labelRes),
                                            fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedSort == option)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is RefillHistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RefillHistoryUiState.Success -> {
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
                        // Left side: Summary stats (35%)
                        Column(
                            modifier = Modifier
                                .weight(0.35f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header card
                            StyledCard {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = state.carName,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.refill_history_title),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Summary stats
                            if (state.refills.isNotEmpty()) {
                                val totalCost = state.refills.sumOf { it.amountPaid }
                                val totalLiters = state.refills.sumOf { it.litersAdded }
                                val totalDistance = state.refills.sumOf { it.tripDistance }
                                val avgConsumption = if (totalDistance > 0) (totalLiters / totalDistance) * 100 else 0.0

                                StyledCard(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    border = null
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.total_spending),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        SummaryItem(
                                            icon = Icons.Default.LocalGasStation,
                                            label = stringResource(R.string.total_refills),
                                            value = "${state.refills.size}"
                                        )

                                        SummaryItem(
                                            icon = Icons.Default.AttachMoney,
                                            label = stringResource(R.string.total_cost),
                                            value = "€%.2f".format(totalCost)
                                        )

                                        SummaryItem(
                                            icon = Icons.Default.LocalGasStation,
                                            label = stringResource(R.string.total_liters_label),
                                            value = "%.1f L".format(totalLiters)
                                        )

                                        SummaryItem(
                                            icon = Icons.Default.Route,
                                            label = stringResource(R.string.total_distance),
                                            value = "%.0f km".format(totalDistance)
                                        )

                                        SummaryItem(
                                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                                            label = stringResource(R.string.avg_consumption),
                                            value = if (avgConsumption > 0) "%.1f L/100km".format(avgConsumption) else "-"
                                        )
                                    }
                                }
                            }

                            // Sort info
                            StyledCard {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.sort),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(selectedSort.labelRes),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Right side: Refills list (65%)
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.65f)
                                .fillMaxHeight(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.refills) { refill ->
                                RefillItemCard(
                                    refill = refill,
                                    carName = null,
                                    onClick = { onRefillClick(refill.id) }
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.carName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = stringResource(
                                        R.string.refills_sorted_by_format,
                                        state.refills.size,
                                        stringResource(selectedSort.labelRes)
                                    ),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Refills list
                        items(state.refills) { refill ->
                            RefillItemCard(
                                refill = refill,
                                carName = null, // Don't show car name in single car view
                                onClick = { onRefillClick(refill.id) }
                            )
                        }
                    }
                }
            }

            is RefillHistoryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }

            is RefillHistoryUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_refills_yet),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.add_first_refill),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
