package com.agcoding.cartrackingapp.presentation.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    showCustomField: Boolean,
    customText: String,
    categoryError: String?,
    onSelectCategory: (String) -> Unit,
    onToggleDropdown: () -> Unit,
    onDismissDropdown: () -> Unit,
    onShowCustomField: () -> Unit,
    onHideCustomField: () -> Unit,
    onCustomTextChange: (String) -> Unit,
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
                        selected = selectedCategory == name && !showCustomField,
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

        // "More categories" dropdown + "Custom" chip on the same row
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Dropdown for non-quick-pick categories
            if (otherCategories.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { onToggleDropdown() }
                ) {
                    val dropdownLabel = if (selectedCategory.isNotBlank()
                        && !showCustomField
                        && selectedCategory !in quickPickCategories
                    ) {
                        selectedCategory
                    } else {
                        stringResource(R.string.more_categories)
                    }

                    FilterChip(
                        selected = selectedCategory.isNotBlank()
                            && !showCustomField
                            && selectedCategory !in quickPickCategories,
                        onClick = {},
                        label = { Text(dropdownLabel) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .widthIn(min = 150.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = onDismissDropdown
                    ) {
                        otherCategories.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = { onSelectCategory(name) },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }

            // Custom chip
            FilterChip(
                selected = showCustomField,
                onClick = {
                    if (showCustomField) onHideCustomField() else onShowCustomField()
                },
                label = { Text(stringResource(R.string.custom_category)) },
                leadingIcon = {
                    if (showCustomField) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }

        // Custom text field
        if (showCustomField) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = customText,
                onValueChange = onCustomTextChange,
                label = { Text(stringResource(R.string.custom_category_hint)) },
                placeholder = { Text(stringResource(R.string.custom_category_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
            showCustomField = false,
            customText = "",
            categoryError = null,
            onSelectCategory = {},
            onToggleDropdown = {},
            onDismissDropdown = {},
            onShowCustomField = {},
            onHideCustomField = {},
            onCustomTextChange = {},
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
            showCustomField = false,
            customText = "",
            categoryError = null,
            onSelectCategory = {},
            onToggleDropdown = {},
            onDismissDropdown = {},
            onShowCustomField = {},
            onHideCustomField = {},
            onCustomTextChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "CategorySelector - Custom field open", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategorySelectorCustomOpen() {
    CarTrackingAppTheme(darkTheme = false) {
        CategorySelector(
            selectedCategory = "Window tinting",
            quickPickCategories = listOf("Insurance", "Oil change"),
            otherCategories = listOf("Parking", "Toll"),
            dropdownExpanded = false,
            showCustomField = true,
            customText = "Window tinting",
            categoryError = null,
            onSelectCategory = {},
            onToggleDropdown = {},
            onDismissDropdown = {},
            onShowCustomField = {},
            onHideCustomField = {},
            onCustomTextChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
