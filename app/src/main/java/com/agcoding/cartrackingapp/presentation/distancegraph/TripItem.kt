package com.agcoding.cartrackingapp.presentation.distancegraph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.TripInfo
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun TripItem(
    trip: TripInfo,
    dateFormat: SimpleDateFormat
) {

    StyledCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(trip.carColor))
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Car name and date
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trip.carName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormat.format(Date(trip.timestamp)),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Distance and liters
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = stringResource(
                        R.string.distance_graph_km_format,
                        trip.distance.toLong().formatNumber()
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.distance_graph_liters_format, trip.liters.toDouble().formatNumber(1)),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TripItemPreview() {
    CarTrackingAppTheme {
        TripItem(
            trip = TripInfo(1, 1, "Toyota Corolla", System.currentTimeMillis(), 450.0, 35.0, 0xFF4CAF50.toInt()),
            dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        )
    }
}
