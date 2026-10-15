package com.agcoding.cartrackingapp.presentation.cardetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.CarStatistics
import com.agcoding.cartrackingapp.presentation.components.LicensePlateBadge
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.util.formatNumber
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/**
 * Who this car is, and the one number that belongs to it alone.
 *
 * The old card spent its top half on an 80dp car icon — the same icon on every
 * card, for a screen you reached by tapping that car's name, so it identified
 * nothing. The space goes to the name at full size, the plate, and the
 * odometer as the hero figure.
 *
 * Underneath, the paperwork: KTEO, insurance, emissions, road tax, tyres. These
 * used to live in a separate card that announced "and 3 more fields" without
 * saying which, so the only way to learn whether the KTEO was due was to open
 * it. As chips they are all legible at once, and absent ones simply are not
 * there — which is itself the answer to "have I filled this in?".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CarHeaderCard(statistics: CarStatistics) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val car = statistics.car
    val chips = car.paperworkChips()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = car.name,
                style = MaterialTheme.typography.displaySmall,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (car.isDefault) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.default_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.actionPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(scheme.actionSecondary)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        LicensePlateBadge(plate = car.licensePlate)
        Spacer(Modifier.height(14.dp))

        StyledCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.current_odometer),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.contentSecondary,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = car.currentOdometer.toInt().toLong().formatNumber(),
                        style = AppNumbers.hero,
                        color = scheme.contentPrimary,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.contentSecondary,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }

                if (chips.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(
                        thickness = dimens.size.divider,
                        color = scheme.borderDefault,
                    )
                    Spacer(Modifier.height(14.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        chips.forEach { chip -> PaperworkChip(chip.label, chip.value) }
                    }
                }
            }
        }
    }
}

/** One fact about the car that is not a measurement: a date, a sum, a size. */
private data class PaperworkEntry(val label: String, val value: String)

@Composable
private fun PaperworkChip(label: String, value: String) {
    val scheme = LocalAppColorScheme.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(scheme.backgroundSecondary)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = scheme.contentSecondary,
            maxLines = 1,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = value,
            style = AppNumbers.tag,
            color = scheme.contentPrimary,
            maxLines = 1,
        )
    }
}

/**
 * The paperwork worth showing, in the order it tends to matter.
 *
 * Fields the user has never filled in are left out rather than shown empty: a
 * chip reading "KTEO —" claims the app knows something about the KTEO.
 */
@Composable
private fun Car.paperworkChips(): List<PaperworkEntry> {
    val entries = mutableListOf<PaperworkEntry>()
    kteoExpirationDate?.let {
        entries += PaperworkEntry(stringResource(R.string.car_chip_kteo), it.asMonthYear())
    }
    insuranceExpirationDate?.let {
        entries += PaperworkEntry(stringResource(R.string.car_chip_insurance), it.asMonthYear())
    }
    emissionsCardExpirationDate?.let {
        entries += PaperworkEntry(stringResource(R.string.car_chip_emissions), it.asMonthYear())
    }
    roadTaxAmount?.let {
        entries += PaperworkEntry(
            stringResource(R.string.car_chip_road_tax),
            stringResource(R.string.car_card_currency_eur_format, it.formatNumber(0)),
        )
    }
    val tyres = listOfNotNull(
        tireBrand?.takeIf { it.isNotBlank() },
        tireDimensions?.takeIf { it.isNotBlank() } ?: tyreSize?.takeIf { it.isNotBlank() },
    ).joinToString(" ")
    if (tyres.isNotBlank()) {
        entries += PaperworkEntry(stringResource(R.string.car_chip_tyres), tyres)
    }
    return entries
}

/** "03/27" — the month is the whole point of these dates; the day never is. */
private fun Long.asMonthYear(zone: ZoneId = ZoneId.systemDefault()): String {
    val date = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
    return "%02d/%02d".format(Locale.getDefault(), date.monthValue, date.year % 100)
}

// ── previews ─────────────────────────────────────────────────────────────

private fun previewStatistics(car: Car) = CarStatistics(
    car = car,
    totalRefills = 501,
    averageConsumption = 6.5,
    totalCost = 15432.50,
    totalDistance = 125678.0,
    costPerKilometer = 0.19,
    serviceExpensesCost = 1200.0,
    serviceExpenseCount = 8,
    otherExpensesCost = 300.0,
    otherExpenseCount = 5,
    averagePricePerLiter = 1.65,
    totalLiters = 5200.0,
    recentRefills = emptyList(),
)

@Preview(name = "Car header — full", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCarHeaderCard() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            CarHeaderCard(
                previewStatistics(
                    Car(
                        id = 1, name = "Toyota Corolla", licensePlate = "ΙΖΧ-4821",
                        currentOdometer = 145678.0, initialOdometer = 20000.0, isDefault = true,
                        kteoExpirationDate = 1800000000000L,
                        insuranceExpirationDate = 1790000000000L,
                        roadTaxAmount = 120.0,
                        tireDimensions = "205/55 R16",
                    )
                )
            )
        }
    }
}

@Preview(name = "Car header — nothing filled in", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCarHeaderCardBare() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(Modifier.padding(18.dp)) {
            CarHeaderCard(
                previewStatistics(
                    Car(
                        id = 2, name = "Tesla Model 3", licensePlate = "",
                        currentOdometer = 5000.0, initialOdometer = 5000.0,
                    )
                )
            )
        }
    }
}
