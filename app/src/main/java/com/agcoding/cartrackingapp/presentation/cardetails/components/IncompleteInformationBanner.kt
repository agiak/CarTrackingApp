package com.agcoding.cartrackingapp.presentation.cardetails.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
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
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun IncompleteInformationBanner(
    car: Car,
    onAddInformationClick: () -> Unit
) {
    StyledCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddInformationClick),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StyledCard(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        border = null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.complete_car_information),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Missing fields list
            val missingFields = buildList {
                if (car.insuranceExpirationDate == null) add(stringResource(R.string.insurance_expiration))
                if (car.kteoExpirationDate == null) add(stringResource(R.string.kteo_expiration))
                if (car.emissionsCardExpirationDate == null) add(stringResource(R.string.emissions_card_expiration))
                if (car.roadTaxAmount == null) add(stringResource(R.string.road_tax_amount))
                if (car.roadTaxDueDate == null) add(stringResource(R.string.road_tax_due_date))
                if (car.lastServiceDate == null) add(stringResource(R.string.last_service))
                if (car.lastTireChangeDate == null) add(stringResource(R.string.last_tire_change))
                if (car.tireBrand.isNullOrBlank()) add(stringResource(R.string.tire_brand_model))
                if (car.tireDimensions.isNullOrBlank()) add(stringResource(R.string.tire_dimensions))
                if (car.tireInstallationDate == null) add(stringResource(R.string.tire_installation_date))
            }

            Text(
                text = stringResource(R.string.missing_fields_label, missingFields.size),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = missingFields.take(5).joinToString(", "),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            if (missingFields.size > 5) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.and_more_fields, missingFields.size - 5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Incomplete Info Banner - Light", showBackground = true, widthDp = 400)
@Composable
private fun PreviewIncompleteInformationBanner() {
    CarTrackingAppTheme(darkTheme = false) {
        IncompleteInformationBanner(
            car = Car(
                id = 1,
                name = "Toyota Corolla",
                licensePlate = "ABC-1234",
                currentOdometer = 45000.0,
                initialOdometer = 0.0,
                // All optional fields are null - incomplete info
            ),
            onAddInformationClick = {}
        )
    }
}

@Preview(name = "Incomplete Info Banner - Dark", showBackground = true, widthDp = 400)
@Composable
private fun PreviewIncompleteInformationBannerDark() {
    CarTrackingAppTheme(darkTheme = true) {
        IncompleteInformationBanner(
            car = Car(
                id = 1,
                name = "BMW 320i",
                licensePlate = "XYZ-5678",
                currentOdometer = 82000.0,
                initialOdometer = 0.0,
                insuranceExpirationDate = System.currentTimeMillis(),
                kteoExpirationDate = System.currentTimeMillis(),
                // Some fields filled, some missing
            ),
            onAddInformationClick = {}
        )
    }
}
