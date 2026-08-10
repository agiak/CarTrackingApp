package com.agcoding.cartrackingapp.presentation.editexpense.components

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.components.ThousandsSeparatorTransformation
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ExpenseAmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    StyledOutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        label = { Text(stringResource(R.string.expense_amount_eur)) },
        placeholder = { Text(stringResource(R.string.amount_placeholder)) },
        leadingIcon = {
            Text(
                text = "€",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        visualTransformation = ThousandsSeparatorTransformation(),
        singleLine = true,
        modifier = modifier
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Expense Amount Field - Empty", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseAmountFieldEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("") }

        ExpenseAmountField(
            amount = amount,
            onAmountChange = { amount = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Expense Amount Field - With Value", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseAmountFieldWithValue() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("65.50") }

        ExpenseAmountField(
            amount = amount,
            onAmountChange = { amount = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Expense Amount Field - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewExpenseAmountFieldDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var amount by remember { mutableStateOf("120.00") }

        ExpenseAmountField(
            amount = amount,
            onAmountChange = { amount = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
