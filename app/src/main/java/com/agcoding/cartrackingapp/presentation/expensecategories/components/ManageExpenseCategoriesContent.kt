package com.agcoding.cartrackingapp.presentation.expensecategories.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ManageExpenseCategoriesContent(
    predefinedCategories: List<String>,
    customCategories: List<String>,
    onDeleteCategory: (String) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = if (isTablet) 24.dp else 16.dp,
            end = if (isTablet) 24.dp else 16.dp,
            top = 16.dp,
            bottom = 88.dp // Space for FAB
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Info card
        item {
            CategoriesInfoCard(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Predefined categories header
        if (predefinedCategories.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.predefined_categories),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(predefinedCategories) { category ->
                CategoryItem(
                    name = category,
                    isCustom = false,
                    onDelete = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Custom categories header
        if (customCategories.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.custom_categories),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(customCategories) { category ->
                CategoryItem(
                    name = category,
                    isCustom = true,
                    onDelete = { onDeleteCategory(category) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Empty state
        if (customCategories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.no_custom_categories_yet),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.tap_plus_to_add_category),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Preview(name = "Categories Content - Phone No Custom", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewCategoriesContentPhoneNoCustom() {
    CarTrackingAppTheme(darkTheme = false) {
        ManageExpenseCategoriesContent(
            predefinedCategories = listOf(
                "Fuel",
                "Service & Maintenance",
                "Insurance",
                "Parking & Tolls",
                "Other"
            ),
            customCategories = emptyList(),
            onDeleteCategory = {},
            isTablet = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Categories Content - Phone With Custom", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewCategoriesContentPhoneWithCustom() {
    CarTrackingAppTheme(darkTheme = false) {
        ManageExpenseCategoriesContent(
            predefinedCategories = listOf(
                "Fuel",
                "Service & Maintenance",
                "Insurance",
                "Parking & Tolls"
            ),
            customCategories = listOf(
                "Car Wash",
                "Tire Replacement",
                "Extended Warranty"
            ),
            onDeleteCategory = {},
            isTablet = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Categories Content - Tablet", showBackground = true, widthDp = 800, heightDp = 600)
@Composable
private fun PreviewCategoriesContentTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            ManageExpenseCategoriesContent(
                predefinedCategories = listOf(
                    "Fuel",
                    "Service & Maintenance",
                    "Insurance",
                    "Parking & Tolls",
                    "Other"
                ),
                customCategories = listOf(
                    "Car Wash",
                    "Tire Replacement",
                    "Extended Warranty",
                    "Detailing"
                ),
                onDeleteCategory = {},
                isTablet = true,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}

@Preview(name = "Categories Content - Dark", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewCategoriesContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ManageExpenseCategoriesContent(
            predefinedCategories = listOf(
                "Fuel",
                "Service & Maintenance",
                "Insurance"
            ),
            customCategories = listOf(
                "Car Wash",
                "Tire Replacement"
            ),
            onDeleteCategory = {},
            isTablet = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Categories Content - Many Categories", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewCategoriesContentManyCat() {
    CarTrackingAppTheme(darkTheme = false) {
        ManageExpenseCategoriesContent(
            predefinedCategories = listOf(
                "Fuel",
                "Service & Maintenance",
                "Insurance",
                "Parking & Tolls",
                "Registration",
                "Other"
            ),
            customCategories = listOf(
                "Car Wash",
                "Tire Replacement",
                "Extended Warranty",
                "Detailing",
                "Window Tinting",
                "Accessories",
                "Audio System",
                "GPS Navigation"
            ),
            onDeleteCategory = {},
            isTablet = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}
