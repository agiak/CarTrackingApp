package com.agcoding.cartrackingapp.presentation.editcar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
fun InsuranceSection(
    insuranceExpirationDate: Long?,
    onInsuranceExpirationDateChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.edit_car_section_insurance_information),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        StyledDatePickerField(
            value = insuranceExpirationDate,
            onDateSelected = onInsuranceExpirationDateChange,
            label = { Text(stringResource(R.string.edit_car_field_insurance_expiration_date)) },
            placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) }
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Insurance Section - With Date", showBackground = true, widthDp = 380)
@Composable
private fun PreviewInsuranceSectionWithDate() {
    CarTrackingAppTheme(darkTheme = false) {
        var insuranceDate by remember { mutableLongStateOf(System.currentTimeMillis() + 180L * 24 * 60 * 60 * 1000) }

        InsuranceSection(
            insuranceExpirationDate = insuranceDate,
            onInsuranceExpirationDateChange = { insuranceDate = it ?: 0L },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Insurance Section - No Date", showBackground = true, widthDp = 380)
@Composable
private fun PreviewInsuranceSectionNoDate() {
    CarTrackingAppTheme(darkTheme = false) {
        var insuranceDate by remember { mutableStateOf<Long?>(null) }

        InsuranceSection(
            insuranceExpirationDate = insuranceDate,
            onInsuranceExpirationDateChange = { insuranceDate = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Insurance Section - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewInsuranceSectionDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var insuranceDate by remember { mutableLongStateOf(System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000) }

        InsuranceSection(
            insuranceExpirationDate = insuranceDate,
            onInsuranceExpirationDateChange = { insuranceDate = it ?: 0L },
            modifier = Modifier.padding(16.dp)
        )
    }
}
