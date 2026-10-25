package com.agcoding.cartrackingapp.presentation.statistics.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.StationSummary
import com.agcoding.cartrackingapp.domain.model.TopRefill
import com.agcoding.cartrackingapp.domain.model.TopRefillSort
import com.agcoding.cartrackingapp.presentation.components.CostSegment
import com.agcoding.cartrackingapp.presentation.components.IconTile
import com.agcoding.cartrackingapp.presentation.components.RowAccent
import com.agcoding.cartrackingapp.presentation.components.StackedCostBar
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.shared.ui.tokens.seriesColors
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The total for the period, and how it divides between stations.
 *
 * On a card surface rather than the accent fill the other heroes use: the bar
 * below the figure is the point of this card, and its bands are derived from
 * the accent, so on an accent background they would be invisible.
 */
@Composable
fun StationsHeroCard(
    totalCost: Double,
    stationCount: Int,
    refillCount: Int,
    stations: List<StationSummary>,
    modifier: Modifier = Modifier,
) {
    val scheme = LocalAppColorScheme.current
    val segments = stationSegments(stations)

    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.fuel_stations_spent_label),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.contentSecondary,
            )
            Spacer(Modifier.height(6.dp))
            HeroAmount(totalCost)
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(
                    R.string.fuel_stations_hero_meta,
                    pluralStringResource(
                        R.plurals.fuel_stations_station_count, stationCount, stationCount
                    ),
                    pluralStringResource(
                        R.plurals.fuel_stations_refill_count, refillCount, refillCount
                    ),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.contentSecondary,
            )
            if (segments.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                StackedCostBar(segments = segments)
            }
        }
    }
}

/**
 * The bands of the hero bar.
 *
 * Only the leading stations get a band of their own; the tail is added into a
 * single last one. With a gap between every pair, thirty stations would be
 * thirty slivers and twenty-nine gaps — a bar that shows less than no bar at
 * all. The list underneath is where the long tail is readable.
 */
@Composable
private fun stationSegments(stations: List<StationSummary>): List<CostSegment> {
    val scheme = LocalAppColorScheme.current
    val spending = stations.filter { it.totalCost > 0.0 }
    if (spending.isEmpty()) return emptyList()

    val leading = spending.take(MAX_BANDS)
    val tail = spending.drop(MAX_BANDS)
    val bandCount = leading.size + if (tail.isEmpty()) 0 else 1
    val colors = scheme.seriesColors(bandCount)

    return buildList {
        leading.forEachIndexed { index, station ->
            add(CostSegment(station.totalCost, colors[index], station.name))
        }
        if (tail.isNotEmpty()) {
            add(CostSegment(tail.sumOf { it.totalCost }, colors.last(), ""))
        }
    }
}

private const val MAX_BANDS = 6

/**
 * One station: what it cost, and on a tap what that was made of.
 *
 * @param isCheapest colours the average price green, [isDearest] rose. Only one
 *   station can be each and only when there is something to compare against —
 *   see `FuelStationsUiState`.
 */
