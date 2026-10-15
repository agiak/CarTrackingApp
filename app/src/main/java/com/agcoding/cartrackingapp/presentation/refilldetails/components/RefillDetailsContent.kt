package com.agcoding.cartrackingapp.presentation.refilldetails.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Location
import com.agcoding.cartrackingapp.domain.usecase.refill.RefillDetails
import com.agcoding.cartrackingapp.presentation.components.GroupedDivider
import com.agcoding.cartrackingapp.presentation.components.GroupedListContainer
import com.agcoding.cartrackingapp.presentation.components.Kpi
import com.agcoding.cartrackingapp.presentation.components.KpiGrid
import com.agcoding.cartrackingapp.presentation.components.LicensePlateBadge
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryFuel
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatMoneyPer
import com.agcoding.cartrackingapp.util.formatNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * One refill, in full.
 *
 * The screen used to open on a "Refill Details" heading and a coloured block
 * saying "Total amount paid" — a title, then a label, then the number. It now
 * opens on the car the refill belongs to and then the number itself: what was
 * paid, when, and the one judgement the app can make about it, which is how
 * this tank's consumption compares with the car's average.
 *
 * That verdict used to be a tile among four, reading "7,3 L/100km" with no
 * indication of whether that is good. In the hero, with the car's own average
 * behind it, it becomes the answer to the question somebody opens a refill to
 * ask.
 *
 * Below it the four figures that are just figures — volume, price per litre,
 * distance, odometer — are tiles; the three things that are text are a grouped
 * list; and the three derived numbers are a KPI row. Each group is one card
 * instead of a card per item.
 */
@Composable
fun RefillDetailsContent(
    details: RefillDetails,
    addressString: String?,
    context: Context,
    onLocationClick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
    tripSection: (@Composable () -> Unit)? = null
) {
    val dimens = LocalAppDimens.current
    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
    val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
    val useSplitView = isTablet || isLandscape

    if (useSplitView) {
        Row(
            modifier = modifier.padding(horizontal = dimens.spacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                CarRow(details.car)
                Spacer(Modifier.height(14.dp))
                RefillHeroCard(details)
                Spacer(Modifier.height(10.dp))
                tripSection?.invoke()
                Spacer(Modifier.height(10.dp))
                MetricTiles(details.refill)
            }
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                DetailsSection(details, addressString, onLocationClick)
                Spacer(Modifier.height(dimens.spacing.sectionTop))
                CostAnalysisSection(details.refill)
                Spacer(Modifier.height(24.dp))
            }
        }
    } else {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.spacing.screenHorizontal)
        ) {
            CarRow(details.car)
            Spacer(Modifier.height(14.dp))
            RefillHeroCard(details)
            Spacer(Modifier.height(10.dp))
            if (tripSection != null) {
                tripSection()
                Spacer(Modifier.height(10.dp))
            }
            MetricTiles(details.refill)
            Spacer(Modifier.height(dimens.spacing.sectionTop))
            DetailsSection(details, addressString, onLocationClick)
            Spacer(Modifier.height(dimens.spacing.sectionTop))
            CostAnalysisSection(details.refill)
            Spacer(Modifier.height(28.dp))
        }
    }
}

/** Which car this belongs to — the one thing the app bar no longer says. */
@Composable
private fun CarRow(car: Car?) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(dimens.size.iconTile)
                .clip(RoundedCornerShape(dimens.radius.iconTile))
                .background(scheme.categoryFuel.copy(alpha = 0.16f))
        ) {
            Icon(
                imageVector = Icons.Default.LocalGasStation,
                contentDescription = null,
                tint = scheme.categoryFuel,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = car?.name ?: stringResource(R.string.unknown_car),
                style = MaterialTheme.typography.titleMedium,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            car?.licensePlate?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(5.dp))
                LicensePlateBadge(plate = it)
            }
        }
    }
}

/**
 * What it cost, and whether it was a good tank.
 *
 * The consumption sits under a divider rather than beside the amount: it is a
 * separate claim about the same refill, and reading it as part of the price
 * would be wrong.
 */
