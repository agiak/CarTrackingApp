package com.agcoding.cartrackingapp.presentation.carcomparison

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.CarComparisonResult
import com.agcoding.cartrackingapp.presentation.components.GroupedDivider
import com.agcoding.cartrackingapp.presentation.components.GroupedListContainer
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatMoneyPer
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * Two cars, head to head.
 *
 * This was three "metric cards" and two "detailed stats" cards, each one
 * repeating both car names and each one a separate rounded box. Five cards to
 * answer a question with two columns in it.
 *
 * It is now one table. The car names are said once, at the top, and every
 * metric is a row with a figure under each name — which is the shape the
 * comparison already has, so the layout stops working against it. The better
 * figure in each row is green; nothing is marked red, because in a two-car
 * comparison "worse" is just "the other one", and colouring both ends makes
 * the table shout at a difference that may be 2%.
 *
 * The sentences the app can say about the difference go underneath, where they
 * read as a conclusion drawn from the table rather than as three more boxes.
 */
@Composable
fun TwoCarsComparisonContent(
    result: CarComparisonResult,
    viewModel: CarComparisonViewModel
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val useSplitView = configuration.screenWidthDp >= 600 || isLandscape
    val dimens = LocalAppDimens.current

    val availableCars by viewModel.availableCars.collectAsState()
    val selectedCar1 by viewModel.selectedCar1.collectAsState()
    val selectedCar2 by viewModel.selectedCar2.collectAsState()

    val selector: @Composable () -> Unit = {
        CarSelectorRow(
            availableCars = availableCars,
            selectedCar1 = selectedCar1,
            selectedCar2 = selectedCar2,
            onCar1Selected = viewModel::selectCar1,
            onCar2Selected = viewModel::selectCar2
        )
    }

    if (useSplitView) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimens.spacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                selector()
                Spacer(modifier = Modifier.height(dimens.spacing.sectionTop))
                HeadlineTable(result)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                InsightsCard(result)
                Spacer(modifier = Modifier.height(dimens.spacing.sectionTop))
                TotalsTable(result)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = dimens.spacing.screenHorizontal,
                    end = dimens.spacing.screenHorizontal,
                    top = 4.dp,
                    bottom = 28.dp
                )
        ) {
            selector()
            Spacer(modifier = Modifier.height(dimens.spacing.sectionTop))
            HeadlineTable(result)
            Spacer(modifier = Modifier.height(dimens.spacing.sectionTop))
            InsightsCard(result)
            Spacer(modifier = Modifier.height(dimens.spacing.sectionTop))
            TotalsTable(result)
        }
    }
}

/** The three metrics the comparison is decided on. */
@Composable
private fun HeadlineTable(result: CarComparisonResult) {
    val c1 = result.car1
    val c2 = result.car2

    ComparisonTable(
        title = stringResource(R.string.car_comparison_summary),
        car1Name = c1.carName,
        car2Name = c2.carName,
        rows = listOf(
            comparisonRow(
                label = stringResource(R.string.car_comparison_cost_per_km),
                v1 = c1.costPerKm,
                v2 = c2.costPerKm,
                format = { it.formatMoneyPer("km", decimals = 3) }
            ),
            comparisonRow(
                label = stringResource(R.string.car_comparison_avg_consumption),
                v1 = c1.avgConsumption,
                v2 = c2.avgConsumption,
                format = { "${it.formatNumber(1)} L/100" }
            ),
            comparisonRow(
                label = stringResource(R.string.car_comparison_maintenance_per_year),
                v1 = c1.maintenancePerYear,
                v2 = c2.maintenancePerYear,
                format = { it.formatMoney(decimals = 0) }
            )
        )
    )
}

/**
 * What each car has actually recorded.
 *
 * Nothing here is better or worse — a car with more kilometres is not winning
 * — so no row is tinted. It is context for the table above: a verdict drawn
 * from 4.000 km deserves less trust than one drawn from 40.000, and this is
 * where that shows.
 */
