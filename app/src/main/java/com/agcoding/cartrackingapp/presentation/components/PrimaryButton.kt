package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/** How tall every full-width action in the redesign is. */
private val ButtonHeight = 52.dp

/**
 * The action a screen is for: Save, Add car, Continue, Sign in.
 *
 * Full width and 52dp tall, which on a phone makes it the easiest thing on the
 * screen to hit — appropriate, because on these screens it is the only thing
 * the user came to do.
 *
 * While busy it shows a spinner **in place of** its label rather than beside
 * it. Beside it, the button changes width mid-tap and the label can wrap; in
 * place, the button is a fixed shape that is either asking to be pressed or
 * visibly working.
 *
 * @param destructive fills with the error colour instead of the accent, for
 *   the irreversible confirmations — clearing all data, deleting a car. The
 *   accent means "this is the thing to do"; these are not.
 */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isBusy: Boolean = false,
    destructive: Boolean = false,
    icon: ImageVector? = null,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    // A busy button is not a disabled one, but it must not be pressed twice.
    val clickable = enabled && !isBusy
    val container = if (destructive) scheme.statusError else scheme.actionPrimary
    // White on the error red as well: actionContent is whatever reads on the
    // accent, which is not guaranteed to read on red.
    val content = if (destructive) Color.White else scheme.actionContent

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .clip(RoundedCornerShape(dimens.radius.button + 2.dp))
            .background(container)
            .clickable(enabled = clickable, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f),
    ) {
        if (isBusy) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = content,
                modifier = Modifier.size(18.dp),
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = content,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * The second choice next to a [PrimaryButton] — Cancel, Sign in beside Create
 * account. Same size and shape, on the quiet surface, so a pair of them reads
 * as one control with two halves rather than as two unrelated buttons.
 */
@Composable
fun TonalButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .clip(RoundedCornerShape(dimens.radius.button + 2.dp))
            .background(scheme.backgroundSecondary)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.contentPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The third option, for the thing that is neither the action nor its
 * alternative: "Continue without an account", "Skip for now".
 */
@Composable
fun AppTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = LocalAppColorScheme.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = scheme.actionPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PrimaryButtonPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            PrimaryButton(label = "Save refill", onClick = {}, icon = Icons.Default.Save)
            PrimaryButton(label = "Saving", onClick = {}, isBusy = true)
            PrimaryButton(label = "Add car", onClick = {}, enabled = false)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TonalButton(label = "Cancel", onClick = {}, modifier = Modifier.weight(1f))
                PrimaryButton(label = "Add car", onClick = {}, modifier = Modifier.weight(1f))
            }
            AppTextButton(label = "Continue without an account", onClick = {})
        }
    }
}