@Composable
fun StationRow(
    station: StationSummary,
    share: Double,
    expanded: Boolean,
    isCheapest: Boolean,
    isDearest: Boolean,
    onClick: () -> Unit,
    onShowRefills: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = LocalAppColorScheme.current
    val name = if (station.isUnknown) {
        stringResource(R.string.fuel_stations_unknown)
    } else {
        station.name
    }

    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.clickable(onClick = onClick)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            ) {
                IconTile(icon = Icons.Default.LocalGasStation)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.contentPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (station.address != null) {
                        Text(
                            text = station.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.contentSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = station.totalCost.formatMoney(),
                        style = AppNumbers.value,
                        color = scheme.contentPrimary,
                        maxLines = 1,
                    )
                    Text(
                        text = stringResource(
                            R.string.fuel_stations_row_meta,
                            pluralStringResource(
                                R.plurals.fuel_stations_refill_count,
                                station.refillCount,
                                station.refillCount,
                            ),
                            "${share.formatNumber(0)}%",
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.contentSecondary,
                        maxLines = 1,
                    )
                }
            }

            ExpandedPanel(visible = expanded) {
                DetailGrid(
                    listOf(
                        Detail(
                            label = stringResource(R.string.fuel_stations_liters),
                            value = "${station.totalLiters.formatNumber(1)} L",
                        ),
                        Detail(
                            label = stringResource(R.string.fuel_stations_avg_price),
                            value = "${station.averagePricePerLiter.formatNumber(3)} €/L",
                            color = when {
                                isCheapest -> scheme.statusSuccess
                                isDearest -> scheme.statusError
                                else -> null
                            },
                        ),
                        Detail(
                            label = stringResource(R.string.fuel_stations_avg_refill),
                            value = station.averageRefillCost.formatMoney(),
                        ),
                    )
                )
                Spacer(Modifier.height(12.dp))
                ExpandedFooter(
                    note = when {
                        isCheapest -> stringResource(R.string.fuel_stations_cheapest)
                        isDearest -> stringResource(R.string.fuel_stations_dearest)
                        else -> null
                    },
                    noteColor = if (isCheapest) scheme.statusSuccess else scheme.statusError,
                    actionLabel = stringResource(R.string.fuel_stations_show_refills),
                    onAction = onShowRefills,
                )
            }
        }
    }
}

/**
 * One refill in the ranking.
 *
 * The ranked figure is whichever the sort chips chose, so the big number on
 * the right changes meaning with the chips. Everything else the refill knows
 * stays behind the tap, including the two numbers that are not being ranked.
 */
@Composable
fun TopRefillRow(
    entry: TopRefill,
    rank: Int,
    sort: TopRefillSort,
    expanded: Boolean,
    onClick: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = LocalAppColorScheme.current
    val datePattern = stringResource(R.string.date_format_dd_mmm_yyyy)
    val dateFormat = remember(datePattern) { SimpleDateFormat(datePattern, Locale.getDefault()) }

    val station = entry.stationName.ifBlank { stringResource(R.string.fuel_stations_unknown) }
    val rankedValue = when (sort) {
        TopRefillSort.TOTAL_COST -> entry.refill.amountPaid.formatMoney()
        TopRefillSort.PRICE_PER_LITER -> "${entry.pricePerLiter.formatNumber(3)} €/L"
        TopRefillSort.LITERS -> "${entry.refill.litersAdded.formatNumber(1)} L"
    }

    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.clickable(onClick = onClick)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            ) {
                Text(
                    text = rank.toString().padStart(2, '0'),
                    style = AppNumbers.compact,
                    color = scheme.contentDisabled,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            R.string.top_refills_row_title,
                            station,
                            "${entry.refill.litersAdded.formatNumber(1)} L",
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.contentPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val date = dateFormat.format(Date(entry.refill.timestamp))
                    Text(
                        // A deleted car leaves its refills behind, so the name
                        // can be missing — in which case the date stands alone
                        // rather than trailing an empty separator.
                        text = entry.carName
                            ?.let { stringResource(R.string.top_refills_row_meta, date, it) }
                            ?: date,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.contentSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = rankedValue,
                    style = AppNumbers.value,
                    // Only the top of the list is accented. Colour every row
                    // and the colour stops saying "this is the one".
                    color = if (rank == 1) scheme.actionPrimary else scheme.contentPrimary,
                    maxLines = 1,
                )
            }

            ExpandedPanel(visible = expanded) {
                DetailGrid(
                    buildList {
                        add(
                            Detail(
                                label = stringResource(R.string.top_refills_total),
                                value = entry.refill.amountPaid.formatMoney(),
                            )
                        )
                        add(
                            Detail(
                                label = stringResource(R.string.top_refills_price),
                                value = "${entry.pricePerLiter.formatNumber(3)} €/L",
                            )
                        )
                        add(
                            Detail(
                                label = stringResource(R.string.top_refills_odometer),
                                value = "${entry.refill.odometerReading.formatNumber(0)} km",
                            )
                        )
                        // Only shown when it was actually worked out: a refill
                        // with no trip distance stores a zero, and "0,0 L/100"
                        // reads as a measurement rather than as a blank.
                        if (entry.refill.fuelConsumption > 0.0) {
                            add(
                                Detail(
                                    label = stringResource(R.string.top_refills_consumption),
                                    value = "${entry.refill.fuelConsumption.formatNumber(1)} L/100",
                                )
                            )
                        }
                        if (entry.stationAddress != null) {
                            add(
                                Detail(
                                    label = stringResource(R.string.top_refills_station),
                                    value = entry.stationAddress,
                                )
                            )
                        }
                        if (entry.tripName != null) {
                            add(
                                Detail(
                                    label = stringResource(R.string.top_refills_trip),
                                    value = entry.tripName,
                                )
                            )
                        }
                    }
                )
                Spacer(Modifier.height(12.dp))
                ExpandedFooter(
                    note = null,
                    noteColor = Color.Unspecified,
                    actionLabel = stringResource(R.string.top_refills_open),
                    onAction = onOpen,
                )
            }
        }
    }
}

