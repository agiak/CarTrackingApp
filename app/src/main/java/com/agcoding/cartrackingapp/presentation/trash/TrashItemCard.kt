package com.agcoding.cartrackingapp.presentation.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.TrashItem
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun TrashItemCard(
    item: TrashItem,
    dateFormat: SimpleDateFormat,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val (title, subtitle, sectionLabel) = when (item) {
        is TrashItem.CarItem -> Triple(item.car.name, item.car.licensePlate, stringResource(R.string.trash_cars_section))
        is TrashItem.RefillItem -> Triple(
            "${item.refill.amountPaid.formatMoney()} · ${item.refill.litersAdded.formatNumber(1)}L",
            item.carName,
            stringResource(R.string.trash_refills_section)
        )
        is TrashItem.ExpenseItem -> Triple(
            "${item.expense.category} · ${item.expense.amount.formatMoney()}",
            item.carName,
            stringResource(R.string.trash_expenses_section)
        )
        is TrashItem.TripItem -> Triple(item.trip.name, item.carName, stringResource(R.string.trash_trips_section))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(sectionLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                Text(
                    stringResource(R.string.trash_deleted_on, dateFormat.format(Date(item.deletedAt))),
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDeletePermanently, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.trash_delete_permanently), fontSize = 12.sp)
                }
                Button(onClick = onRestore, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.trash_restore))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrashItemCardPreview() {
    CarTrackingAppTheme {
        TrashItemCard(
            item = TrashItem.TripItem(
                trip = Trip(carId = 1, name = "Weekend Trip", createdAt = 0L, updatedAt = 0L),
                carName = "My Car",
                deletedAtMs = 0L
            ),
            dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
            onRestore = {},
            onDeletePermanently = {}
        )
    }
}
