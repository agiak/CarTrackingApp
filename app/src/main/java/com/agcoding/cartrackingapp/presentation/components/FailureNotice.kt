package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.brand.ExpenseNotSaved
import com.agcoding.cartrackingapp.shared.ui.brand.RefillNotSaved
import com.agcoding.cartrackingapp.shared.ui.brand.rememberBrandColors
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme

/** Which save failed, and so which animation says so. */
enum class FailedKind { REFILL, EXPENSE }

/**
 * A save that did not go through: the matching animation beside the reason.
 *
 * Beside, not instead of the form. A failure is something the user has to act
 * on — fix a field, try again — so the form has to stay reachable, which rules
 * out the full-surface treatment a success gets. It sits where the plain error
 * line used to, on a tinted strip so that it reads as one block rather than as
 * a picture that happens to be above some text.
 *
 * [message] doubles as the replay key: a second failure with a different
 * reason plays the animation again, the same reason twice does not, which is
 * the right answer both times.
 */
@Composable
fun FailureNotice(
    kind: FailedKind,
    message: String,
    modifier: Modifier = Modifier,
    surface: Color = LocalAppColorScheme.current.backgroundCard,
) {
    val scheme = LocalAppColorScheme.current
    // The strip, not the card under it. statusErrorSubtle is the error colour
    // at 12% over [surface], so that is what the viewer sees — and the badge
    // rings and the wallet's pocket are cut out of what the viewer sees.
    val brand = rememberBrandColors(surface = lerp(surface, scheme.statusError, 0.12f))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(scheme.statusErrorSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (kind) {
            FailedKind.REFILL -> RefillNotSaved(
                modifier = Modifier.width(NOTICE_WIDTH),
                colors = brand,
                replayKey = message,
            )

            FailedKind.EXPENSE -> ExpenseNotSaved(
                modifier = Modifier.width(NOTICE_WIDTH),
                colors = brand,
                replayKey = message,
            )
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.statusError,
        )
    }
}

/** Small enough to sit in a form row, big enough for the badge to be legible. */
private val NOTICE_WIDTH = 86.dp

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun FailureNoticePreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Row(Modifier.padding(16.dp)) {
            FailureNotice(kind = FailedKind.REFILL, message = "Consumption looks wrong: 48.2 l/100 km")
        }
    }
}
