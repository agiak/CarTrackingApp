package com.agcoding.cartrackingapp.presentation.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
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
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryFuel
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryOther
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService

/**
 * One record in the list: a refill, a service, or another expense.
 *
 * The tile on the left is the kind of record, not its date. The date tile
 * carried a day number and a weekday in a column of other day numbers, which
 * is the one thing in the row that is almost never what you are looking for —
 * scrolling a month of records, "is this fuel or a service" is answered dozens
 * of times and "was it the 4th or the 11th" hardly ever. The category was a
 * 6dp dot in the corner of that tile, which is as much as a dot can say.
 *
 * So the tile is now the category's own icon in the category's own colour —
 * the same pump, wrench and receipt the details screens and the cost bar use —
 * and the date moves into the subtitle beside the car and the time, where it
 * is still there to be read but is not taking the row's most prominent
 * position.
 *
 * The tint stays on the tile rather than on the card. At this density a tinted
 * card per row turns the list into stripes, and the thing the eye follows down
 * the page is the amount on the right.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    amount: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tags: List<String> = emptyList(),
) {
    val scheme = LocalAppColorScheme.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, scheme.borderDefault, RoundedCornerShape(22.dp))
            .background(scheme.backgroundCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        CategoryTile(icon = icon, accent = accent)

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(7.dp))
                // Wraps rather than scrolls or truncates: on a narrow phone in
                // a large font the three figures still all have to be legible,
                // and a second line costs less than a hidden one.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    tags.forEach { tag -> Tag(tag) }
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = amount,
            style = AppNumbers.value,
            color = scheme.contentPrimary,
            maxLines = 1,
        )
    }
}

/**
 * What kind of record this is.
 *
 * A square rather than the 50x56 the date tile needed, so the row is shorter
 * and more of them fit on a screen. The glyph sits on its own colour at low
 * alpha: full strength, three of these in a column read as three buttons.
 */
@Composable
private fun CategoryTile(icon: ImageVector, accent: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.16f)),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun Tag(text: String) {
    val scheme = LocalAppColorScheme.current
    Text(
        text = text,
        style = AppNumbers.tag,
        color = scheme.contentSecondary,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(scheme.backgroundSecondary)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TransactionCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        val scheme = LocalAppColorScheme.current
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            TransactionCard(
                icon = Icons.Default.LocalGasStation,
                title = "Ανεφοδιασμός",
                subtitle = "4 Οκτ · Toyota Corolla · 17:17",
                amount = "96,20 €",
                accent = scheme.categoryFuel,
                tags = listOf("69,9 L", "700 km", "10,0 L/100"),
                onClick = {},
            )
            TransactionCard(
                icon = Icons.Default.Build,
                title = "Μεγάλο σέρβις",
                subtitle = "28 Σεπ · VW Golf · 09:40",
                amount = "286,00 €",
                accent = scheme.categoryService,
                onClick = {},
            )
            TransactionCard(
                icon = Icons.Default.Receipt,
                title = "Πλύσιμο",
                subtitle = "12 Σεπ · Toyota Corolla · 13:02",
                amount = "15,00 €",
                accent = scheme.categoryOther,
                onClick = {},
            )
        }
    }
}