@Composable
private fun RefillHeroCard(details: RefillDetails) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val refill = details.refill

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.hero))
            .background(scheme.backgroundCard)
            .padding(20.dp)
    ) {
        SectionLabel(stringResource(R.string.refill_details_fuel_refill))
        Spacer(Modifier.height(8.dp))
        HeroAmount(refill.amountPaid)
        Spacer(Modifier.height(6.dp))
        Text(
            text = formatDateTime(refill.timestamp),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.contentSecondary
        )

        if (refill.fuelConsumption > 0.0) {
            Spacer(Modifier.height(16.dp))
            GroupedDivider(startInset = false)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.metric_consumption),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.contentSecondary
                )
                Text(
                    text = "${refill.fuelConsumption.formatNumber(1)} L/100km",
                    style = AppNumbers.value,
                    color = scheme.categoryFuel,
                    maxLines = 1
                )
            }
            ConsumptionVerdict(
                consumption = refill.fuelConsumption,
                average = details.car?.averageConsumption ?: 0.0
            )
        }
    }
}

/**
 * The amount, with its cents set smaller.
 *
 * Splitting on the locale's decimal separator rather than reformatting: the
 * app's formatter already decided how this number is written, and taking it
 * apart afterwards keeps the two halves in agreement.
 */
@Composable
private fun HeroAmount(amount: Double) {
    val scheme = LocalAppColorScheme.current
    val text = amount.formatMoney()
    val cut = text.indexOfFirst { it == ',' || it == '.' }

    Row(verticalAlignment = Alignment.Bottom) {
        if (cut > 0) {
            Text(
                text = text.substring(0, cut),
                style = AppNumbers.hero,
                color = scheme.contentPrimary,
                maxLines = 1
            )
            Text(
                text = text.substring(cut),
                style = AppNumbers.heroFraction,
                color = scheme.contentSecondary,
                modifier = Modifier.padding(bottom = 4.dp),
                maxLines = 1
            )
        } else {
            Text(
                text = text,
                style = AppNumbers.hero,
                color = scheme.contentPrimary,
                maxLines = 1
            )
        }
    }
}

/**
 * This tank against the car's average.
 *
 * Nothing is drawn when there is no average to compare with — a car with one
 * refill in it has no baseline, and "0% under average" would be a sentence the
 * data does not support. Within half a litre per hundred kilometres the pill
 * says so plainly instead of reporting a 2% swing as news.
 */
@Composable
private fun ConsumptionVerdict(consumption: Double, average: Double) {
    val scheme = LocalAppColorScheme.current
    if (average <= 0.0 || consumption <= 0.0) return

    val delta = consumption - average
    val onPar = abs(delta) < 0.5
    val better = delta < 0
    val percent = abs(delta / average * 100).roundToInt()

    val tint = when {
        onPar -> scheme.contentSecondary
        better -> scheme.statusSuccess
        else -> scheme.categoryService
    }
    val icon = when {
        onPar -> Icons.AutoMirrored.Filled.TrendingFlat
        better -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingUp
    }
    val label = when {
        onPar -> stringResource(R.string.refill_details_consumption_on_par)
        better -> stringResource(R.string.refill_details_consumption_under, percent.toString())
        else -> stringResource(R.string.refill_details_consumption_over, percent.toString())
    }

    Spacer(Modifier.height(10.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(LocalAppDimens.current.radius.chip))
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            maxLines = 1
        )
    }
}

/** The four measured figures, two across. */
@Composable
private fun MetricTiles(refill: FuelRefill) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                icon = Icons.Default.WaterDrop,
                label = stringResource(R.string.metric_fuel_volume),
                value = refill.litersAdded.formatNumber(1),
                unit = "L",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                icon = Icons.Default.EuroSymbol,
                label = stringResource(R.string.metric_price_per_liter),
                value = refill.pricePerLiter.formatNumber(3),
                unit = "€",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                icon = Icons.Default.Route,
                label = stringResource(R.string.metric_trip_distance),
                value = refill.tripDistance.formatNumber(0),
                unit = "km",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                icon = Icons.Default.Speed,
                label = stringResource(R.string.odometer),
                value = refill.odometerReading.formatNumber(0),
                unit = "km",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimens.radius.tile))
            .background(scheme.backgroundCard)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = scheme.contentSecondary,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.contentSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = AppNumbers.tile,
                color = scheme.contentPrimary,
                maxLines = 1
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.contentSecondary,
                modifier = Modifier.padding(bottom = 2.dp),
                maxLines = 1
            )
        }
    }
}

