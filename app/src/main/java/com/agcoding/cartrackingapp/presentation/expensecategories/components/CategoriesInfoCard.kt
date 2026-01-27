package com.agcoding.cartrackingapp.presentation.expensecategories.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun CategoriesInfoCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.expense_categories_info_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.expense_categories_info_desc),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Categories Info Card - Light", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoriesInfoCard() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoriesInfoCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Categories Info Card - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoriesInfoCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CategoriesInfoCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Categories Info Card - Tablet", showBackground = true, widthDp = 600)
@Composable
private fun PreviewCategoriesInfoCardTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoriesInfoCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}
