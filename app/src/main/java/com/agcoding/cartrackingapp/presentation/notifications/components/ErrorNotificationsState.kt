package com.agcoding.cartrackingapp.presentation.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ErrorNotificationsState(
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.error_loading_data),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Error Notifications State - Light", showBackground = true, widthDp = 380, heightDp = 600)
@Composable
private fun PreviewErrorNotificationsState() {
    CarTrackingAppTheme(darkTheme = false) {
        ErrorNotificationsState(
            message = "Failed to load notifications",
            onRetry = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Error Notifications State - Dark", showBackground = true, widthDp = 380, heightDp = 600)
@Composable
private fun PreviewErrorNotificationsStateDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ErrorNotificationsState(
            message = "Network connection error",
            onRetry = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