/** Date, place and notes: the three things about a refill that are sentences. */
@Composable
private fun DetailsSection(
    details: RefillDetails,
    addressString: String?,
    onLocationClick: (Double, Double) -> Unit
) {
    val refill = details.refill

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(stringResource(R.string.additional_information))
        Spacer(Modifier.height(10.dp))
        GroupedListContainer {
            DetailRow(
                icon = Icons.Default.CalendarToday,
                label = stringResource(R.string.date_time),
                value = formatDateTime(refill.timestamp)
            )
            refill.location?.let { location ->
                GroupedDivider()
                DetailRow(
                    icon = Icons.Default.LocationOn,
                    label = stringResource(R.string.location_label),
                    value = addressString ?: stringResource(
                        R.string.location_lat_lng_format,
                        location.latitude,
                        location.longitude
                    ),
                    external = true,
                    onClick = { onLocationClick(location.latitude, location.longitude) }
                )
            }
            refill.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                GroupedDivider()
                DetailRow(
                    icon = Icons.Default.Notes,
                    label = stringResource(R.string.notes),
                    value = notes
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    external: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(dimens.size.iconTile)
                .clip(RoundedCornerShape(dimens.radius.iconTile))
                .background(scheme.backgroundSecondary)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.contentSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.contentSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.contentPrimary
            )
        }
        if (external) {
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = scheme.contentDisabled,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

/** What the refill works out to, once divided through. */
@Composable
private fun CostAnalysisSection(refill: FuelRefill) {
    val costPerKm = if (refill.tripDistance > 0) refill.amountPaid / refill.tripDistance else 0.0
    val efficiency = if (refill.litersAdded > 0) refill.tripDistance / refill.litersAdded else 0.0

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(stringResource(R.string.cost_analysis))
        Spacer(Modifier.height(10.dp))
        KpiGrid(
            columns = 3,
            items = listOf(
                Kpi(
                    label = stringResource(R.string.cost_per_kilometer_label),
                    value = costPerKm.formatMoneyPer("km", 3)
                ),
                Kpi(
                    label = stringResource(R.string.fuel_efficiency_label),
                    value = efficiency.formatNumber(1),
                    unit = "km/L"
                ),
                Kpi(
                    label = stringResource(R.string.total_liters_label),
                    value = refill.litersAdded.formatNumber(2),
                    unit = "L"
                )
            )
        )
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE, d MMM yyyy · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// ============================================
// Preview Composables
// ============================================

private val previewRefill = FuelRefill(
    id = 1,
    carId = 1,
    timestamp = 1_760_000_000_000L,
    amountPaid = 70.43,
    litersAdded = 39.3,
    pricePerLiter = 1.792,
    tripDistance = 641.0,
    odometerReading = 119_625.0,
    fuelConsumption = 6.13,
    location = Location(latitude = 37.9838, longitude = 23.7275),
    notes = "Full tank before the weekend trip."
)

private val previewCar = Car(
    id = 1,
    name = "Toyota Corolla",
    licensePlate = "ABC-1234",
    initialOdometer = 0.0,
    currentOdometer = 119_625.0,
    averageConsumption = 8.7
)

@Preview(name = "Refill details — dark", showBackground = true, widthDp = 390, heightDp = 1000)
@Composable
private fun PreviewRefillDetailsDark() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillDetailsContent(
            details = RefillDetails(refill = previewRefill, car = previewCar),
            addressString = "Shell · Kifisias Ave 212, Marousi",
            context = androidx.compose.ui.platform.LocalContext.current,
            onLocationClick = { _, _ -> }
        )
    }
}

@Preview(name = "Refill details — light", showBackground = true, widthDp = 390, heightDp = 1000)
@Composable
private fun PreviewRefillDetailsLight() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillDetailsContent(
            details = RefillDetails(refill = previewRefill, car = previewCar),
            addressString = "Shell · Kifisias Ave 212, Marousi",
            context = androidx.compose.ui.platform.LocalContext.current,
            onLocationClick = { _, _ -> }
        )
    }
}

/** No location, no notes, and no average to judge the tank against. */
@Preview(name = "Refill details — bare", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun PreviewRefillDetailsBare() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillDetailsContent(
            details = RefillDetails(
                refill = previewRefill.copy(location = null, notes = null),
                car = previewCar.copy(averageConsumption = 0.0)
            ),
            addressString = null,
            context = androidx.compose.ui.platform.LocalContext.current,
            onLocationClick = { _, _ -> }
        )
    }
}
