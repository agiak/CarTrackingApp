package com.agcoding.cartrackingapp.presentation.editrefill.components

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
fun RefillDateField(
    selectedDateMillis: Long,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateText = dateFormat.format(Date(selectedDateMillis))

    StyledOutlinedTextField(
        value = dateText,
        onValueChange = { },
        label = { Text(stringResource(R.string.date)) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = onShowDatePicker) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = stringResource(R.string.select_date)
                )
            }
        },
        modifier = modifier
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Refill Date Field - Today", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillDateFieldToday() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillDateField(
            selectedDateMillis = System.currentTimeMillis(),
            onShowDatePicker = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Refill Date Field - Past", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillDateFieldPast() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillDateField(
            selectedDateMillis = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L,
            onShowDatePicker = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Refill Date Field - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillDateFieldDark() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillDateField(
            selectedDateMillis = System.currentTimeMillis(),
            onShowDatePicker = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