// ============================================
// Shared pieces of an expanded row
// ============================================

private data class Detail(val label: String, val value: String, val color: Color? = null)

/**
 * The part of a row that is only there once it has been tapped.
 *
 * Separated from the always-visible half by a rule rather than by a change of
 * background: the row is one card, and a second surface inside it would read
 * as a second card that had lost its corners.
 */
@Composable
private fun ExpandedPanel(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            HorizontalDivider(
                thickness = 1.dp,
                color = LocalAppColorScheme.current.borderDefault,
            )
            Column(modifier = Modifier.padding(14.dp)) { content() }
        }
    }
}

/** Label-over-value cells, three to a line. */
@Composable
private fun DetailGrid(details: List<Detail>) {
    val scheme = LocalAppColorScheme.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        details.chunked(COLUMNS).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                line.forEach { detail ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detail.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.contentSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = detail.value,
                            style = AppNumbers.value,
                            color = detail.color ?: scheme.contentPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                // A short last line keeps its columns the width of the ones
                // above it instead of stretching to fill the card.
                repeat(COLUMNS - line.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private const val COLUMNS = 3

/** An optional note on the left, and the row's one way out on the right. */
@Composable
private fun ExpandedFooter(
    note: String?,
    noteColor: Color,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val scheme = LocalAppColorScheme.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (note != null) {
            Text(
                text = note,
                style = MaterialTheme.typography.labelMedium,
                color = noteColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
        } else {
            Spacer(Modifier.weight(1f))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onAction),
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.actionPrimary,
                maxLines = 1,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.actionPrimary,
            )
        }
    }
}

/**
 * A money figure with its cents set small, like the details screens do.
 *
 * Cut at the *last* separator: the app formats in the European style, so
 * "2.015,60 €" carries a thousands dot before the decimal comma, and looking
 * for the first of the two would set everything after "2" in the small size.
 */
@Composable
private fun HeroAmount(amount: Double) {
    val scheme = LocalAppColorScheme.current
    val text = amount.formatMoney()
    val cut = text.indexOfLast { it == ',' || it == '.' }

    Row(verticalAlignment = Alignment.Bottom) {
        if (cut > 0) {
            Text(
                text = text.substring(0, cut),
                style = AppNumbers.hero,
                color = scheme.contentPrimary,
                maxLines = 1,
            )
            Text(
                text = text.substring(cut),
                style = AppNumbers.heroFraction,
                color = scheme.contentSecondary,
                modifier = Modifier.padding(bottom = 4.dp),
                maxLines = 1,
            )
        } else {
            Text(
                text = text,
                style = AppNumbers.hero,
                color = scheme.contentPrimary,
                maxLines = 1,
            )
        }
    }
}
