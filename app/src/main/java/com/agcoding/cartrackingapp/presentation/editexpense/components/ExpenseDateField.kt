package com.agcoding.cartrackingapp.presentation.editexpense.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseDateField(
    selectedDate: Long,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    StyledOutlinedTextField(
        value = dateFormat.format(Date(selectedDate)),
        onValueChange = {},
        label = { Text(stringResource(R.string.date)) },
        trailingIcon = {
            IconButton(onClick = onShowDatePicker) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = stringResource(R.string.pick_date)
                )
            }
        },
        readOnly = true,
        modifier = modifier
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Expense Date Field - Today", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseDateFieldToday() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseDateField(
            selectedDate = System.currentTimeMillis(),
            onShowDatePicker = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Expense Date Field - Past Date", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseDateFieldPastDate() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseDateField(
            selectedDate = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
            onShowDatePicker = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Expense Date Field - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseDateFieldDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ExpenseDateField(
            selectedDate = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L,
            onShowDatePicker = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
