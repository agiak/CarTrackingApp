package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
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
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
