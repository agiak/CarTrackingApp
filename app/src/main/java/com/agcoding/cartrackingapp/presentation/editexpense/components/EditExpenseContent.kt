package com.agcoding.cartrackingapp.presentation.editexpense.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun EditExpenseContent(
    category: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    selectedDate: Long,
    onShowDatePicker: () -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CategoryHeader(
            category = category,
            modifier = Modifier.fillMaxWidth()
        )

        ExpenseAmountField(
            amount = amount,
            onAmountChange = onAmountChange,
            modifier = Modifier.fillMaxWidth()
        )

        ExpenseDateField(
            selectedDate = selectedDate,
            onShowDatePicker = onShowDatePicker,
            modifier = Modifier.fillMaxWidth()
        )

        ExpenseNotesField(
            notes = notes,
            onNotesChange = onNotesChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        SaveExpenseButton(
            isSaving = isSaving,
            isEnabled = amount.isNotBlank(),
            onSaveClick = onSaveClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Edit Expense Content - Empty", showBackground = true, widthDp = 380)
@Composable
private fun PreviewEditExpenseContentEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        EditExpenseContent(
            category = "Fuel",
            amount = amount,
            onAmountChange = { amount = it },
            selectedDate = System.currentTimeMillis(),
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            isSaving = false,
            onSaveClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Edit Expense Content - Filled", showBackground = true, widthDp = 380)
@Composable
private fun PreviewEditExpenseContentFilled() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("65.50") }
        var notes by remember { mutableStateOf("Regular fuel refill at Shell station") }

        EditExpenseContent(
            category = "Fuel",
            amount = amount,
            onAmountChange = { amount = it },
            selectedDate = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L,
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            isSaving = false,
            onSaveClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Edit Expense Content - Saving", showBackground = true, widthDp = 380)
@Composable
private fun PreviewEditExpenseContentSaving() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("120.00") }
        var notes by remember { mutableStateOf("Oil change and filter replacement") }

        EditExpenseContent(
            category = "Service & Maintenance",
            amount = amount,
            onAmountChange = { amount = it },
            selectedDate = System.currentTimeMillis(),
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            isSaving = true,
            onSaveClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Edit Expense Content - Tablet", showBackground = true, widthDp = 600)
@Composable
private fun PreviewEditExpenseContentTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("450.00") }
        var notes by remember { mutableStateOf("Annual insurance payment") }

        EditExpenseContent(
            category = "Insurance",
            amount = amount,
            onAmountChange = { amount = it },
            selectedDate = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            isSaving = false,
            onSaveClick = {},
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(name = "Edit Expense Content - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewEditExpenseContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var amount by remember { mutableStateOf("85.25") }
        var notes by remember { mutableStateOf("") }

        EditExpenseContent(
            category = "Parking & Tolls",
            amount = amount,
            onAmountChange = { amount = it },
            selectedDate = System.currentTimeMillis(),
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            isSaving = false,
            onSaveClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
