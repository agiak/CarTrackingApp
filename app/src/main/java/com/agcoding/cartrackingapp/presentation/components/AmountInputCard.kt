package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService

/**
 * The figure a form is really about: the amount of an expense, the cost of a
 * refill.
 *
 * On a screen of eight identical text fields, the one that the whole form
 * exists to capture looked exactly like the notes field. Here it gets a card
 * of its own and the hero type, so the first thing a thumb reaches for is the
 * first thing that needs typing — and the entered figure is legible from
 * arm's length, which matters when the other hand is holding a receipt.
 *
 * The unit is drawn beside the field rather than typed into it, so the value
 * stays a number the parser can read.
 */
@Composable
fun AmountInputCard(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suffix: String,
    modifier: Modifier = Modifier,
    placeholder: String = "0",
    isError: Boolean = false,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> scheme.categoryService
            focused -> scheme.actionPrimary
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "amountBorder",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radius.card))
                .background(scheme.backgroundCard)
                .border(
                    BorderStroke(1.5.dp, borderColor),
                    RoundedCornerShape(dimens.radius.card),
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.contentSecondary,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = AppNumbers.summary,
                            color = scheme.contentDisabled,
                            maxLines = 1,
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = AppNumbers.summary.copy(color = scheme.contentPrimary),
                        cursorBrush = SolidColor(scheme.actionPrimary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        visualTransformation = visualTransformation,
                        interactionSource = interactionSource,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.contentSecondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
        if (errorText != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.categoryService,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun AmountInputCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            AmountInputCard(
                value = "286,00",
                onValueChange = {},
                label = "Amount",
                suffix = "€",
            )
            AmountInputCard(
                value = "",
                onValueChange = {},
                label = "Amount",
                suffix = "€",
                isError = true,
                errorText = "Enter an amount",
            )
        }
    }
}
