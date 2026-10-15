@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * The one date filter in the app: Transactions, Statistics and Car Details all
 * open this, so the interaction is learned once.
 *
 * The model underneath is a set of years crossed with a set of months — months
 * narrow *every* selected year, so 2024 and 2025 with March and April means
 * those two months in both. The sheet makes that visible by keeping years and
 * months in separate grids rather than pretending to offer a date range.
 *
 * Choices are staged and committed with Apply, unlike the app's other filter
 * sheets. It is the right exception: picking "March 2025" is two taps, and
 * applying live would re-query and redraw the screen behind the sheet after
 * the first of them, with a year's worth of data flashing past on the way to
 * what the user actually wanted.
 *
 * @param availableYears years that actually have data, newest first. The
 *   current year is always offered even with no records yet, so the grid is
 *   never empty.
 */
@Composable
fun DateFilterSheet(
    selected: DateFilter,
    availableYears: List<Int>,
    onFilterChange: (DateFilter) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    // Re-seeded whenever the screen's filter changes, so reopening the sheet
    // always starts from what is actually on screen rather than from whatever
    // was abandoned last time.
    var draft by remember(selected) { mutableStateOf(selected) }

    val years = remember(availableYears) {
        (availableYears + YearMonth.now().year).distinct().sortedDescending()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = dimens.radius.sheet, topEnd = dimens.radius.sheet),
        containerColor = scheme.backgroundCard,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = { SheetHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // ── header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.date_filter_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = scheme.contentPrimary,
                    )
                    Spacer(Modifier.height(3.dp))
                    // What Apply would do, spelled out. Without it the grids
                    // are the only feedback, and two grids cannot say "March
                    // and April of 2024 and 2025" at a glance.
                    Text(
                        text = dateFilterLabel(draft),
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.actionPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(dimens.size.headerAction)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = scheme.contentSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // ── quick ─────────────────────────────────────────────────────
            SectionGap()
            SectionLabel(stringResource(R.string.date_filter_quick))
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                val now = YearMonth.now()
                AppFilterChip(
                    label = stringResource(R.string.date_filter_all_time),
                    selected = !draft.isActive,
                    onClick = { draft = DateFilter.None },
                )
                val thisMonth = DateFilter.of(now.year, now.monthValue)
                AppFilterChip(
                    label = stringResource(R.string.date_filter_this_month),
                    selected = draft == thisMonth,
                    onClick = { draft = thisMonth },
                )
                val lastThree = lastThreeMonths(now)
                AppFilterChip(
                    label = stringResource(R.string.date_filter_last_three_months),
                    selected = draft == lastThree,
                    onClick = { draft = lastThree },
                )
                val thisYear = DateFilter(years = setOf(now.year))
                AppFilterChip(
                    label = stringResource(R.string.date_filter_this_year),
                    selected = draft == thisYear,
                    onClick = { draft = thisYear },
                )
            }

            // ── year ──────────────────────────────────────────────────────
            SectionGap()
            SectionLabel(stringResource(R.string.date_filter_year))
            Spacer(Modifier.height(10.dp))
            CellGrid(
                items = years.map { it.toString() },
                selectedAt = { index -> years[index] in draft.years },
                enabledAt = { true },
                onClick = { index -> draft = draft.toggleYear(years[index]) },
            )

            // ── month ─────────────────────────────────────────────────────
            SectionGap()
            val monthsEnabled = draft.isActive
            val now = YearMonth.now()
            // Only when the current year is the *only* one selected: with an
            // earlier year also chosen, December is a real month to ask for.
            val onlyCurrentYear = draft.years == setOf(now.year)
            SectionLabel(stringResource(R.string.date_filter_month))
            Spacer(Modifier.height(10.dp))
            CellGrid(
                items = (1..12).map { shortMonthName(it) },
                selectedAt = { index -> (index + 1) in draft.months },
                enabledAt = { index ->
                    monthsEnabled && !(onlyCurrentYear && index + 1 > now.monthValue)
                },
                onClick = { index -> draft = draft.toggleMonth(index + 1) },
            )

            // ── footer ────────────────────────────────────────────────────
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetButton(
                    label = stringResource(R.string.date_filter_reset),
                    weight = 1f,
                    filled = false,
                    onClick = { draft = DateFilter.None },
                )
                SheetButton(
                    label = stringResource(R.string.date_filter_apply),
                    weight = 2f,
                    filled = true,
                    onClick = {
                        onFilterChange(draft.normalized)
                        onDismiss()
                    },
                )
            }
        }
    }
}

/**
 * The last three months, clamped to the current calendar year.
 *
 * A true rolling window would cross a year boundary in January and February,
 * and this filter cannot express that: years and months multiply out, so
 * {2025, 2026} x {12, 1, 2} would also select December 2026 and January 2025.
 * Returning a shorter window is wrong by omission; returning the cross product
 * would be wrong by addition, and silently inflate every total on the screen.
 */
