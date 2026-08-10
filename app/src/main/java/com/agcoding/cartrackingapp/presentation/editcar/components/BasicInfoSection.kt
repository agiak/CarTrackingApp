package com.agcoding.cartrackingapp.presentation.editcar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.components.ThousandsSeparatorTransformation
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun BasicInfoSection(
    name: String,
    onNameChange: (String) -> Unit,
    licensePlate: String,
    onLicensePlateChange: (String) -> Unit,
    odometer: String,
    onOdometerChange: (String) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.edit_car_section_basic_information),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )

        StyledOutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.edit_car_field_car_name)) },
            placeholder = { Text(stringResource(R.string.edit_car_placeholder_car_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Two-column layout for license plate and odometer on tablets
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StyledOutlinedTextField(
                    value = licensePlate,
                    onValueChange = { onLicensePlateChange(it.uppercase()) },
                    label = { Text(stringResource(R.string.edit_car_field_license_plate)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_license_plate)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                StyledOutlinedTextField(
                    value = odometer,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d+$"))) {
                            onOdometerChange(newValue)
                        }
                    },
                    label = { Text(stringResource(R.string.edit_car_field_current_odometer_km)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_odometer)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandsSeparatorTransformation(),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            StyledOutlinedTextField(
                value = licensePlate,
                onValueChange = { onLicensePlateChange(it.uppercase()) },
                label = { Text(stringResource(R.string.edit_car_field_license_plate)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_license_plate)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            StyledOutlinedTextField(
                value = odometer,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d+$"))) {
                        onOdometerChange(newValue)
                    }
                },
                label = { Text(stringResource(R.string.edit_car_field_current_odometer_km)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_odometer)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = ThousandsSeparatorTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Basic Info Section - Phone", showBackground = true, widthDp = 380)
@Composable
private fun PreviewBasicInfoSectionPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        var name by remember { mutableStateOf("Toyota Corolla") }
        var licensePlate by remember { mutableStateOf("ABC-1234") }
        var odometer by remember { mutableStateOf("45000") }

        BasicInfoSection(
            name = name,
            onNameChange = { name = it },
            licensePlate = licensePlate,
            onLicensePlateChange = { licensePlate = it },
            odometer = odometer,
            onOdometerChange = { odometer = it },
            isTablet = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Basic Info Section - Tablet", showBackground = true, widthDp = 800)
@Composable
private fun PreviewBasicInfoSectionTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        var name by remember { mutableStateOf("Honda Civic") }
        var licensePlate by remember { mutableStateOf("XYZ-9876") }
        var odometer by remember { mutableStateOf("120000") }

        BasicInfoSection(
            name = name,
            onNameChange = { name = it },
            licensePlate = licensePlate,
            onLicensePlateChange = { licensePlate = it },
            odometer = odometer,
            onOdometerChange = { odometer = it },
            isTablet = true,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(name = "Basic Info Section - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewBasicInfoSectionDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var name by remember { mutableStateOf("BMW 320i") }
        var licensePlate by remember { mutableStateOf("DEF-5678") }
        var odometer by remember { mutableStateOf("78500") }

        BasicInfoSection(
            name = name,
            onNameChange = { name = it },
            licensePlate = licensePlate,
            onLicensePlateChange = { licensePlate = it },
            odometer = odometer,
            onOdometerChange = { odometer = it },
            isTablet = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}
