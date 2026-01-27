package com.agcoding.cartrackingapp.presentation.expensehistory.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun CategoryFilterChips(
    availableCategories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text(stringResource(R.string.all)) }
        )

        // Category chips
        availableCategories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) }
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Category Filter Chips - None Selected", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryFilterChipsNoneSelected() {
    CarTrackingAppTheme(darkTheme = false) {
        var selected by remember { mutableStateOf<String?>(null) }

        CategoryFilterChips(
            availableCategories = listOf("Fuel", "Service & Maintenance", "Insurance", "Parking & Tolls", "Other"),
            selectedCategory = selected,
            onCategorySelected = { selected = it }
        )
    }
}

@Preview(name = "Category Filter Chips - Selected", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryFilterChipsSelected() {
    CarTrackingAppTheme(darkTheme = false) {
        var selected by remember { mutableStateOf<String?>("Fuel") }

        CategoryFilterChips(
            availableCategories = listOf("Fuel", "Service & Maintenance", "Insurance", "Parking & Tolls"),
            selectedCategory = selected,
            onCategorySelected = { selected = it }
        )
    }
}

@Preview(name = "Category Filter Chips - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryFilterChipsDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var selected by remember { mutableStateOf<String?>("Insurance") }

        CategoryFilterChips(
            availableCategories = listOf("Fuel", "Service & Maintenance", "Insurance", "Parking & Tolls", "Other"),
            selectedCategory = selected,
            onCategorySelected = { selected = it }
        )
    }
}