private fun lastThreeMonths(now: YearMonth): DateFilter {
    val first = maxOf(1, now.monthValue - 2)
    return DateFilter(years = setOf(now.year), months = (first..now.monthValue).toSet())
}

@Composable
private fun SectionGap() = Spacer(Modifier.height(22.dp))

@Composable
private fun SheetHandle() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(LocalAppColorScheme.current.contentDisabled),
        )
    }
}

/**
 * A four-across grid of equal cells.
 *
 * Built from Rows rather than a LazyVerticalGrid because this lives inside a
 * bottom sheet that scrolls: a lazy grid there either fights the sheet for the
 * drag or collapses to zero height, and with at most twelve fixed cells there
 * is nothing to be lazy about.
 */
@Composable
private fun CellGrid(
    items: List<String>,
    selectedAt: (Int) -> Boolean,
    enabledAt: (Int) -> Boolean,
    onClick: (Int) -> Unit,
    columns: Int = 4,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.indices.chunked(columns).forEach { rowIndices ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowIndices.forEach { index ->
                    GridCell(
                        label = items[index],
                        selected = selectedAt(index),
                        enabled = enabledAt(index),
                        onClick = { onClick(index) },
                    )
                }
                // Keeps the last row's cells the same width as the rows above
                // instead of letting three cells stretch across four columns.
                repeat(columns - rowIndices.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RowScope.GridCell(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val scheme = LocalAppColorScheme.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) scheme.actionPrimary else scheme.backgroundSecondary)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.28f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) scheme.actionContent else scheme.contentPrimary,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RowScope.SheetButton(
    label: String,
    weight: Float,
    filled: Boolean,
    onClick: () -> Unit,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(weight)
            .height(48.dp)
            .clip(RoundedCornerShape(dimens.radius.button))
            .background(if (filled) scheme.actionPrimary else scheme.backgroundSecondary)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = if (filled) scheme.actionContent else scheme.contentPrimary,
            maxLines = 1,
        )
    }
}

/**
 * The control that opens [DateFilterSheet], showing what the screen is
 * currently scoped to so the period can be read without opening anything.
 */
@Composable
fun DateFilterButton(
    filter: DateFilter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
) {
    if (filter.isActive) {
        ActiveDateChip(
            label = dateFilterLabel(filter),
            onClick = onClick,
            modifier = modifier,
            onClear = onClear,
        )
    } else {
        AppFilterChip(
            label = dateFilterLabel(filter),
            selected = false,
            onClick = onClick,
            modifier = modifier,
        )
    }
}

/**
 * Human-readable description of [filter] — "All time", "2025", "March 2025",
 * "2023, 2024, 2025", "Mar, Apr · 2024, 2025".
 *
 * Long selections are truncated to keep the chip a chip: the first two entries
 * plus a "+N" tail.
 */
@Composable
fun dateFilterLabel(filter: DateFilter): String {
    if (!filter.isActive) return stringResource(R.string.date_filter_all_time)

    val years = filter.years.sorted()
    val months = filter.months.sorted()

    // The common single-year cases read better spelled out.
    if (years.size == 1 && months.isEmpty()) return years.first().toString()
    if (years.size == 1 && months.size == 1) {
        return "${fullMonthName(months.first())} ${years.first()}"
    }

    val yearsPart = summarize(years.map { it.toString() })
    if (months.isEmpty()) return yearsPart

    val monthsPart = summarize(months.map { shortMonthName(it) })
    return "$monthsPart · $yearsPart"
}

/** "a, b" for short lists, "a, b +N" once there are more than three. */
private fun summarize(values: List<String>): String = when {
    values.size <= 3 -> values.joinToString(", ")
    else -> values.take(2).joinToString(", ") + " +" + (values.size - 2)
}

/** Locale-aware standalone month name, e.g. "Μάρτιος" / "March". */
private fun fullMonthName(month: Int, locale: Locale = Locale.getDefault()): String =
    Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

/** Locale-aware short month name used on the compact month cells. */
private fun shortMonthName(month: Int, locale: Locale = Locale.getDefault()): String =
    Month.of(month).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

// ── previews ─────────────────────────────────────────────────────────────

@Preview(name = "Date filter button", showBackground = true)
@Composable
private fun PreviewDateFilterButtons() {
    CarTrackingAppTheme(darkTheme = true) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            DateFilterButton(filter = DateFilter.None, onClick = {})
            DateFilterButton(
                filter = DateFilter.of(year = 2026, month = 3),
                onClick = {},
                onClear = {},
            )
        }
    }
}
