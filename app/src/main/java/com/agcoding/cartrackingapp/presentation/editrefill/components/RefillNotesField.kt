package com.agcoding.cartrackingapp.presentation.editrefill.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun RefillNotesField(
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    StyledOutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text(stringResource(R.string.expense_notes_optional)) },
        placeholder = { Text(stringResource(R.string.expense_notes_hint)) },
        minLines = 2,
        maxLines = 4,
        modifier = modifier
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Refill Notes - Empty", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillNotesEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        var notes by remember { mutableStateOf("") }

        RefillNotesField(
            notes = notes,
            onNotesChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Refill Notes - With Text", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillNotesWithText() {
    CarTrackingAppTheme(darkTheme = false) {
        var notes by remember { mutableStateOf("Shell station on highway, used premium fuel") }

        RefillNotesField(
            notes = notes,
            onNotesChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Refill Notes - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillNotesDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var notes by remember { mutableStateOf("BP station, full tank refill") }

        RefillNotesField(
            notes = notes,
            onNotesChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
