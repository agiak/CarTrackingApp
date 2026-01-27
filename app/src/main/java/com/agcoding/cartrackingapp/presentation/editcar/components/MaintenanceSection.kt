package com.agcoding.cartrackingapp.presentation.editcar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledDatePickerField
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun MaintenanceSection(
    lastServiceDate: Long?,
    onLastServiceDateChange: (Long?) -> Unit,
    lastTireChangeDate: Long?,
    onLastTireChangeDateChange: (Long?) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.edit_car_section_maintenance_history),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Two-column for maintenance dates on tablets
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StyledDatePickerField(
                    value = lastServiceDate,
                    onDateSelected = onLastServiceDateChange,
                    label = { Text(stringResource(R.string.edit_car_field_last_service_date)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) },
                    modifier = Modifier.weight(1f)
                )

                StyledDatePickerField(
                    value = lastTireChangeDate,
                    onDateSelected = onLastTireChangeDateChange,
                    label = { Text(stringResource(R.string.edit_car_field_last_tire_change_date)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            StyledDatePickerField(
                value = lastServiceDate,
                onDateSelected = onLastServiceDateChange,
                label = { Text(stringResource(R.string.edit_car_field_last_service_date)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) }
            )

            StyledDatePickerField(
                value = lastTireChangeDate,
                onDateSelected = onLastTireChangeDateChange,
                label = { Text(stringResource(R.string.edit_car_field_last_tire_change_date)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) }
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Maintenance Section - Phone", showBackground = true, widthDp = 380)
@Composable
private fun PreviewMaintenanceSectionPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        var lastServiceDate by remember { mutableLongStateOf(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) }
        var lastTireChangeDate by remember { mutableLongStateOf(System.currentTimeMillis() - 180L * 24 * 60 * 60 * 1000) }

        MaintenanceSection(
            lastServiceDate = lastServiceDate,
            onLastServiceDateChange = { lastServiceDate = it ?: 0L },
            lastTireChangeDate = lastTireChangeDate,
            onLastTireChangeDateChange = { lastTireChangeDate = it ?: 0L },
            isTablet = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Maintenance Section - Tablet", showBackground = true, widthDp = 800)
@Composable
private fun PreviewMaintenanceSectionTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        var lastServiceDate by remember { mutableLongStateOf(System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000) }
        var lastTireChangeDate by remember { mutableLongStateOf(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000) }

        MaintenanceSection(
            lastServiceDate = lastServiceDate,
            onLastServiceDateChange = { lastServiceDate = it ?: 0L },
            lastTireChangeDate = lastTireChangeDate,
            onLastTireChangeDateChange = { lastTireChangeDate = it ?: 0L },
            isTablet = true,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(name = "Maintenance Section - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewMaintenanceSectionDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var lastServiceDate by remember { mutableStateOf<Long?>(null) }
        var lastTireChangeDate by remember { mutableStateOf<Long?>(null) }

        MaintenanceSection(
            lastServiceDate = lastServiceDate,
            onLastServiceDateChange = { lastServiceDate = it },
            lastTireChangeDate = lastTireChangeDate,
            onLastTireChangeDateChange = { lastTireChangeDate = it },
            isTablet = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}
