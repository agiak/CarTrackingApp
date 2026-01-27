package com.agcoding.cartrackingapp.presentation.expensehistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun CategoryFiltersCard(
    availableCategories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    StyledCard(modifier = modifier) {
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
                    onClick = { onCategorySelected(null) },
                    label = { Text(stringResource(R.string.all)) },
                    modifier = Modifier.fillMaxWidth()
                )

                availableCategories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Category Filters - None Selected", showBackground = true, widthDp = 300)
@Composable
private fun PreviewCategoryFiltersNoneSelected() {
    CarTrackingAppTheme(darkTheme = false) {
        var selected by remember { mutableStateOf<String?>(null) }

        CategoryFiltersCard(
            availableCategories = listOf("Fuel", "Service & Maintenance", "Insurance", "Parking & Tolls"),
            selectedCategory = selected,
            onCategorySelected = { selected = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Category Filters - One Selected", showBackground = true, widthDp = 300)
@Composable
private fun PreviewCategoryFiltersSelected() {
    CarTrackingAppTheme(darkTheme = false) {
        var selected by remember { mutableStateOf<String?>("Fuel") }

        CategoryFiltersCard(
            availableCategories = listOf("Fuel", "Service & Maintenance", "Insurance", "Parking & Tolls", "Other"),
            selectedCategory = selected,
            onCategorySelected = { selected = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Category Filters - Dark", showBackground = true, widthDp = 300)
@Composable
private fun PreviewCategoryFiltersDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var selected by remember { mutableStateOf<String?>(null) }

        CategoryFiltersCard(
            availableCategories = listOf("Fuel", "Service & Maintenance", "Insurance"),
            selectedCategory = selected,
            onCategorySelected = { selected = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}
