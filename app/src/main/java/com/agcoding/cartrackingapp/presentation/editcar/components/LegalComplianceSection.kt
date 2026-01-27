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
import androidx.compose.runtime.mutableLongStateOf
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
import com.agcoding.cartrackingapp.presentation.components.StyledDatePickerField
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun LegalComplianceSection(
    kteoExpirationDate: Long?,
    onKteoExpirationDateChange: (Long?) -> Unit,
    emissionsCardExpirationDate: Long?,
    onEmissionsCardExpirationDateChange: (Long?) -> Unit,
    roadTaxAmount: String,
    onRoadTaxAmountChange: (String) -> Unit,
    roadTaxDueDate: Long?,
    onRoadTaxDueDateChange: (Long?) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.edit_car_section_legal_compliance),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Two-column for KTEO and Emissions on tablets
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StyledDatePickerField(
                    value = kteoExpirationDate,
                    onDateSelected = onKteoExpirationDateChange,
                    label = { Text(stringResource(R.string.edit_car_field_kteo_expiration_date)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) },
                    modifier = Modifier.weight(1f)
                )

                StyledDatePickerField(
                    value = emissionsCardExpirationDate,
                    onDateSelected = onEmissionsCardExpirationDateChange,
                    label = { Text(stringResource(R.string.edit_car_field_emissions_card_expiration)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Two-column for Road Tax fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StyledOutlinedTextField(
                    value = roadTaxAmount,
                    onValueChange = onRoadTaxAmountChange,
                    label = { Text(stringResource(R.string.edit_car_field_road_tax_amount_eur)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_road_tax_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                StyledDatePickerField(
                    value = roadTaxDueDate,
                    onDateSelected = onRoadTaxDueDateChange,
                    label = { Text(stringResource(R.string.edit_car_field_road_tax_due_date)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            StyledDatePickerField(
                value = kteoExpirationDate,
                onDateSelected = onKteoExpirationDateChange,
                label = { Text(stringResource(R.string.edit_car_field_kteo_expiration_date)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) }
            )

            StyledDatePickerField(
                value = emissionsCardExpirationDate,
                onDateSelected = onEmissionsCardExpirationDateChange,
                label = { Text(stringResource(R.string.edit_car_field_emissions_card_expiration)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) }
            )

            StyledOutlinedTextField(
                value = roadTaxAmount,
                onValueChange = onRoadTaxAmountChange,
                label = { Text(stringResource(R.string.edit_car_field_road_tax_amount_eur)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_road_tax_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            StyledDatePickerField(
                value = roadTaxDueDate,
                onDateSelected = onRoadTaxDueDateChange,
                label = { Text(stringResource(R.string.edit_car_field_road_tax_due_date)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) }
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Legal Compliance Section - Phone", showBackground = true, widthDp = 380)
@Composable
private fun PreviewLegalComplianceSectionPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        var kteoDate by remember { mutableLongStateOf(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000) }
        var emissionsDate by remember { mutableLongStateOf(System.currentTimeMillis() + 180L * 24 * 60 * 60 * 1000) }
        var roadTaxAmount by remember { mutableStateOf("450.00") }
        var roadTaxDueDate by remember { mutableLongStateOf(System.currentTimeMillis() + 60L * 24 * 60 * 60 * 1000) }

        LegalComplianceSection(
            kteoExpirationDate = kteoDate,
            onKteoExpirationDateChange = { kteoDate = it ?: 0L },
            emissionsCardExpirationDate = emissionsDate,
            onEmissionsCardExpirationDateChange = { emissionsDate = it ?: 0L },
            roadTaxAmount = roadTaxAmount,
            onRoadTaxAmountChange = { roadTaxAmount = it },
            roadTaxDueDate = roadTaxDueDate,
            onRoadTaxDueDateChange = { roadTaxDueDate = it ?: 0L },
            isTablet = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Legal Compliance Section - Tablet", showBackground = true, widthDp = 800)
@Composable
private fun PreviewLegalComplianceSectionTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        var kteoDate by remember { mutableLongStateOf(System.currentTimeMillis() + 730L * 24 * 60 * 60 * 1000) }
        var emissionsDate by remember { mutableLongStateOf(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000) }
        var roadTaxAmount by remember { mutableStateOf("320.50") }
        var roadTaxDueDate by remember { mutableLongStateOf(System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000) }

        LegalComplianceSection(
            kteoExpirationDate = kteoDate,
            onKteoExpirationDateChange = { kteoDate = it ?: 0L },
            emissionsCardExpirationDate = emissionsDate,
            onEmissionsCardExpirationDateChange = { emissionsDate = it ?: 0L },
            roadTaxAmount = roadTaxAmount,
            onRoadTaxAmountChange = { roadTaxAmount = it },
            roadTaxDueDate = roadTaxDueDate,
            onRoadTaxDueDateChange = { roadTaxDueDate = it ?: 0L },
            isTablet = true,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(name = "Legal Compliance Section - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewLegalComplianceSectionDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var kteoDate by remember { mutableStateOf<Long?>(null) }
        var emissionsDate by remember { mutableStateOf<Long?>(null) }
        var roadTaxAmount by remember { mutableStateOf("") }
        var roadTaxDueDate by remember { mutableStateOf<Long?>(null) }

        LegalComplianceSection(
            kteoExpirationDate = kteoDate,
            onKteoExpirationDateChange = { kteoDate = it },
            emissionsCardExpirationDate = emissionsDate,
            onEmissionsCardExpirationDateChange = { emissionsDate = it },
            roadTaxAmount = roadTaxAmount,
            onRoadTaxAmountChange = { roadTaxAmount = it },
            roadTaxDueDate = roadTaxDueDate,
            onRoadTaxDueDateChange = { roadTaxDueDate = it },
            isTablet = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}
