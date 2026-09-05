@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * The one date filter UI in the app: pick a year, then optionally a month inside it.
 *
 * Every screen that filters by date uses this sheet so the interaction is identical
 * everywhere. Selections apply immediately — there is no separate confirm step —
 * which matches the other filter sheets.
 *
 * @param availableYears years that actually have data, newest first. The current year
 *   is always offered even when it has no records yet, so the list is never empty.
 */
@Composable
fun DateFilterSheet(
    selected: DateFilter,
    availableYears: List<Int>,
    onFilterChange: (DateFilter) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val years = remember(availableYears) {
        (availableYears + YearMonth.now().year).distinct().sortedDescending()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.date_filter_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (selected.isActive) {
                    TextButton(onClick = { onFilterChange(DateFilter.None) }) {
                        Text(stringResource(R.string.clear))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.date_filter_year),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selected.year == null,
                    onClick = { onFilterChange(DateFilter.None) },
                    label = { Text(stringResource(R.string.date_filter_all_time)) },
                    colors = selectedChipColors()
                )
                years.forEach { year ->
                    FilterChip(
                        selected = selected.year == year,
                        onClick = { onFilterChange(selected.withYear(year)) },
                        label = { Text(year.toString()) },
                        colors = selectedChipColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Months only make sense once a year is chosen.
            val monthsEnabled = selected.year != null
            Text(
                text = stringResource(R.string.date_filter_month),
                style = MaterialTheme.typography.titleMedium,
                color = if (monthsEnabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    enabled = monthsEnabled,
                    selected = monthsEnabled && selected.month == null,
                    onClick = { onFilterChange(selected.withMonth(null)) },
                    label = { Text(stringResource(R.string.date_filter_whole_year)) },
                    colors = selectedChipColors()
                )
                (1..12).forEach { month ->
                    FilterChip(
                        enabled = monthsEnabled,
                        selected = selected.month == month,
                        onClick = { onFilterChange(selected.withMonth(month)) },
                        label = { Text(shortMonthName(month)) },
                        colors = selectedChipColors()
                    )
                }
            }
        }
    }
}

/**
 * The button that opens [DateFilterSheet]. Shows the active period so the user can
 * see what the screen is scoped to without opening the sheet.
 */
@Composable
fun DateFilterButton(
    filter: DateFilter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        modifier = modifier,
        selected = filter.isActive,
        onClick = onClick,
        label = { Text(dateFilterLabel(filter)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = selectedChipColors()
    )
}

@Composable
private fun selectedChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
)

/** Human-readable description of [filter], e.g. "All time", "2025", "March 2025". */
@Composable
fun dateFilterLabel(filter: DateFilter): String {
    val year = filter.year ?: return stringResource(R.string.date_filter_all_time)
    val month = filter.month ?: return year.toString()
    return "${fullMonthName(month)} $year"
}

/** Locale-aware standalone month name, e.g. "Μάρτιος" / "March". */
private fun fullMonthName(month: Int, locale: Locale = Locale.getDefault()): String =
    Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

/** Locale-aware short month name used on the compact month chips. */
private fun shortMonthName(month: Int, locale: Locale = Locale.getDefault()): String =
    Month.of(month).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Date filter button - all time", showBackground = true)
@Composable
private fun PreviewDateFilterButtonAllTime() {
    CarTrackingAppTheme(darkTheme = false) {
        DateFilterButton(filter = DateFilter.None, onClick = {})
    }
}

@Preview(name = "Date filter button - month", showBackground = true)
@Composable
private fun PreviewDateFilterButtonMonth() {
    CarTrackingAppTheme(darkTheme = false) {
        DateFilterButton(filter = DateFilter(year = 2025, month = 3), onClick = {})
    }
}
