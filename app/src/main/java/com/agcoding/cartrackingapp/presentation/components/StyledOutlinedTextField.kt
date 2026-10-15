package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * The app's text field: filled, not outlined.
 *
 * A form of outlined fields is a form of empty rectangles — on a screen of
 * cards, every border competes with the card edges around it and the eye has
 * to sort out which lines mean "surface" and which mean "type here". Filled on
 * the card surface, a field reads as a slot in the card instead, and the only
 * border left is the one that appears when the field is focused or wrong,
 * where a line actually carries information.
 *
 * It stays an OutlinedTextField underneath on purpose. The component has
 * eleven slot parameters and is called from every form in the app; swapping in
 * a hand-rolled field would mean re-testing all of them to change how a
 * rectangle is painted. Making the resting border transparent and filling the
 * container gets the whole design with no call site touched.
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(14.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        errorContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        // Transparent rather than absent: the border still occupies its
        // 1dp, so focusing a field does not nudge its contents sideways.
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        errorBorderColor = MaterialTheme.colorScheme.tertiary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        errorLabelColor = MaterialTheme.colorScheme.tertiary,
        focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
        cursorColor = MaterialTheme.colorScheme.primary,
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
        visualTransformation = visualTransformation,
        shape = shape,
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

