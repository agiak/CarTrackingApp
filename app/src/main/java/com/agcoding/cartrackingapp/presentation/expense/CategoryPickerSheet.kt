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

/** Longest a category name may be. Keeps chips and list rows readable. */
private const val MAX_CATEGORY_LENGTH = 30

/**
 * Category picker with type-to-create.
 *
 * One field does both jobs: it filters the list as you type, and if what you
 * typed is not already a category it offers to create it. That replaces the old
 * separate "Custom" chip and free-text field, which let you type a name blind —
 * so "parking" could be created next to an existing "Parking" and split the same
 * expenses across two categories in the statistics.
 *
 * The create row is offered only when nothing matches case-insensitively, so an
 * existing category is always reused rather than duplicated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerSheet(
    categories: List<String>,
    selectedCategory: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    val trimmedQuery = query.trim()
    val visibleCategories = remember(categories, trimmedQuery) {
        if (trimmedQuery.isEmpty()) categories
        else categories.filter { it.contains(trimmedQuery, ignoreCase = true) }
    }
    // Offer creation only for a name that does not already exist in any casing.
    val canCreate = trimmedQuery.isNotEmpty() &&
        categories.none { it.equals(trimmedQuery, ignoreCase = true) }

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

            Spacer(modifier = Modifier.height(12.dp))

            StyledOutlinedTextField(
                value = query,
                onValueChange = { if (it.length <= MAX_CATEGORY_LENGTH) query = it },
                placeholder = { Text(stringResource(R.string.category_search_or_create)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Tells the user the field doubles as "create", so they do not go
            // hunting for a separate custom-category control.
            Text(
                text = stringResource(R.string.category_type_to_create_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Create row, pinned above the results while it applies.
            if (canCreate) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(trimmedQuery) }
                        .padding(vertical = 14.dp),
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
                        text = stringResource(R.string.category_create_format, trimmedQuery),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider()
            }

            if (visibleCategories.isEmpty() && !canCreate) {
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
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Category Picker", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryPicker() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoryPickerSheet(
            categories = listOf("Insurance", "Parking", "Small service", "Toll"),
            selectedCategory = "Parking",
            onSelect = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Category Picker - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryPickerDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CategoryPickerSheet(
            categories = listOf(
                "Insurance", "Oil change", "Tire change", "Parking",
                "Toll", "Car wash", "Accessories", "Road tax"
            ),
            selectedCategory = "Toll",
            onSelect = {},
            onDismiss = {}
        )
    }
}
