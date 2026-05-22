package com.agcoding.cartrackingapp.shared.ui.components.textfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.theme.AppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.utils.UiText
import com.agcoding.cartrackingapp.shared.ui.utils.asString

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: UiText? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scheme  = LocalAppColorScheme.current
    val dimens  = LocalAppDimens.current
    val tfColors = scheme.defaultTextFieldColors()

    Column(modifier = modifier) {
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,
            label           = { Text(label) },
            placeholder     = placeholder?.let { { Text(it) } },
            isError         = error != null,
            enabled         = enabled,
            readOnly        = readOnly,
            singleLine      = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingIcon    = trailingIcon?.let {
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(it, contentDescription = null)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = tfColors.focusedBorderColor,
                unfocusedBorderColor = tfColors.unfocusedBorderColor,
                errorBorderColor     = tfColors.errorBorderColor,
                focusedLabelColor    = tfColors.focusedLabelColor,
                unfocusedLabelColor  = tfColors.unfocusedLabelColor,
                errorLabelColor      = tfColors.errorLabelColor,
                cursorColor          = tfColors.cursorColor,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        // Always reserves height to prevent layout jump when error appears/disappears.
        Text(
            text     = error?.asString(context) ?: "",
            color    = if (error != null) tfColors.errorMessageColor else Color.Transparent,
            style    = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(
                start = dimens.spacing.sm,
                top   = dimens.spacing.xs,
            ),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "AppTextField – Default Light", showBackground = true)
@Composable
private fun PreviewTfDefaultLight() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = false) {
        AppTextField(value = "Toyota Corolla", onValueChange = {}, label = "Car name")
    }
}

@Preview(name = "AppTextField – Default Dark", showBackground = true)
@Composable
private fun PreviewTfDefaultDark() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = true) {
        AppTextField(value = "", onValueChange = {}, label = "Car name", placeholder = "Enter name")
    }
}

@Preview(name = "AppTextField – Ocean Light", showBackground = true)
@Composable
private fun PreviewTfOceanLight() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = false) {
        AppTextField(
            value = "abc",
            onValueChange = {},
            label = "License plate",
            error = UiText.DynamicString("Field is required"),
        )
    }
}

@Preview(name = "AppTextField – Ocean Dark", showBackground = true)
@Composable
private fun PreviewTfOceanDark() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = true) {
        AppTextField(value = "", onValueChange = {}, label = "Notes")
    }
}
