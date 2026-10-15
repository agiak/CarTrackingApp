package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers

/**
 * A car's plate, drawn as a plate.
 *
 * Monospaced and letter-spaced inside a thin rectangle, because that is what
 * the thing itself looks like — which means it needs no label to say what it
 * is, and can sit next to the car's name without competing with it.
 *
 * Renders nothing at all when the plate is blank. A car without one would
 * otherwise show an empty box, which reads as a missing value rather than as
 * a car the user simply never typed a plate for.
 */
@Composable
fun LicensePlateBadge(
    plate: String,
    modifier: Modifier = Modifier,
) {
    if (plate.isBlank()) return

    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Box(
        modifier = modifier
            .border(
                BorderStroke(dimens.size.divider, scheme.borderDefault),
                RoundedCornerShape(dimens.radius.plate),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = plate.uppercase(),
            style = AppNumbers.plate,
            color = scheme.contentSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun LicensePlateBadgePreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp),
        ) {
            LicensePlateBadge("ΙΖΧ-4821")
            LicensePlateBadge("ABC 1234")
            LicensePlateBadge("")
        }
    }
}
