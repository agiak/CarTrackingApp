package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * A styled OutlinedTextField with consistent border colors matching the app's card design.
 * Uses the same outline variant color as StyledCard for visual consistency.
 */
@Composable
fun StyledOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        errorBorderColor = MaterialTheme.colorScheme.error,
    )
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        colors = colors
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Styled TextField - Empty", showBackground = true, widthDp = 350)
@Composable
private fun PreviewStyledOutlinedTextFieldEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        StyledOutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Label") },
            placeholder = { Text("Enter text here") },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Styled TextField - With Value", showBackground = true, widthDp = 350)
@Composable
private fun PreviewStyledOutlinedTextFieldWithValue() {
    CarTrackingAppTheme(darkTheme = false) {
        StyledOutlinedTextField(
            value = "Sample text content",
            onValueChange = {},
            label = { Text("Label") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = null)
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Styled TextField - Error State", showBackground = true, widthDp = 350)
@Composable
private fun PreviewStyledOutlinedTextFieldError() {
    CarTrackingAppTheme(darkTheme = false) {
        StyledOutlinedTextField(
            value = "Invalid input",
            onValueChange = {},
            label = { Text("Label") },
            isError = true,
            supportingText = { Text("This field contains an error") },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Styled TextField - Dark Mode", showBackground = true, widthDp = 350)
@Composable
private fun PreviewStyledOutlinedTextFieldDark() {
    CarTrackingAppTheme(darkTheme = true) {
        StyledOutlinedTextField(
            value = "Dark mode text",
            onValueChange = {},
            label = { Text("Label") },
            placeholder = { Text("Enter text") },
            modifier = Modifier.padding(16.dp)
        )
    }
}

