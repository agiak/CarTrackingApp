package com.agcoding.cartrackingapp.presentation.expensehistory.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.presentation.components.ExpenseItemCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ExpenseHistoryContent(
    expenses: List<Expense>,
    availableCategories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    sortOptionName: String,
    onExpenseClick: (Long) -> Unit,
    useSplitView: Boolean,
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty() && selectedCategory == null) {
        // No expenses at all
        Box(
            modifier = modifier.fillMaxSize(),
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
            modifier = modifier
                .fillMaxSize()
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
                if (availableCategories.isNotEmpty()) {
                    CategoryFiltersCard(
                        availableCategories = availableCategories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Summary stats
                if (expenses.isNotEmpty()) {
                    val totalCost = expenses.sumOf { it.amount }
                    val serviceCount = expenses.count {
                        it.category == "Service" || it.category.contains("service", ignoreCase = true)
                    }
                    val otherCount = expenses.size - serviceCount

                    ExpenseSummaryCard(
                        expenseCount = expenses.size,
                        totalCost = totalCost,
                        serviceCount = serviceCount,
                        otherCount = otherCount,
                        selectedCategory = selectedCategory,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Sort info
                SortInfoCard(
                    sortOptionName = sortOptionName,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Right side: Expenses list (65%)
            if (expenses.isEmpty()) {
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
                        TextButton(onClick = { onCategorySelected(null) }) {
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
                    items(expenses) { expense ->
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
        // Single column layout for portrait phones
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Category filter chips
            if (availableCategories.isNotEmpty()) {
                CategoryFilterChips(
                    availableCategories = availableCategories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }

            if (expenses.isEmpty()) {
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
                        TextButton(onClick = { onCategorySelected(null) }) {
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
                                text = stringResource(R.string.sorted_by_format, sortOptionName),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = pluralStringResource(
                                    R.plurals.expenses_count,
                                    expenses.size,
                                    expenses.size
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(expenses) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            carName = null,
                            onClick = { onExpenseClick(expense.id) }
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Expense History - Phone With Data", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewExpenseHistoryPhoneWithData() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseHistoryContent(
            expenses = listOf(
                Expense(
                    id = 1,
                    carId = 1,
                    category = "Fuel",
                    amount = 65.50,
                    timestamp = System.currentTimeMillis(),
                    notes = "Shell station"
                ),
                Expense(
                    id = 2,
                    carId = 1,
                    category = "Service & Maintenance",
                    amount = 120.00,
                    timestamp = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                    notes = "Oil change"
                )
            ),
            availableCategories = listOf("Fuel", "Service & Maintenance", "Insurance"),
            selectedCategory = null,
            onCategorySelected = {},
            sortOptionName = "Date (Newest First)",
            onExpenseClick = {},
            useSplitView = false
        )
    }
}

@Preview(name = "Expense History - Tablet Split View", showBackground = true, widthDp = 900, heightDp = 600)
@Composable
private fun PreviewExpenseHistoryTabletSplitView() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseHistoryContent(
            expenses = listOf(
                Expense(
                    id = 1,
                    carId = 1,
                    category = "Fuel",
                    amount = 65.50,
                    timestamp = System.currentTimeMillis(),
                    notes = "Shell station"
                ),
                Expense(
                    id = 2,
                    carId = 1,
                    category = "Fuel",
                    amount = 72.30,
                    timestamp = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                    notes = "BP station"
                ),
                Expense(
                    id = 3,
                    carId = 1,
                    category = "Service & Maintenance",
                    amount = 120.00,
                    timestamp = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000L,
                    notes = "Oil change"
                )
            ),
            availableCategories = listOf("Fuel", "Service & Maintenance", "Insurance", "Parking & Tolls"),
            selectedCategory = null,
            onCategorySelected = {},
            sortOptionName = "Date (Newest First)",
            onExpenseClick = {},
            useSplitView = true
        )
    }
}

@Preview(name = "Expense History - Empty State", showBackground = true, widthDp = 380, heightDp = 600)
@Composable
private fun PreviewExpenseHistoryEmptyState() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseHistoryContent(
            expenses = emptyList(),
            availableCategories = emptyList(),
            selectedCategory = null,
            onCategorySelected = {},
            sortOptionName = "Date (Newest First)",
            onExpenseClick = {},
            useSplitView = false
        )
    }
}

@Preview(name = "Expense History - Dark Mode", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewExpenseHistoryDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ExpenseHistoryContent(
            expenses = listOf(
                Expense(
                    id = 1,
                    carId = 1,
                    category = "Fuel",
                    amount = 65.50,
                    timestamp = System.currentTimeMillis(),
                    notes = "Shell station"
                )
            ),
            availableCategories = listOf("Fuel", "Service & Maintenance"),
            selectedCategory = null,
            onCategorySelected = {},
            sortOptionName = "Amount (Highest First)",
            onExpenseClick = {},
            useSplitView = false
        )
    }
}
