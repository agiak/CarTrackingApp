package com.agcoding.cartrackingapp.presentation.editexpense.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
fun SaveExpenseButton(
    isSaving: Boolean,
    isEnabled: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onSaveClick,
        enabled = isEnabled && !isSaving,
        modifier = modifier
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(stringResource(R.string.save_changes))
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Save Expense Button - Enabled", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSaveExpenseButtonEnabled() {
    CarTrackingAppTheme(darkTheme = false) {
        SaveExpenseButton(
            isSaving = false,
            isEnabled = true,
            onSaveClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Save Expense Button - Disabled", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSaveExpenseButtonDisabled() {
    CarTrackingAppTheme(darkTheme = false) {
        SaveExpenseButton(
            isSaving = false,
            isEnabled = false,
            onSaveClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Save Expense Button - Saving", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSaveExpenseButtonSaving() {
    CarTrackingAppTheme(darkTheme = false) {
        SaveExpenseButton(
            isSaving = true,
            isEnabled = true,
            onSaveClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Save Expense Button - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSaveExpenseButtonDark() {
    CarTrackingAppTheme(darkTheme = true) {
        SaveExpenseButton(
            isSaving = false,
            isEnabled = true,
            onSaveClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
