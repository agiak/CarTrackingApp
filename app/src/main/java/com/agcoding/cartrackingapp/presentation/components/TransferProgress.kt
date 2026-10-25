package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.brand.RoadProgress
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import kotlin.math.roundToInt

/**
 * A file moving in or out of the app, as a figure over a road.
 *
 * Used for the first cloud sync and for an import — the two waits the user
 * sits in front of with nothing to do. The figure answers "how much longer";
 * the car answers "is it still going", which a number alone does not when it
 * rests on the same value for a few seconds.
 *
 * Both of them appear in the same first run, one after the other, so they
 * share a treatment: two different ones would read as two different kinds of
 * operation rather than as two steps of the same setup.
 */
@Composable
fun TransferProgress(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val scheme = LocalAppColorScheme.current
    val percent = (progress.coerceIn(0f, 1f) * 100).roundToInt()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$percent%",
            // Larger than any other figure in the app, because for the length
            // of this screen it is the only one.
            style = AppNumbers.summary.copy(fontSize = 46.sp, lineHeight = 52.sp),
            color = scheme.contentPrimary,
        )

        Spacer(modifier = Modifier.height(4.dp))

        RoadProgress(progress = progress, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TransferProgressPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            TransferProgress(progress = 0.62f)
        }
    }
}
