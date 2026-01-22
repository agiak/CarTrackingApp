package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.agcoding.cartrackingapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A reusable date picker field that displays a styled outlined text field
 * and opens a date picker dialog when clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledDatePickerField(
    value: Long?,
    onDateSelected: (Long?) -> Unit,
    label: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    dateFormatter: SimpleDateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) },
    enabled: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showDatePicker = true
            }
    ) {
        StyledOutlinedTextField(
            value = value?.let { dateFormatter.format(Date(it)) } ?: "",
            onValueChange = {},
            label = label,
            placeholder = placeholder,
            readOnly = true,
            enabled = false, // Disabled to prevent internal click handling
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = stringResource(R.string.edit_car_cd_select_date)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            // Use custom colors to make disabled field look normal
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = value ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
