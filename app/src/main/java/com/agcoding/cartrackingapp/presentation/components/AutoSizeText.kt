package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers

/**
 * A single line of text that shrinks until it fits the width it is given.
 *
 * For the figures that live inside something: the total in the middle of the
 * donut, a value in a narrow tile. These have a fixed hole to sit in and a
 * value whose length nobody controls — "988,46 €" and "154.611,52 €" are the
 * same field, and the type size that suits the first spills out of the ring
 * for the second. Ellipsising is not an option when the thing being cut off is
 * the number the card exists to show.
 *
 * It measures, and if the line overflowed it drops the size by 8% and measures
 * again, down to [minFontSize]. Each pass is a recomposition, so the text is
 * held invisible until a pass fits — otherwise the first frame shows the
 * oversized version and the number visibly jumps as it settles.
 *
 * The caller still has to give it a bounded width; `Modifier.width(...)` or a
 * weight in a Row. With no bound there is nothing to overflow and nothing to
 * shrink.
 */
@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 11.sp,
    textAlign: TextAlign? = null,
) {
    // Keyed on the text and the starting size: a new value starts over at full
    // size rather than inheriting the shrink the previous one needed.
    var resolved by remember(text, style) { mutableStateOf(style) }
    var measured by remember(text, style) { mutableStateOf(false) }

    Text(
        text = text,
        style = resolved,
        color = color,
        maxLines = 1,
        softWrap = false,
        textAlign = textAlign,
        modifier = modifier.drawWithContent { if (measured) drawContent() },
        onTextLayout = { result ->
            if (result.didOverflowWidth && resolved.fontSize > minFontSize) {
                resolved = resolved.copy(fontSize = resolved.fontSize * 0.92f)
            } else {
                measured = true
            }
        },
    )
}

@Preview(name = "Auto-size — in a 140dp hole", showBackground = true, widthDp = 300)
@Composable
private fun AutoSizeTextPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        val scheme = LocalAppColorScheme.current
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            listOf("9,90 €", "988,46 €", "12.480,00 €", "154.611,52 €").forEach {
                AutoSizeText(
                    text = it,
                    style = AppNumbers.donut,
                    color = scheme.contentPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(140.dp),
                )
            }
        }
    }
}
