package com.agcoding.cartrackingapp.presentation.cloud.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * A password field with the eye toggle.
 *
 * Every account screen needs the same thing, and getting it wrong — leaving
 * autocorrect on, or offering the wrong IME action — is the kind of detail
 * that makes typing a password on a phone miserable.
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    supportingText: String? = null,
    isError: Boolean = false,
) {
    StyledOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility, enabled = enabled) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(
                        if (visible) R.string.cloud_hide_password else R.string.cloud_show_password
                    )
                )
            }
        },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        }
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewPasswordFieldHidden() {
    CarTrackingAppTheme {
        PasswordField(
            value = "secret123",
            onValueChange = {},
            label = "Password",
            visible = false,
            onToggleVisibility = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewPasswordFieldVisible() {
    CarTrackingAppTheme {
        PasswordField(
            value = "secret123",
            onValueChange = {},
            label = "New password",
            visible = true,
            onToggleVisibility = {},
            supportingText = "At least 6 characters",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