@Composable
private fun TotalsTable(result: CarComparisonResult) {
    val c1 = result.car1
    val c2 = result.car2

    ComparisonTable(
        title = stringResource(R.string.statistics),
        car1Name = c1.carName,
        car2Name = c2.carName,
        rows = listOf(
            ComparisonRow(
                label = stringResource(R.string.car_comparison_total_expenses),
                value1 = c1.totalExpenses.formatMoney(0),
                value2 = c2.totalExpenses.formatMoney(0)
            ),
            ComparisonRow(
                label = stringResource(R.string.car_comparison_total_kilometers),
                value1 = "${c1.totalKilometers.formatNumber(0)} km",
                value2 = "${c2.totalKilometers.formatNumber(0)} km"
            ),
            ComparisonRow(
                label = stringResource(R.string.car_comparison_total_liters),
                value1 = "${c1.totalLiters.formatNumber(0)} L",
                value2 = "${c2.totalLiters.formatNumber(0)} L"
            ),
            ComparisonRow(
                label = stringResource(R.string.car_comparison_years_active),
                value1 = c1.yearsActive.formatNumber(1),
                value2 = c2.yearsActive.formatNumber(1)
            )
        )
    )
}

/** One line of the table. [better] is 1, 2, or null when neither side wins. */
private data class ComparisonRow(
    val label: String,
    val value1: String,
    val value2: String,
    val better: Int? = null
)

/**
 * Builds a row and works out which side wins, for the metrics where lower is
 * better — which, on this screen, is all of them: every figure here is a cost
 * or a consumption.
 */
private fun comparisonRow(
    label: String,
    v1: Double?,
    v2: Double?,
    format: (Double) -> String
) = ComparisonRow(
    label = label,
    value1 = v1?.let(format) ?: "—",
    value2 = v2?.let(format) ?: "—",
    better = when {
        v1 == null || v2 == null -> null
        v1 < v2 -> 1
        v2 < v1 -> 2
        else -> null
    }
)

@Composable
private fun ComparisonTable(
    title: String,
    car1Name: String,
    car2Name: String,
    rows: List<ComparisonRow>
) {
    val scheme = LocalAppColorScheme.current

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(title)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
            Spacer(modifier = Modifier.weight(1.1f))
            TableHeaderCell(name = car1Name, color = scheme.actionPrimary)
            TableHeaderCell(name = car2Name, color = scheme.contentPrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))

        GroupedListContainer {
            rows.forEachIndexed { index, row ->
                if (index > 0) GroupedDivider(startInset = false)
                TableRow(row)
            }
        }
    }
}

@Composable
private fun RowScope.TableHeaderCell(name: String, color: Color) {
    Text(
        text = name,
        modifier = Modifier.weight(1f),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        textAlign = TextAlign.End,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TableRow(row: ComparisonRow) {
    val scheme = LocalAppColorScheme.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = row.label,
            modifier = Modifier.weight(1.1f),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.contentSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = row.value1,
            modifier = Modifier.weight(1f),
            style = AppNumbers.compact,
            color = if (row.better == 1) scheme.statusSuccess else scheme.contentPrimary,
            textAlign = TextAlign.End,
            maxLines = 1
        )
        Text(
            text = row.value2,
            modifier = Modifier.weight(1f),
            style = AppNumbers.compact,
            color = if (row.better == 2) scheme.statusSuccess else scheme.contentPrimary,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

/**
 * What the table adds up to, in sentences.
 *
 * The verdict is the heading and the three differences are the working. If
 * neither car wins the app says so rather than picking one at random.
 */
@Composable
private fun InsightsCard(result: CarComparisonResult) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    val winner = when (result.overallWinner) {
        result.car1.carId -> result.car1
        result.car2.carId -> result.car2
        else -> null
    }

    fun nameOf(carId: Long): String =
        if (carId == result.car1.carId) result.car1.carName else result.car2.carName

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.card))
            .background(scheme.actionSecondary)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(dimens.size.iconTile)
                    .clip(RoundedCornerShape(dimens.radius.iconTile))
                    .background(scheme.statusSuccess.copy(alpha = 0.18f))
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = scheme.statusSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (winner != null) {
                    stringResource(R.string.car_comparison_overall_winner, winner.carName)
                } else {
                    stringResource(R.string.car_comparison_equal_performance)
                },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary
            )
        }

        result.costPerKmDifference?.let { diff ->
            InsightLine(
                text = stringResource(
                    R.string.car_comparison_cost_per_km_insight,
                    nameOf(diff.higherCarId),
                    formatPercentage(diff.percentageDifference),
                    nameOf(diff.lowerCarId)
                )
            )
        }
        result.consumptionDifference?.let { diff ->
            InsightLine(
                text = stringResource(
                    R.string.car_comparison_consumption_insight,
                    nameOf(diff.lowerCarId),
                    formatPercentage(diff.percentageDifference),
                    nameOf(diff.higherCarId)
                )
            )
        }
        result.maintenanceDifference?.let { diff ->
            InsightLine(
                text = stringResource(
                    R.string.car_comparison_maintenance_insight,
                    nameOf(diff.higherCarId),
                    diff.absoluteDifference.formatMoney(0),
                    nameOf(diff.lowerCarId)
                )
            )
        }
    }
}

