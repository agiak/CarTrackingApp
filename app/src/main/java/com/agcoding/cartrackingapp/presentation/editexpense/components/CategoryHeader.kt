package com.agcoding.cartrackingapp.presentation.editexpense.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun CategoryHeader(
    category: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = category,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier
    )
    Spacer(modifier = Modifier.height(8.dp))
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Category Header - Fuel", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryHeaderFuel() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoryHeader(
            category = "Fuel",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Category Header - Service", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryHeaderService() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoryHeader(
            category = "Service & Maintenance",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Category Header - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryHeaderDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CategoryHeader(
            category = "Insurance",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
