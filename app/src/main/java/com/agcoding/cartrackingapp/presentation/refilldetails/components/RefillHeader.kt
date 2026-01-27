package com.agcoding.cartrackingapp.presentation.refilldetails.components

import androidx.compose.foundation.layout.Column
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
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun RefillHeader(
    carName: String?,
    licensePlate: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.refill_details_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = if (carName != null && licensePlate != null)
                "$carName • $licensePlate"
            else
                stringResource(R.string.unknown_car),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Refill Header - With Car", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillHeader() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillHeader(
            carName = "Toyota Corolla",
            licensePlate = "ABC-1234",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Refill Header - Unknown Car", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillHeaderUnknown() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillHeader(
            carName = null,
            licensePlate = null,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Refill Header - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillHeaderDark() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillHeader(
            carName = "Honda Civic",
            licensePlate = "XYZ-5678",
            modifier = Modifier.padding(16.dp)
        )
    }
}
