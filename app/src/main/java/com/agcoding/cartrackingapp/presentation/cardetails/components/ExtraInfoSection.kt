package com.agcoding.cartrackingapp.presentation.cardetails.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExtraInfoSection(
    car: Car,
    onEditClick: () -> Unit
) {
    val hasExtraInfo = car.insuranceExpirationDate != null ||
            car.kteoExpirationDate != null ||
            car.emissionsCardExpirationDate != null ||
            car.roadTaxAmount != null ||
            car.roadTaxDueDate != null ||
            car.lastServiceDate != null ||
            car.lastTireChangeDate != null ||
            !car.tireBrand.isNullOrBlank() ||
            !car.tireDimensions.isNullOrBlank() ||
            car.tireInstallationDate != null ||
            !car.tyreSize.isNullOrBlank()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.extra_information),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (!hasExtraInfo) {
            // Banner when no extra info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEditClick),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.add_extra_information),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.add_extra_information_desc),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.add_info),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            // Display extra info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

                    // Insurance Information
                    car.insuranceExpirationDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.insurance_expiration),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Legal & Compliance
                    car.kteoExpirationDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.kteo_expiration),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    car.emissionsCardExpirationDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.emissions_card_expiration),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    car.roadTaxAmount?.let { amount ->
                        ExtraInfoItem(
                            label = stringResource(R.string.road_tax_amount),
                            value = amount.formatMoney()
                        )
                    }

                    car.roadTaxDueDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.road_tax_due_date),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Maintenance History
                    car.lastServiceDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.last_service),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    car.lastTireChangeDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.last_tire_change),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Tires Information
                    if (!car.tireBrand.isNullOrBlank()) {
                        ExtraInfoItem(
                            label = stringResource(R.string.tire_brand_model),
                            value = car.tireBrand
                        )
                    }

                    if (!car.tireDimensions.isNullOrBlank()) {
                        ExtraInfoItem(
                            label = stringResource(R.string.tire_dimensions),
                            value = car.tireDimensions
                        )
                    }

                    car.tireInstallationDate?.let { date ->
                        ExtraInfoItem(
                            label = stringResource(R.string.tire_installation_date),
                            value = dateFormatter.format(Date(date))
                        )
                    }

                    // Legacy field (for backward compatibility)
                    if (!car.tyreSize.isNullOrBlank()) {
                        ExtraInfoItem(
                            label = stringResource(R.string.tyre_size_legacy),
                            value = car.tyreSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExtraInfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Extra Info Section - Empty", showBackground = true, widthDp = 400)
@Composable
private fun PreviewExtraInfoSectionEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        ExtraInfoSection(
            car = Car(
                id = 1,
                name = "Toyota Corolla",
                licensePlate = "ABC-1234",
                currentOdometer = 45000.0,
                initialOdometer = 0.0
            ),
            onEditClick = {}
        )
    }
}

@Preview(name = "Extra Info Section - Filled", showBackground = true, widthDp = 400)
@Composable
private fun PreviewExtraInfoSectionFilled() {
    CarTrackingAppTheme(darkTheme = false) {
        ExtraInfoSection(
            car = Car(
                id = 1,
                name = "BMW 320i",
                licensePlate = "XYZ-5678",
                currentOdometer = 82000.0,
                initialOdometer = 0.0,
                insuranceExpirationDate = System.currentTimeMillis(),
                kteoExpirationDate = System.currentTimeMillis(),
                emissionsCardExpirationDate = System.currentTimeMillis(),
                roadTaxAmount = 350.0,
                roadTaxDueDate = System.currentTimeMillis(),
                lastServiceDate = System.currentTimeMillis(),
                lastTireChangeDate = System.currentTimeMillis(),
                tireBrand = "Michelin Pilot Sport 4",
                tireDimensions = "225/45 R17",
                tireInstallationDate = System.currentTimeMillis()
            ),
            onEditClick = {}
        )
    }
}

@Preview(name = "Extra Info Item", showBackground = true, widthDp = 400)
@Composable
private fun PreviewExtraInfoItem() {
    CarTrackingAppTheme(darkTheme = false) {
        ExtraInfoItem(
            label = "Insurance Expiration",
            value = "25/12/2025"
        )
    }
}
