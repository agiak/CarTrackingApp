package com.agcoding.cartrackingapp.presentation.expense

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(
    selectedCategory: String,
    quickPickCategories: List<String>,
    otherCategories: List<String>,
    dropdownExpanded: Boolean,
    categoryError: String?,
    onSelectCategory: (String) -> Unit,
    onToggleDropdown: () -> Unit,
    onDismissDropdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.expense_category),
            style = MaterialTheme.typography.titleSmall,
            color = if (categoryError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
        )

        if (categoryError != null) {
            Text(
                text = categoryError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick-pick chips
        if (quickPickCategories.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                quickPickCategories.forEach { name ->
                    FilterChip(
                        selected = selectedCategory == name,
                        onClick = { onSelectCategory(name) },
                        label = { Text(name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Card-style selector rather than a chip with a dropdown arrow: it shows
        // the current category at full width and reads as a field like the ones
        // below it, instead of looking like an inline menu. Tapping it opens the
        // picker sheet, where a category can also be created by typing.
        Card(
            onClick = onToggleDropdown,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                if (categoryError != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCategory.ifBlank {
                        stringResource(R.string.select_category)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selectedCategory.isNotBlank()) FontWeight.Medium
                                 else FontWeight.Normal,
                    color = if (selectedCategory.isNotBlank()) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    // The picker lists every category — not just the non-quick-pick ones — so a
    // category is always findable from one place, whether or not it is starred.
    if (dropdownExpanded) {
        val allCategories = remember(quickPickCategories, otherCategories) {
            (quickPickCategories + otherCategories).distinct().sorted()
        }
        CategoryPickerSheet(
            categories = allCategories,
            selectedCategory = selectedCategory,
            onSelect = onSelectCategory,
            onDismiss = onDismissDropdown
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "CategorySelector - No quick picks", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategorySelectorEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        CategorySelector(
            selectedCategory = "",
            quickPickCategories = emptyList(),
            otherCategories = listOf("Insurance", "Oil change", "Tire change", "Parking"),
            dropdownExpanded = false,
            categoryError = null,
            onSelectCategory = {},
            onToggleDropdown = {},
            onDismissDropdown = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "CategorySelector - With quick picks", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategorySelectorWithQuickPicks() {
    CarTrackingAppTheme(darkTheme = false) {
        CategorySelector(
            selectedCategory = "Insurance",
            quickPickCategories = listOf("Insurance", "Oil change", "Tire change"),
            otherCategories = listOf("Parking", "Toll", "Car wash", "Accessories"),
            dropdownExpanded = false,
            categoryError = null,
            onSelectCategory = {},
            onToggleDropdown = {},
            onDismissDropdown = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "CategorySelector - Custom category selected", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategorySelectorCustomSelected() {
    CarTrackingAppTheme(darkTheme = false) {
        CategorySelector(
            selectedCategory = "Window tinting",
            quickPickCategories = listOf("Insurance", "Oil change"),
            otherCategories = listOf("Parking", "Toll"),
            dropdownExpanded = false,
            categoryError = null,
            onSelectCategory = {},
            onToggleDropdown = {},
            onDismissDropdown = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
