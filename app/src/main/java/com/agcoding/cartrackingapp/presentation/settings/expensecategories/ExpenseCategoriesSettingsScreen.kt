package com.agcoding.cartrackingapp.presentation.settings.expensecategories
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

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

@Preview(showBackground = true)
@Composable
private fun ExpenseCategoriesSettingsScreenPreview() {
    CarTrackingAppTheme {
        ExpenseCategoriesSettingsScreen(onNavigateBack = {})
    }
}
