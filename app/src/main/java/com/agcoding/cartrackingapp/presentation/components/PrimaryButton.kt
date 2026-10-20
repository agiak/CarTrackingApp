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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.readableContent

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
    val shape = RoundedCornerShape(dimens.radius.button + 2.dp)

    val accent = if (destructive) scheme.statusError else scheme.actionPrimary
    // Disabled is a pair of colours, not a `Modifier.alpha` over the finished
    // button. The alpha layer sat after `background` in the chain, so it faded
    // the label and left the fill at full strength — a disabled button came
    // out louder than an enabled one, with ghost text on it.
    val container = if (enabled) accent else accent.copy(alpha = 0.30f)
    val content = when {
        !enabled -> scheme.contentSecondary.copy(alpha = 0.7f)
        // Worked out from the red rather than borrowed from the accent: in
        // dark mode statusError is a pale pink (#F09595), on which the white
        // this used to hardcode scores 2.2:1.
        destructive -> scheme.statusError.readableContent()
        else -> scheme.actionContent
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            // The fill is a plain draw, shaped in place. Clipping first put it
            // inside a graphics layer for no reason: the only thing that has
            // to be clipped here is the ripple.
            .background(container, shape)
            .clip(shape)
            .clickable(enabled = clickable, onClick = onClick),
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
 *
 * @param contentColor recolours the label and icon while the container stays
 *   quiet. Delete beside Edit is the case it exists for: both are secondary
 *   actions, so neither should be filled, but only one of them is irreversible
 *   and that difference has to be visible.
 */
@Composable
fun TonalButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    contentColor: Color? = null,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val shape = RoundedCornerShape(dimens.radius.button + 2.dp)
    val ink = contentColor ?: scheme.contentPrimary
    // See [PrimaryButton]: the state is in the colours, not in a layer over
    // the top of them.
    val container =
        if (enabled) scheme.backgroundSecondary else scheme.backgroundSecondary.copy(alpha = 0.5f)
    val content = if (enabled) ink else scheme.contentSecondary.copy(alpha = 0.7f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .background(container, shape)
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) scheme.actionPrimary else scheme.contentDisabled,
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
