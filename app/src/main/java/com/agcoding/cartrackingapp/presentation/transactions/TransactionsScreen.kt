package com.agcoding.cartrackingapp.presentation.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories
import com.agcoding.cartrackingapp.presentation.components.AppFilterChip
import com.agcoding.cartrackingapp.presentation.components.DateFilterSheet
import com.agcoding.cartrackingapp.presentation.components.FilterChipRow
import com.agcoding.cartrackingapp.presentation.components.HeaderIconButton
import com.agcoding.cartrackingapp.presentation.components.ScreenHeader
import com.agcoding.cartrackingapp.presentation.components.dateFilterLabel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.presentation.transactions.components.MonthSummaryCard
import com.agcoding.cartrackingapp.presentation.transactions.components.TransactionCard
import com.agcoding.cartrackingapp.presentation.transactions.model.Transaction
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionType
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryFuel
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryOther
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService
import com.agcoding.cartrackingapp.util.formatNumber
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

@Composable
fun TransactionsScreen(
    onTransactionClick: (Transaction) -> Unit,
    onAddRefill: (carId: Long) -> Unit = {},
    onAddExpense: (carId: Long) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val cars by viewModel.cars.collectAsStateWithLifecycle()
    val hasAnyTransactions by viewModel.hasAnyTransactions.collectAsStateWithLifecycle()
    val defaultCarId by viewModel.defaultCarId.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showDateSheet by remember { mutableStateOf(false) }

    TransactionsContent(
        transactions = transactions,
        filter = filter,
        cars = cars,
        hasAnyTransactions = hasAnyTransactions,
        defaultCarId = defaultCarId,
        onTransactionClick = onTransactionClick,
        onAddRefill = onAddRefill,
        onAddExpense = onAddExpense,
        onOpenDateSheet = { showDateSheet = true },
        onOpenSortSheet = { showSortSheet = true },
        onOpenFilterSheet = { showFilterSheet = true },
        onSelectAllCars = viewModel::clearCarFilter,
        onToggleCar = viewModel::toggleCarSelection,
        onDateFilterChange = viewModel::setDateFilter,
    )

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = sortOption,
            onSortSelected = {
                viewModel.setSortOption(it)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filter = filter,
            cars = cars,
            onToggleRefills = viewModel::toggleRefillFilter,
            onToggleExpenses = viewModel::toggleExpenseFilter,
            onToggleCarSelection = viewModel::toggleCarSelection,
            onClearCarFilter = viewModel::clearCarFilter,
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showDateSheet) {
        DateFilterSheet(
            selected = filter.dateFilter,
            availableYears = availableYears,
            onFilterChange = viewModel::setDateFilter,
            onDismiss = { showDateSheet = false }
        )
    }
}

/**
 * The screen, with no ViewModel in it.
 *
 * The three filter surfaces that used to be cards down the left of a tablet
 * layout are gone. Everything they held is now either a chip on this screen or
 * a sheet reachable from the header, on every screen size — which is both less
 * code and the same features in the same place whatever you are holding.
 */
