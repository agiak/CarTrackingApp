package com.agcoding.cartrackingapp.presentation.settings.expensecategories

import androidx.compose.runtime.Composable
import com.agcoding.cartrackingapp.presentation.expensecategories.ManageExpenseCategoriesScreen

@Composable
fun ExpenseCategoriesSettingsScreen(
    onNavigateBack: () -> Unit
) {
    // Simply reuse the existing ManageExpenseCategoriesScreen
    ManageExpenseCategoriesScreen(
        onNavigateBack = onNavigateBack
    )
}
