package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme

/**
 * The app's text field. There is one, and this is it.
 *
 * Every form in the app calls this — add car, add refill, add expense, edit
 * reminder, create trip, the quick-entry widget — so a field looks and behaves
 * the same wherever it is met. Two other field components used to exist beside
 * it; a form could not be read as "a form" when the boxes in it were drawn
 * three different ways.
 *
 * **Filled, with a border at rest.** The fill is [AppColorScheme.backgroundSecondary]
 * rather than the card colour, and this is the part that was wrong: a field
 * filled with the surface it sits on is invisible, which is exactly what
 * happened in the add-car sheet, where the sheet is a card and the fields were
 * card-coloured too. backgroundSecondary is a step away from both the screen
 * background and the card, so the slot is visible on either. The hairline
 * border is there for the same reason — it says "type here" before the user
 * has touched anything, and the focused and error states then only have to
 * change its colour, not conjure a line out of nothing.
 *
 * It stays an OutlinedTextField underneath on purpose. The component has
 * eleven slot parameters and is called from every form in the app; swapping in
 * a hand-rolled field would mean re-testing all of them to change how a
 * rectangle is painted.
 */
@Composable
fun AppTextField(
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
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(14.dp),
    colors: TextFieldColors = appTextFieldColors()
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
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = shape,
        colors = colors
    )
}

/**
 * The field's palette, from the user's chosen scheme rather than from
 * Material's roles.
 *
 * Exposed because a few call sites hand the same colours to a Material
 * component this wrapper does not cover — the exposed dropdown's anchor, for
 * one. They should look like a field because they are one.
 *
 * @param disabledLooksEnabled for the fields that are disabled only so the
 *   component underneath stops swallowing taps — the date picker opens a
 *   dialog instead of a keyboard, so its field must not be typeable, but it is
 *   not *unavailable* and should not be greyed out as if it were.
 */
@Composable
fun appTextFieldColors(disabledLooksEnabled: Boolean = false): TextFieldColors {
    val scheme = LocalAppColorScheme.current
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = scheme.backgroundSecondary,
        unfocusedContainerColor = scheme.backgroundSecondary,
        disabledContainerColor = if (disabledLooksEnabled) {
            scheme.backgroundSecondary
        } else {
            scheme.backgroundSecondary.copy(alpha = 0.5f)
        },
        errorContainerColor = scheme.backgroundSecondary,

        focusedTextColor = scheme.contentPrimary,
        unfocusedTextColor = scheme.contentPrimary,
        disabledTextColor = if (disabledLooksEnabled) {
            scheme.contentPrimary
        } else {
            scheme.contentDisabled
        },
        errorTextColor = scheme.contentPrimary,

        // A visible hairline at rest, not a transparent one. The field has to
        // read as a slot before it is focused; that is the whole job of the
        // resting state.
        focusedBorderColor = scheme.actionPrimary,
        unfocusedBorderColor = scheme.borderDefault,
        disabledBorderColor = if (disabledLooksEnabled) {
            scheme.borderDefault
        } else {
            scheme.borderDefault.copy(alpha = 0.5f)
        },
        errorBorderColor = scheme.statusError,

        focusedLabelColor = scheme.actionPrimary,
        unfocusedLabelColor = scheme.contentSecondary,
        disabledLabelColor = if (disabledLooksEnabled) {
            scheme.contentSecondary
        } else {
            scheme.contentDisabled
        },
        errorLabelColor = scheme.statusError,

        focusedPlaceholderColor = scheme.contentDisabled,
        unfocusedPlaceholderColor = scheme.contentDisabled,
        disabledPlaceholderColor = scheme.contentDisabled,

        focusedLeadingIconColor = scheme.contentSecondary,
        unfocusedLeadingIconColor = scheme.contentSecondary,
        focusedTrailingIconColor = scheme.contentSecondary,
        unfocusedTrailingIconColor = scheme.contentSecondary,
        disabledTrailingIconColor = if (disabledLooksEnabled) {
            scheme.contentSecondary
        } else {
            scheme.contentDisabled
        },

        focusedSupportingTextColor = scheme.contentSecondary,
        unfocusedSupportingTextColor = scheme.contentSecondary,
        errorSupportingTextColor = scheme.statusError,

        cursorColor = scheme.actionPrimary,
        errorCursorColor = scheme.statusError
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Field — states, dark", showBackground = true, widthDp = 360)
@Composable
private fun PreviewAppTextFieldDark() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            AppTextField(
                value = "",
                onValueChange = {},
                label = { Text("Car name") },
                placeholder = { Text("Corolla") },
                singleLine = true
            )
            AppTextField(
                value = "Corolla",
                onValueChange = {},
                label = { Text("Car name") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true
            )
            AppTextField(
                value = "abc",
                onValueChange = {},
                label = { Text("License plate") },
                isError = true,
                supportingText = { Text("Field is required") },
                singleLine = true
            )
            AppTextField(
                value = "Read only",
                onValueChange = {},
                label = { Text("Date") },
                enabled = false,
                singleLine = true
            )
        }
    }
}

@Preview(name = "Field — states, light", showBackground = true, widthDp = 360)
@Composable
private fun PreviewAppTextFieldLight() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            AppTextField(
                value = "",
                onValueChange = {},
                label = { Text("Notes") },
                placeholder = { Text("Optional") }
            )
            AppTextField(
                value = "1.000",
                onValueChange = {},
                label = { Text("Odometer") },
                suffix = { Text("km") },
                singleLine = true
            )
        }
    }
}
