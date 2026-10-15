package com.agcoding.cartrackingapp.presentation.transactions.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * One record in the list: a refill or an expense.
 *
 * The date is a tile on the left rather than a line of text, which is what
 * lets the day grouping go away entirely. The old screen inserted a header row
 * for every day that had records; on a month with activity most days that is
 * more headers than records, and the headers are the part nobody reads. A
 * column of date tiles gives the same scanning structure without spending a
 * row on it.
 *
 * The category is a 6dp dot on the tile, not a coloured icon or a tinted card.
 * At this density a tinted card per row turns the list into stripes, and the
 * thing the eye is actually following down the page is the amount on the
 * right.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionCard(
    day: String,
    weekday: String,
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
            .background(scheme.backgroundCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        DateTile(day = day, weekday = weekday, accent = accent)

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

@Composable
private fun DateTile(day: String, weekday: String, accent: Color) {
    val scheme = LocalAppColorScheme.current
    Box(
        modifier = Modifier
            .size(width = 50.dp, height = 56.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(scheme.backgroundSecondary),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                text = day,
                style = AppNumbers.tile,
                color = scheme.contentPrimary,
                maxLines = 1,
            )
            Text(
                text = weekday,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.contentSecondary,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(accent),
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
                day = "04", weekday = "ΚΥΡ",
                title = "Ανεφοδιασμός",
                subtitle = "Toyota Corolla · 17:17",
                amount = "96,20 €",
                accent = scheme.categoryFuel,
                tags = listOf("69,9 L", "700 km", "10,0 L/100"),
                onClick = {},
            )
            TransactionCard(
                day = "28", weekday = "ΤΡΙ",
                title = "Μεγάλο σέρβις",
                subtitle = "VW Golf · 09:40",
                amount = "286,00 €",
                accent = scheme.categoryService,
                onClick = {},
            )
            TransactionCard(
                day = "12", weekday = "ΣΑΒ",
                title = "Πλύσιμο",
                subtitle = "Toyota Corolla · 13:02",
                amount = "15,00 €",
                accent = scheme.categoryOther,
                onClick = {},
            )
        }
    }
}
