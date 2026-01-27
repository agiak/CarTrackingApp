package com.agcoding.cartrackingapp.presentation.carlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Error State - Light", showBackground = true)
@Composable
private fun PreviewErrorState() {
    CarTrackingAppTheme(darkTheme = false) {
        ErrorState(message = "Failed to load cars. Please try again.")
    }
}

@Preview(name = "Error State - Dark", showBackground = true)
@Composable
private fun PreviewErrorStateDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ErrorState(message = "Failed to load cars. Please try again.")
    }
}
