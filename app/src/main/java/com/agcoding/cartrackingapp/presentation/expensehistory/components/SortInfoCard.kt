package com.agcoding.cartrackingapp.presentation.expensehistory.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun SortInfoCard(
    sortOptionName: String,
    modifier: Modifier = Modifier
) {
    StyledCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.sort),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = sortOptionName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Sort Info Card - Date", showBackground = true, widthDp = 300)
@Composable
private fun PreviewSortInfoCardDate() {
    CarTrackingAppTheme(darkTheme = false) {
        SortInfoCard(
            sortOptionName = "Date (Newest First)",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Sort Info Card - Amount", showBackground = true, widthDp = 300)
@Composable
private fun PreviewSortInfoCardAmount() {
    CarTrackingAppTheme(darkTheme = false) {
        SortInfoCard(
            sortOptionName = "Amount (Highest First)",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Sort Info Card - Dark", showBackground = true, widthDp = 300)
@Composable
private fun PreviewSortInfoCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        SortInfoCard(
            sortOptionName = "Category (A-Z)",
            modifier = Modifier.padding(16.dp)
        )
    }
}