@Composable
private fun TransactionsContent(
    transactions: List<TransactionWithData>,
    filter: TransactionFilter,
    cars: List<Car>,
    hasAnyTransactions: Boolean,
    defaultCarId: Long?,
    onTransactionClick: (Transaction) -> Unit,
    onAddRefill: (carId: Long) -> Unit,
    onAddExpense: (carId: Long) -> Unit,
    onOpenDateSheet: () -> Unit,
    onOpenSortSheet: () -> Unit,
    onOpenFilterSheet: () -> Unit,
    onSelectAllCars: () -> Unit,
    onToggleCar: (Long) -> Unit,
    onDateFilterChange: (DateFilter) -> Unit,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val margin = dimens.spacing.screenHorizontal

    // The card's figures are summed from the list below it rather than queried
    // separately, so the headline total can never disagree with the records
    // the user can see — whatever combination of filters produced them.
    val fuelTotal = transactions.filter { it.transaction.type == TransactionType.REFILL }
        .sumOf { it.transaction.amount }
    val serviceTotal = transactions
        .filterIsInstance<TransactionWithData.ExpenseTransaction>()
        .filter { ExpenseCategories.isServiceCategory(it.expense.category) }
        .sumOf { it.transaction.amount }
    val otherTotal = transactions
        .filterIsInstance<TransactionWithData.ExpenseTransaction>()
        .filterNot { ExpenseCategories.isServiceCategory(it.expense.category) }
        .sumOf { it.transaction.amount }
    val grandTotal = fuelTotal + serviceTotal + otherTotal

    val shownMonth = filter.dateFilter.singleMonthOrNull()
    // dateFilterLabel already answers "All time" for an inactive filter, so a
    // month name is the only case it cannot phrase on its own.
    val periodLabel = shownMonth?.displayName() ?: dateFilterLabel(filter.dateFilter)

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    ScreenHeader(
                        title = stringResource(R.string.nav_transactions),
                        actions = {
                            HeaderIconButton(
                                icon = Icons.Default.CalendarMonth,
                                contentDescription = stringResource(R.string.date_filter_title),
                                onClick = onOpenDateSheet,
                                showDot = filter.dateFilter.isActive,
                            )
                            HeaderIconButton(
                                icon = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.sort),
                                onClick = onOpenSortSheet,
                            )
                            HeaderIconButton(
                                icon = Icons.Default.FilterList,
                                contentDescription = stringResource(R.string.filter),
                                onClick = onOpenFilterSheet,
                                showDot = !filter.showRefills || !filter.showExpenses ||
                                    filter.selectedCarIds.isNotEmpty(),
                            )
                        },
                    )
                }

                item {
                    MonthSummaryCard(
                        periodLabel = periodLabel,
                        total = stringResource(
                            R.string.car_card_currency_eur_format,
                            grandTotal.formatNumber(2),
                        ),
                        fuel = fuelTotal,
                        service = serviceTotal,
                        other = otherTotal,
                        fuelLabel = legend(R.string.fuel, fuelTotal),
                        serviceLabel = legend(R.string.service, serviceTotal),
                        otherLabel = legend(R.string.other, otherTotal),
                        onPrevious = shownMonth?.let {
                            { onDateFilterChange(it.minusMonths(1).toFilter()) }
                        },
                        onNext = shownMonth?.let {
                            { onDateFilterChange(it.plusMonths(1).toFilter()) }
                        },
                        nextEnabled = shownMonth != null && shownMonth < YearMonth.now(),
                        modifier = Modifier.padding(horizontal = margin),
                    )
                }

                if (cars.size > 1) {
                    item {
                        FilterChipRow {
                            AppFilterChip(
                                label = stringResource(R.string.all_cars),
                                selected = filter.selectedCarIds.isEmpty(),
                                onClick = onSelectAllCars,
                            )
                            cars.forEach { car ->
                                AppFilterChip(
                                    label = car.name,
                                    selected = car.id in filter.selectedCarIds,
                                    onClick = { onToggleCar(car.id) },
                                )
                            }
                        }
                    }
                }

                if (transactions.isEmpty()) {
                    item {
                        EmptyTransactions(
                            hasAnyTransactions = hasAnyTransactions,
                            defaultCarId = defaultCarId,
                            onAddRefill = onAddRefill,
                            onAddExpense = onAddExpense,
                            modifier = Modifier.padding(horizontal = margin, vertical = 40.dp),
                        )
                    }
                } else {
                    items(
                        items = transactions,
                        key = { "${it.transaction.type.name}_${it.transaction.id}" }
                    ) { data ->
                        val accent = when {
                            data is TransactionWithData.ExpenseTransaction &&
                                ExpenseCategories.isServiceCategory(data.expense.category) ->
                                scheme.categoryService
                            data is TransactionWithData.ExpenseTransaction -> scheme.categoryOther
                            else -> scheme.categoryFuel
                        }
                        val stamp = data.transaction.timestamp.toStamp()

                        TransactionCard(
                            day = stamp.day,
                            weekday = stamp.weekday,
                            title = when (data) {
                                is TransactionWithData.RefillTransaction ->
                                    stringResource(R.string.add_refill)
                                is TransactionWithData.ExpenseTransaction -> data.expense.category
                            },
                            subtitle = "${data.transaction.carName} · ${stamp.time}",
                            amount = stringResource(
                                R.string.car_card_currency_eur_format,
                                data.transaction.amount.formatNumber(2),
                            ),
                            accent = accent,
                            tags = when (data) {
                                is TransactionWithData.RefillTransaction -> refillTags(data)
                                is TransactionWithData.ExpenseTransaction -> emptyList()
                            },
                            onClick = { onTransactionClick(data.transaction) },
                            modifier = Modifier.padding(horizontal = margin),
                        )
                    }
                }
            }
        }
    }
}

/** "Fuel 463 €" — the legend carries its own figure, so the bar needs no axis. */
@Composable
private fun legend(labelRes: Int, amount: Double): String {
    val name = stringResource(labelRes)
    val value = stringResource(R.string.car_card_currency_eur_format, amount.formatNumber(0))
    return "$name $value"
}

