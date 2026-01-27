package com.agcoding.cartrackingapp.presentation.refilldetails.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TotalAmountCard(
    amountPaid: Double,
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondary,
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.total_amount_paid),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "€%.2f".format(amountPaid),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDate(timestamp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Total Amount Card - Normal", showBackground = true, widthDp = 380)
@Composable
private fun PreviewTotalAmountCard() {
    CarTrackingAppTheme(darkTheme = false) {
        TotalAmountCard(
            amountPaid = 65.50,
            timestamp = System.currentTimeMillis(),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Total Amount Card - Large Amount", showBackground = true, widthDp = 380)
@Composable
private fun PreviewTotalAmountCardLarge() {
    CarTrackingAppTheme(darkTheme = false) {
        TotalAmountCard(
            amountPaid = 125.75,
            timestamp = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Total Amount Card - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewTotalAmountCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        TotalAmountCard(
            amountPaid = 82.30,
            timestamp = System.currentTimeMillis(),
            modifier = Modifier.padding(16.dp)
        )
    }
}
