package com.agcoding.cartrackingapp.presentation.editrefill.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.util.Locale

@Composable
fun CalculatedValuesCard(
    amountPaid: String,
    litersAdded: String,
    tripDistance: String,
    modifier: Modifier = Modifier
) {
    val amount = amountPaid.toDoubleOrNull()
    val liters = litersAdded.toDoubleOrNull()
    val distance = tripDistance.toDoubleOrNull()

    if (amount != null && liters != null && liters > 0) {
        val pricePerLiter = amount / liters
        val consumption = if (distance != null && distance > 0) {
            (liters / distance) * 100.0
        } else null

        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.calculated),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.price_per_liter_format,
                        String.format(Locale.getDefault(), "%.3f", pricePerLiter)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                if (consumption != null) {
                    Text(
                        text = stringResource(
                            R.string.fuel_consumption_format,
                            String.format(Locale.getDefault(), "%.2f", consumption)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Calculated Values - Full Data", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCalculatedValuesFullData() {
    CarTrackingAppTheme(darkTheme = false) {
        CalculatedValuesCard(
            amountPaid = "65.50",
            litersAdded = "42.5",
            tripDistance = "580",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Calculated Values - No Distance", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCalculatedValuesNoDistance() {
    CarTrackingAppTheme(darkTheme = false) {
        CalculatedValuesCard(
            amountPaid = "72.30",
            litersAdded = "48.2",
            tripDistance = "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Calculated Values - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCalculatedValuesDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CalculatedValuesCard(
            amountPaid = "58.90",
            litersAdded = "38.7",
            tripDistance = "520",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
