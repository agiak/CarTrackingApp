package com.agcoding.cartrackingapp.presentation.editrefill.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun SaveRefillButton(
    isSaving: Boolean,
    isEnabled: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onSaveClick,
        modifier = modifier,
        enabled = isEnabled && !isSaving
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(stringResource(R.string.save_changes))
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Save Refill Button - Enabled", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSaveRefillButtonEnabled() {
    CarTrackingAppTheme(darkTheme = false) {
        SaveRefillButton(
            isSaving = false,
            isEnabled = true,
            onSaveClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Save Refill Button - Disabled", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSaveRefillButtonDisabled() {
    CarTrackingAppTheme(darkTheme = false) {
        SaveRefillButton(
            isSaving = false,
            isEnabled = false,
            onSaveClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Save Refill Button - Saving", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSaveRefillButtonSaving() {
    CarTrackingAppTheme(darkTheme = false) {
        SaveRefillButton(
            isSaving = true,
            isEnabled = true,
            onSaveClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Save Refill Button - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSaveRefillButtonDark() {
    CarTrackingAppTheme(darkTheme = true) {
        SaveRefillButton(
            isSaving = false,
            isEnabled = true,
            onSaveClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
