package com.agcoding.cartrackingapp.presentation.expensehistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney

@Composable
fun ExpenseSummaryCard(
    expenseCount: Int,
    totalCost: Double,
    serviceCount: Int,
    otherCount: Int,
    selectedCategory: String?,
    modifier: Modifier = Modifier
) {
    StyledCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = null,
        modifier = modifier
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
                value = "$expenseCount"
            )

            ExpenseSummaryItem(
                icon = Icons.Default.EuroSymbol,
                label = stringResource(R.string.total_cost),
                value = totalCost.formatMoney()
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
                    value = selectedCategory
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Expense Summary Card - No Filter", showBackground = true, widthDp = 300)
@Composable
private fun PreviewExpenseSummaryCardNoFilter() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseSummaryCard(
            expenseCount = 45,
            totalCost = 2840.75,
            serviceCount = 12,
            otherCount = 33,
            selectedCategory = null,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Expense Summary Card - With Filter", showBackground = true, widthDp = 300)
@Composable
private fun PreviewExpenseSummaryCardWithFilter() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseSummaryCard(
            expenseCount = 18,
            totalCost = 1250.50,
            serviceCount = 5,
            otherCount = 13,
            selectedCategory = "Fuel",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Expense Summary Card - Dark", showBackground = true, widthDp = 300)
@Composable
private fun PreviewExpenseSummaryCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ExpenseSummaryCard(
            expenseCount = 28,
            totalCost = 1895.25,
            serviceCount = 8,
            otherCount = 20,
            selectedCategory = "Service & Maintenance",
            modifier = Modifier.padding(16.dp)
        )
    }
}
