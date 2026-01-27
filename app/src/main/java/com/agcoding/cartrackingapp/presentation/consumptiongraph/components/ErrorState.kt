package com.agcoding.cartrackingapp.presentation.consumptiongraph.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Error State - Light", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewErrorState() {
    CarTrackingAppTheme(darkTheme = false) {
        ErrorState(
            message = "Failed to load consumption data. Please try again.",
            onRetry = {}
        )
    }
}

@Preview(name = "Error State - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewErrorStateDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ErrorState(
            message = "Failed to load consumption data. Please try again.",
            onRetry = {}
        )
    }
}
