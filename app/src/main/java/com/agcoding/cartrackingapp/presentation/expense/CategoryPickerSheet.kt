package com.agcoding.cartrackingapp.presentation.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Category picker presented as a bottom sheet instead of an anchored dropdown.
 *
 * A dropdown anchored to a chip gets cramped fast: it is limited to the chip's
 * width, scrolls awkwardly once there are many categories, and gives no room to
 * search. The sheet lists every category at full width, marks the current one,
 * filters as you type, and offers the custom-category entry in the same place.
 *
 * The search field only appears once the list is long enough to warrant it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerSheet(
    categories: List<String>,
    selectedCategory: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    onCustomCategory: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    val showSearch = categories.size > 6
    val visibleCategories = remember(categories, query) {
        if (query.isBlank()) categories
        else categories.filter { it.contains(query.trim(), ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.select_category),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }

            if (showSearch) {
                Spacer(modifier = Modifier.height(12.dp))
                StyledOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.search_category)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (visibleCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_categories_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    items(visibleCategories, key = { it }) { name ->
                        val isSelected = name == selectedCategory
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(name) }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (onCustomCategory != null) {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCustomCategory() }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = stringResource(R.string.custom_category),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Category Picker - Short list", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryPickerShort() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoryPickerSheet(
            categories = listOf("Insurance", "Parking", "Toll"),
            selectedCategory = "Parking",
            onSelect = {},
            onDismiss = {},
            onCustomCategory = {}
        )
    }
}

@Preview(name = "Category Picker - With search", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryPickerLong() {
    CarTrackingAppTheme(darkTheme = true) {
        CategoryPickerSheet(
            categories = listOf(
                "Insurance", "Oil change", "Tire change", "Parking",
                "Toll", "Car wash", "Accessories", "Road tax"
            ),
            selectedCategory = "Toll",
            onSelect = {},
            onDismiss = {},
            onCustomCategory = {}
        )
    }
}
