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
import com.agcoding.cartrackingapp.presentation.expensecategories.CategoryWithQuickPick
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ManageExpenseCategoriesContent(
    predefinedCategories: List<CategoryWithQuickPick>,
    customCategories: List<CategoryWithQuickPick>,
    onDeleteCategory: (String) -> Unit,
    onToggleQuickPick: (name: String, current: Boolean) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = if (isTablet) 24.dp else 16.dp,
            end = if (isTablet) 24.dp else 16.dp,
            top = 16.dp,
            bottom = 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            CategoriesInfoCard(modifier = Modifier.fillMaxWidth())
        }

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
                    name = category.name,
                    isCustom = false,
                    isQuickPick = category.isQuickPick,
                    onDelete = null,
                    onToggleQuickPick = { onToggleQuickPick(category.name, category.isQuickPick) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

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
                    name = category.name,
                    isCustom = true,
                    isQuickPick = category.isQuickPick,
                    onDelete = { onDeleteCategory(category.name) },
                    onToggleQuickPick = { onToggleQuickPick(category.name, category.isQuickPick) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (customCategories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                CategoryWithQuickPick("Fuel", false),
                CategoryWithQuickPick("Insurance", true),
                CategoryWithQuickPick("Service", false)
            ),
            customCategories = emptyList(),
            onDeleteCategory = {},
            onToggleQuickPick = { _, _ -> },
            isTablet = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Categories Content - With Custom", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewCategoriesContentWithCustom() {
    CarTrackingAppTheme(darkTheme = false) {
        ManageExpenseCategoriesContent(
            predefinedCategories = listOf(
                CategoryWithQuickPick("Fuel", true),
                CategoryWithQuickPick("Insurance", false)
            ),
            customCategories = listOf(
                CategoryWithQuickPick("Car Wash", true),
                CategoryWithQuickPick("Tire Replacement", false)
            ),
            onDeleteCategory = {},
            onToggleQuickPick = { _, _ -> },
            isTablet = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Categories Content - Dark", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewCategoriesContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ManageExpenseCategoriesContent(
            predefinedCategories = listOf(
                CategoryWithQuickPick("Insurance", true),
                CategoryWithQuickPick("Service", false)
            ),
            customCategories = listOf(
                CategoryWithQuickPick("Car Wash", true)
            ),
            onDeleteCategory = {},
            onToggleQuickPick = { _, _ -> },
            isTablet = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}