@Composable
private fun InsightLine(text: String) {
    val scheme = LocalAppColorScheme.current
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(scheme.contentDisabled)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.contentSecondary
        )
    }
}

/**
 * The two cars being compared, with the exchange icon between them.
 *
 * Both boxes are the same shape and the same surface; the only difference is
 * the dot, which carries the colour of that car's column in the tables below.
 * The old pair used primaryContainer and secondaryContainer, which made the
 * first car look selected and the second look disabled.
 */
@Composable
private fun CarSelectorRow(
    availableCars: List<Car>,
    selectedCar1: Car?,
    selectedCar2: Car?,
    onCar1Selected: (Car) -> Unit,
    onCar2Selected: (Car) -> Unit
) {
    val scheme = LocalAppColorScheme.current
    var showCar1Selector by remember { mutableStateOf(false) }
    var showCar2Selector by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            CarSelectorBox(
                label = stringResource(R.string.car_comparison_car_1),
                carName = selectedCar1?.name,
                dotColor = scheme.actionPrimary,
                onClick = { showCar1Selector = true }
            )
            DropdownMenu(
                expanded = showCar1Selector,
                onDismissRequest = { showCar1Selector = false }
            ) {
                availableCars.filter { it.id != selectedCar2?.id }.forEach { car ->
                    DropdownMenuItem(
                        text = { Text(car.name) },
                        onClick = {
                            onCar1Selected(car)
                            showCar1Selector = false
                        }
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
            contentDescription = null,
            tint = scheme.contentDisabled,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(22.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            CarSelectorBox(
                label = stringResource(R.string.car_comparison_car_2),
                carName = selectedCar2?.name,
                dotColor = scheme.contentPrimary,
                onClick = { showCar2Selector = true }
            )
            DropdownMenu(
                expanded = showCar2Selector,
                onDismissRequest = { showCar2Selector = false }
            ) {
                availableCars.filter { it.id != selectedCar1?.id }.forEach { car ->
                    DropdownMenuItem(
                        text = { Text(car.name) },
                        onClick = {
                            onCar2Selected(car)
                            showCar2Selector = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CarSelectorBox(
    label: String,
    carName: String?,
    dotColor: Color,
    onClick: () -> Unit
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.tile))
            .background(scheme.backgroundCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = scheme.contentSecondary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = carName ?: stringResource(R.string.car_comparison_select_car),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = if (carName != null) scheme.contentPrimary else scheme.contentDisabled,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = scheme.contentSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Two cars - headline table", showBackground = true, widthDp = 400)
@Composable
private fun PreviewHeadlineTable() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(18.dp)) {
            HeadlineTable(result = previewTwoCarResult)
        }
    }
}

@Preview(name = "Two cars - totals", showBackground = true, widthDp = 400)
@Composable
private fun PreviewTotalsTable() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(18.dp)) {
            TotalsTable(result = previewTwoCarResult)
        }
    }
}

@Preview(name = "Two cars - insights", showBackground = true, widthDp = 400)
@Composable
private fun PreviewInsightsCard() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(18.dp)) {
            InsightsCard(result = previewTwoCarResult)
        }
    }
}

@Preview(name = "Two cars - selector", showBackground = true, widthDp = 400)
@Composable
private fun PreviewCarSelectorRow() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(18.dp)) {
            CarSelectorRow(
                availableCars = emptyList(),
                selectedCar1 = Car(
                    id = 1,
                    name = previewCorolla.carName,
                    licensePlate = "ABC-1234",
                    currentOdometer = 120_000.0,
                    initialOdometer = 0.0
                ),
                selectedCar2 = null,
                onCar1Selected = {},
                onCar2Selected = {}
            )
        }
    }
}
