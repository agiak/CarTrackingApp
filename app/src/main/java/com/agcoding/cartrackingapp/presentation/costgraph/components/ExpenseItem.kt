package com.agcoding.cartrackingapp.presentation.costgraph.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.domain.model.CostItem
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseItem(expense: CostItem) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    StyledCard(
        modifier = Modifier.fillMaxWidth()
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
                    text = dateFormat.format(Date(expense.date)),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "€${String.format(Locale.getDefault(), "%.2f", expense.amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Expense Item - Fuel", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseItemFuel() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseItem(
            expense = CostItem(
                id = 1,
                category = "Fuel",
                amount = 65.50,
                date = System.currentTimeMillis(),
                description = "Gas station",
                carName = "Toyota Corolla"
            )
        )
    }
}

@Preview(name = "Expense Item - Service", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseItemService() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseItem(
            expense = CostItem(
                id = 2,
                category = "Oil Change",
                amount = 120.00,
                date = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                description = "Regular maintenance",
                carName = "Honda Civic"
            )
        )
    }
}

@Preview(name = "Expense Item - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseItemDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ExpenseItem(
            expense = CostItem(
                id = 3,
                category = "Tire Change",
                amount = 450.00,
                date = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000L,
                description = "All season tires",
                carName = "BMW 320i"
            )
        )
    }
}
