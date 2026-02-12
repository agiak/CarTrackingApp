package com.agcoding.cartrackingapp.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Widget Preview Composable
 * Shows a preview of how the Quick Add Widget will appear on the home screen
 */
@Composable
fun WidgetPreviewScreen(
    lastTransactionType: String = "Refill",
    lastTransactionAmount: String = "€70.00",
    lastTransactionDate: String = "12/05/2026",
    lastTransactionCar: String = "Toyota Corolla",
    hasTransaction: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.widget_quick_add),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.widget_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Widget Preview Card
            WidgetPreviewCard(
                lastTransactionType = lastTransactionType,
                lastTransactionAmount = lastTransactionAmount,
                lastTransactionDate = lastTransactionDate,
                lastTransactionCar = lastTransactionCar,
                hasTransaction = hasTransaction
            )
        }
    }
}

@Composable
fun WidgetPreviewCard(
    lastTransactionType: String,
    lastTransactionAmount: String,
    lastTransactionDate: String,
    lastTransactionCar: String,
    hasTransaction: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_cariboo1_monochrome),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Last Transaction Preview (if available)
            if (hasTransaction) {
                LastTransactionPreview(
                    transactionType = lastTransactionType,
                    amount = lastTransactionAmount,
                    date = lastTransactionDate,
                    carName = lastTransactionCar
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Action Buttons
            WidgetActionButton(
                icon = R.drawable.ic_refill,
                text = stringResource(id = R.string.widget_add_fuel),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            WidgetActionButton(
                icon = R.drawable.ic_receipt_24dp,
                text = stringResource(id = R.string.widget_add_expense),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LastTransactionPreview(
    transactionType: String,
    amount: String,
    date: String,
    carName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transactionType,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$date • $carName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WidgetActionButton(
    icon: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 480)
@Composable
fun WidgetPreviewScreenPreview() {
    CarTrackingAppTheme {
        WidgetPreviewScreen(
            lastTransactionType = "Refill",
            lastTransactionAmount = "€70.00",
            lastTransactionDate = "12/05/2026",
            lastTransactionCar = "Toyota Corolla",
            hasTransaction = true
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 480, locale = "el")
@Composable
fun WidgetPreviewScreenPreviewGreek() {
    CarTrackingAppTheme {
        WidgetPreviewScreen(
            lastTransactionType = "Ανεφοδιασμός",
            lastTransactionAmount = "€70.00",
            lastTransactionDate = "12/05/2026",
            lastTransactionCar = "Toyota Corolla",
            hasTransaction = true
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 480)
@Composable
fun WidgetPreviewScreenNoTransactionPreview() {
    CarTrackingAppTheme {
        WidgetPreviewScreen(
            hasTransaction = false
        )
    }
}