/** The three figures a refill is actually judged by, in the order they are read. */
@Composable
private fun refillTags(data: TransactionWithData.RefillTransaction): List<String> {
    val tags = mutableListOf<String>()
    val refill = data.refill
    if (refill.litersAdded > 0) {
        tags += stringResource(R.string.liters_format, refill.litersAdded.formatNumber(1))
    }
    if (refill.tripDistance > 0) {
        tags += stringResource(R.string.car_card_km_format, refill.tripDistance.formatNumber(0))
    }
    if (refill.fuelConsumption > 0) {
        tags += stringResource(
            R.string.consumption_l_per_100km_format,
            refill.fuelConsumption.formatNumber(1),
        )
    }
    return tags
}

@Composable
private fun EmptyTransactions(
    hasAnyTransactions: Boolean,
    defaultCarId: Long?,
    onAddRefill: (Long) -> Unit,
    onAddExpense: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = LocalAppColorScheme.current.contentDisabled,
        )
        Text(
            text = stringResource(
                if (!hasAnyTransactions) R.string.no_transactions_yet
                else R.string.no_transactions_found
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalAppColorScheme.current.contentSecondary,
        )
        // Offered only when the phone holds nothing at all. With records that
        // a filter is hiding, the useful action is changing the filter, and
        // "add a refill" would be answering a question nobody asked.
        if (!hasAnyTransactions && defaultCarId != null) {
            Spacer(Modifier.height(4.dp))
            Button(onClick = { defaultCarId.let(onAddRefill) }) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.refill_widget_label))
            }
            OutlinedButton(onClick = { defaultCarId.let(onAddExpense) }) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.expense_widget_label))
            }
        }
    }
}

// ── dates ────────────────────────────────────────────────────────────────

private data class Stamp(val day: String, val weekday: String, val time: String)

private fun Long.toStamp(zone: ZoneId = ZoneId.systemDefault()): Stamp {
    val at = Instant.ofEpochMilli(this).atZone(zone)
    val locale = Locale.getDefault()
    return Stamp(
        day = "%02d".format(locale, at.dayOfMonth),
        weekday = at.dayOfWeek
            .getDisplayName(java.time.format.TextStyle.SHORT, locale)
            .uppercase(locale),
        time = "%02d:%02d".format(locale, at.hour, at.minute),
    )
}

/**
 * The month this filter is showing, or null if it is showing anything else.
 *
 * The filter is a set of years crossed with a set of months, so most of what
 * it can express is not a month at all. Only the exact single-year,
 * single-month case can be stepped through with arrows; everything else keeps
 * its label and loses them.
 */
private fun DateFilter.singleMonthOrNull(): YearMonth? {
    val year = years.singleOrNull() ?: return null
    val month = months.singleOrNull() ?: return null
    return YearMonth.of(year, month)
}

private fun YearMonth.toFilter() = DateFilter.of(year, monthValue)

private fun YearMonth.displayName(): String {
    val locale = Locale.getDefault()
    // STANDALONE because Greek inflects month names when they follow a day
    // ("4 Οκτωβρίου"); on its own above a total it must read "Οκτώβριος".
    val name = month.getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    return "$name $year"
}

// ── previews ─────────────────────────────────────────────────────────────

@Preview(name = "Transactions — dark", showBackground = true, showSystemUi = true)
@Composable
private fun TransactionsPreviewDark() {
    CarTrackingAppTheme(darkTheme = true) {
        TransactionsContent(
            transactions = emptyList(),
            filter = TransactionFilter(
                dateFilter = DateFilter.of(LocalDate.now().year, LocalDate.now().monthValue),
            ),
            cars = listOf(
                Car(id = 1, name = "Corolla", licensePlate = "ΙΖΧ-4821",
                    currentOdometer = 1.0, initialOdometer = 0.0),
                Car(id = 2, name = "Golf", licensePlate = "ΑΒΕ-1190",
                    currentOdometer = 1.0, initialOdometer = 0.0),
            ),
            hasAnyTransactions = true,
            defaultCarId = 1L,
            onTransactionClick = {}, onAddRefill = {}, onAddExpense = {},
            onOpenDateSheet = {}, onOpenSortSheet = {}, onOpenFilterSheet = {},
            onSelectAllCars = {}, onToggleCar = {}, onDateFilterChange = {},
        )
    }
}

@Preview(name = "Transactions — nothing yet", showBackground = true, showSystemUi = true)
@Composable
private fun TransactionsPreviewEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        TransactionsContent(
            transactions = emptyList(),
            filter = TransactionFilter(),
            cars = emptyList(),
            hasAnyTransactions = false,
            defaultCarId = 1L,
            onTransactionClick = {}, onAddRefill = {}, onAddExpense = {},
            onOpenDateSheet = {}, onOpenSortSheet = {}, onOpenFilterSheet = {},
            onSelectAllCars = {}, onToggleCar = {}, onDateFilterChange = {},
        )
    }
}
