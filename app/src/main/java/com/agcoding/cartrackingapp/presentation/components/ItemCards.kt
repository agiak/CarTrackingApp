package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Location
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shared composable for displaying a refill item
 * Used in both CarDetailsScreen and MonthDetailsScreen for consistency
 */
@Composable
fun RefillItemCard(
    refill: FuelRefill,
    carName: String? = null,
    onClick: () -> Unit
) {
    val itemDatePattern = stringResource(R.string.date_format_full_with_time)
    val dateFormat = remember(itemDatePattern) { SimpleDateFormat(itemDatePattern, Locale.getDefault()) }

    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Car name (if provided)
                if (carName != null) {
                    Text(
                        text = carName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Date
                Text(
                    text = dateFormat.format(Date(refill.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Details row: Liters • Distance • Consumption
                Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    // Liters
                    Text(
                        text = stringResource(R.string.liters_format, String.format("%.1f", refill.litersAdded)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Distance
                    Text(
                        text = stringResource(R.string.kilometers_format, String.format("%.0f", refill.tripDistance)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (refill.tripDistance > 0) {
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Consumption
                        val consumption = (refill.litersAdded / refill.tripDistance) * 100
                        Text(
                            text = stringResource(
                                R.string.consumption_l_per_100km_format,
                                String.format("%.1f", consumption)
                            ),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Price
            Text(
                text = stringResource(R.string.currency_eur_format, String.format("%.2f", refill.amountPaid)),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Shared composable for displaying an expense/service item
 * Used in both CarDetailsScreen and MonthDetailsScreen for consistency
 */
@Composable
fun ExpenseItemCard(
    expense: Expense,
    carName: String? = null,
    onClick: () -> Unit
) {
    val itemDatePattern = stringResource(R.string.date_format_full_with_time)
    val dateFormat = remember(itemDatePattern) { SimpleDateFormat(itemDatePattern, Locale.getDefault()) }

    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Category
                Text(
                    text = expense.category,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Car name (if provided)
                if (carName != null) {
                    Text(
                        text = carName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Date
                Text(
                    text = dateFormat.format(Date(expense.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = stringResource(R.string.currency_eur_format, String.format("%.2f", expense.amount)),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Refill Card - With Car Name", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillItemCard() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillItemCard(
            refill = FuelRefill(
                id = 1,
                carId = 1,
                timestamp = System.currentTimeMillis(),
                amountPaid = 65.50,
                litersAdded = 42.5,
                pricePerLiter = 1.54,
                tripDistance = 580.0,
                odometerReading = 12580.0,
                fuelConsumption = 7.33,
                location = Location(37.9838, 23.7275),
                notes = "Regular refill"
            ),
            carName = "Toyota Corolla",
            onClick = {},
        )
    }
}

@Preview(name = "Refill Card - No Car Name", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillItemCardNoCar() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillItemCard(
            refill = FuelRefill(
                id = 2,
                carId = 1,
                timestamp = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                amountPaid = 72.30,
                litersAdded = 45.0,
                pricePerLiter = 1.61,
                tripDistance = 620.0,
                odometerReading = 12000.0,
                fuelConsumption = 7.26,
                location = null,
                notes = null
            ),
            carName = null,
            onClick = {}
        )
    }
}

@Preview(name = "Expense Card - Service", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseItemCard() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseItemCard(
            expense = Expense(
                id = 1,
                carId = 1,
                category = "Oil Change",
                amount = 85.00,
                timestamp = System.currentTimeMillis(),
                notes = "Regular maintenance",
                reminderEnabled = true,
                reminderDate = System.currentTimeMillis() + 90 * 24 * 60 * 60 * 1000L,
                reminderMileage = 15000,
                preExpiryNotificationSent = false,
                reminderDismissed = false
            ),
            carName = "Honda Civic",
            onClick = {}
        )
    }
}

@Preview(name = "Expense Card - Dark Mode", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseItemCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ExpenseItemCard(
            expense = Expense(
                id = 2,
                carId = 1,
                category = "Tire Change",
                amount = 450.00,
                timestamp = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000L,
                notes = "All four tires replaced",
                reminderEnabled = false,
                reminderDate = null,
                reminderMileage = null,
                preExpiryNotificationSent = false,
                reminderDismissed = false
            ),
            carName = null,
            onClick = {}
        )
    }
}

