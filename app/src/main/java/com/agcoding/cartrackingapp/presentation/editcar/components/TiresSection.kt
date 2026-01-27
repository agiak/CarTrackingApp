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
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun TiresSection(
    tireBrand: String,
    onTireBrandChange: (String) -> Unit,
    tireDimensions: String,
    onTireDimensionsChange: (String) -> Unit,
    tireInstallationDate: Long?,
    onTireInstallationDateChange: (Long?) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.edit_car_section_tires_information),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Two-column for tire fields on tablets
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StyledOutlinedTextField(
                    value = tireBrand,
                    onValueChange = onTireBrandChange,
                    label = { Text(stringResource(R.string.edit_car_field_tire_brand_model)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_tire_brand_model)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                StyledOutlinedTextField(
                    value = tireDimensions,
                    onValueChange = onTireDimensionsChange,
                    label = { Text(stringResource(R.string.edit_car_field_tire_dimensions)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_tire_dimensions)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            StyledOutlinedTextField(
                value = tireBrand,
                onValueChange = onTireBrandChange,
                label = { Text(stringResource(R.string.edit_car_field_tire_brand_model)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_tire_brand_model)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            StyledOutlinedTextField(
                value = tireDimensions,
                onValueChange = onTireDimensionsChange,
                label = { Text(stringResource(R.string.edit_car_field_tire_dimensions)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_tire_dimensions)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        StyledDatePickerField(
            value = tireInstallationDate,
            onDateSelected = onTireInstallationDateChange,
            label = { Text(stringResource(R.string.edit_car_field_tire_installation_date)) },
            placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) }
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Tires Section - Phone", showBackground = true, widthDp = 380)
@Composable
private fun PreviewTiresSectionPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        var tireBrand by remember { mutableStateOf("Michelin Pilot Sport 4") }
        var tireDimensions by remember { mutableStateOf("225/45 R17") }
        var tireInstallationDate by remember { mutableLongStateOf(System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000) }

        TiresSection(
            tireBrand = tireBrand,
            onTireBrandChange = { tireBrand = it },
            tireDimensions = tireDimensions,
            onTireDimensionsChange = { tireDimensions = it },
            tireInstallationDate = tireInstallationDate,
            onTireInstallationDateChange = { tireInstallationDate = it ?: 0L },
            isTablet = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Tires Section - Tablet", showBackground = true, widthDp = 800)
@Composable
private fun PreviewTiresSectionTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        var tireBrand by remember { mutableStateOf("Continental PremiumContact 6") }
        var tireDimensions by remember { mutableStateOf("205/55 R16") }
        var tireInstallationDate by remember { mutableLongStateOf(System.currentTimeMillis() - 180L * 24 * 60 * 60 * 1000) }

        TiresSection(
            tireBrand = tireBrand,
            onTireBrandChange = { tireBrand = it },
            tireDimensions = tireDimensions,
            onTireDimensionsChange = { tireDimensions = it },
            tireInstallationDate = tireInstallationDate,
            onTireInstallationDateChange = { tireInstallationDate = it ?: 0L },
            isTablet = true,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(name = "Tires Section - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewTiresSectionDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var tireBrand by remember { mutableStateOf("") }
        var tireDimensions by remember { mutableStateOf("") }
        var tireInstallationDate by remember { mutableStateOf<Long?>(null) }

        TiresSection(
            tireBrand = tireBrand,
            onTireBrandChange = { tireBrand = it },
            tireDimensions = tireDimensions,
            onTireDimensionsChange = { tireDimensions = it },
            tireInstallationDate = tireInstallationDate,
            onTireInstallationDateChange = { tireInstallationDate = it },
            isTablet = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}
