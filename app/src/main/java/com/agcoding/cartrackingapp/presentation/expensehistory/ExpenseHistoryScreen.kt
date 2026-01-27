package com.agcoding.cartrackingapp.presentation.expensehistory

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.ExpenseItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHistoryScreen(
    onNavigateBack: () -> Unit,
    onExpenseClick: (Long) -> Unit,
    viewModel: com.agcoding.cartrackingapp.presentation.expensehistory.ExpenseHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.expense_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
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
                        ExpenseSortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.displayNameResId)) },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    showSortMenu = false
                                },
                                trailingIcon = if (sortOption == option) {
                                    { Text("✓") }
                                } else null
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ExpenseHistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ExpenseHistoryUiState.Success -> {
                val configuration = LocalConfiguration.current
                val screenWidthDp = configuration.screenWidthDp
                val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
                val useSplitView = screenWidthDp >= 600 || isLandscape

                if (state.expenses.isEmpty() && selectedCategory == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_expenses_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (useSplitView) {
                    // Split view for tablets and landscape
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left side: Category filters and summary (35%)
                        Column(
                            modifier = Modifier
                                .weight(0.35f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Categories card
                            if (state.availableCategories.isNotEmpty()) {
                                StyledCard {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Category,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.expense_show_categories),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Category chips in vertical layout
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            FilterChip(
                                                selected = selectedCategory == null,
                                                onClick = { viewModel.setSelectedCategory(null) },
                                                label = { Text(stringResource(R.string.all)) },
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            state.availableCategories.forEach { category ->
                                                FilterChip(
                                                    selected = selectedCategory == category,
                                                    onClick = { viewModel.setSelectedCategory(category) },
                                                    label = { Text(category) },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Summary stats
                            if (state.expenses.isNotEmpty()) {
                                val totalCost = state.expenses.sumOf { it.amount.toDouble() }
                                val serviceCount = state.expenses.count { it.category == "Service" || it.category.contains("service", ignoreCase = true) }
                                val otherCount = state.expenses.size - serviceCount

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

                                        ExpenseSummaryItem(
                                            icon = Icons.Default.Receipt,
                                            label = stringResource(R.string.expense_history_title),
                                            value = "${state.expenses.size}"
                                        )

                                        ExpenseSummaryItem(
                                            icon = Icons.Default.AttachMoney,
                                            label = stringResource(R.string.total_cost),
                                            value = "€%.2f".format(totalCost)
                                        )

                                        ExpenseSummaryItem(
                                            icon = Icons.Default.Build,
                                            label = stringResource(R.string.services),
                                            value = "$serviceCount"
                                        )

                                        ExpenseSummaryItem(
                                            icon = Icons.Default.Category,
                                            label = stringResource(R.string.other),
                                            value = "$otherCount"
                                        )

                                        if (selectedCategory != null) {
                                            ExpenseSummaryItem(
                                                icon = Icons.Default.FilterList,
                                                label = stringResource(R.string.filter),
                                                value = selectedCategory ?: ""
                                            )
                                        }
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
                                        text = stringResource(sortOption.displayNameResId),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Right side: Expenses list (65%)
                        if (state.expenses.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(0.65f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = selectedCategory?.let {
                                            stringResource(R.string.no_expenses_in_category_format, it)
                                        } ?: stringResource(R.string.no_expenses_yet),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(onClick = { viewModel.setSelectedCategory(null) }) {
                                        Text(stringResource(R.string.show_all_expenses))
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(0.65f)
                                    .fillMaxHeight(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.expenses) { expense ->
                                    ExpenseItemCard(
                                        expense = expense,
                                        carName = null,
                                        onClick = { onExpenseClick(expense.id) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Original single column layout for portrait phones
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Category filter chips
                        if (state.availableCategories.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // "All" chip
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { viewModel.setSelectedCategory(null) },
                                    label = { Text(stringResource(R.string.all)) }
                                )

                                // Category chips
                                state.availableCategories.forEach { category ->
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = { viewModel.setSelectedCategory(category) },
                                        label = { Text(category) }
                                    )
                                }
                            }
                        }

                        if (state.expenses.isEmpty()) {
                            // No expenses for selected category
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = selectedCategory?.let {
                                            stringResource(R.string.no_expenses_in_category_format, it)
                                        } ?: stringResource(R.string.no_expenses_yet),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(onClick = { viewModel.setSelectedCategory(null) }) {
                                        Text(stringResource(R.string.show_all_expenses))
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Sort and filter info
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.sorted_by_format, stringResource(sortOption.displayNameResId)),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = androidx.compose.ui.res.pluralStringResource(
                                                R.plurals.expenses_count,
                                                state.expenses.size,
                                                state.expenses.size
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                items(state.expenses) { expense ->
                                    ExpenseItemCard(
                                        expense = expense,
                                        carName = null, // Don't show car name in single car view
                                        onClick = { onExpenseClick(expense.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is ExpenseHistoryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseSummaryItem(
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
