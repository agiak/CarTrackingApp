package com.agcoding.cartrackingapp.presentation.editexpense.components

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
fun ExpenseNotesField(
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    StyledOutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text(stringResource(R.string.expense_notes_optional)) },
        placeholder = { Text(stringResource(R.string.expense_details_hint)) },
        minLines = 3,
        maxLines = 5,
        modifier = modifier
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Expense Notes Field - Empty", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseNotesFieldEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        var notes by remember { mutableStateOf("") }

        ExpenseNotesField(
            notes = notes,
            onNotesChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Expense Notes Field - With Text", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseNotesFieldWithText() {
    CarTrackingAppTheme(darkTheme = false) {
        var notes by remember { mutableStateOf("Regular fuel refill at Shell station on highway. Full tank.") }

        ExpenseNotesField(
            notes = notes,
            onNotesChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Expense Notes Field - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseNotesFieldDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var notes by remember { mutableStateOf("Oil change and filter replacement at local mechanic.") }

        ExpenseNotesField(
            notes = notes,
            onNotesChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
