package com.agcoding.cartrackingapp.presentation.carlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.HorizontalDivider
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
import com.agcoding.cartrackingapp.presentation.components.LicensePlateBadge
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * One car, as a card: who it is, what it has cost, and the two things the user
 * came here to do.
 *
 * The old card showed four figures in a 2×2 grid, one of which was "Distance".
 * For almost every car that number equalled the odometer directly above it,
 * because a car's distance *is* its odometer unless it was bought used and the
 * initial reading was set — so the card spent a quarter of itself repeating
 * the figure next to it. It is gone, and the room went to cost per kilometre,
 * which is the one number that lets two cars be compared.
 *
 * The two actions were icon buttons in the header, sharing the top row with
 * the car's name and sized for a fingertip at 24dp. They are now labelled
 * buttons on a row of their own: adding a refill is the most common thing
 * anyone does in this app, and it was the least visible thing on the screen.
 */
@Composable
fun CarCard(
    car: Car,
    onClick: () -> Unit,
    onAddRefillClick: () -> Unit,
    onAddServiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    StyledCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(dimens.spacing.cardPadding)) {

            // ── who ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = car.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.contentPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (car.isDefault) {
                            DefaultPill()
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    LicensePlateBadge(plate = car.licensePlate)
                }

                Spacer(Modifier.width(12.dp))

                // The odometer leads the card because it is the number the
                // user reads off the dashboard and comes here to check.
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.car_card_odometer),
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.contentSecondary,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(
                            R.string.car_card_km_format,
                            car.currentOdometer.formatNumber(0),
                        ),
                        style = AppNumbers.compact,
                        color = scheme.contentPrimary,
                        maxLines = 1,
                    )
                }
            }

            CardRule()

            // ── what it costs ─────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                CarStat(
                    label = stringResource(R.string.car_card_total_cost),
                    value = stringResource(
                        R.string.car_card_currency_eur_format,
                        car.totalCost.formatNumber(2),
                    ),
                )
                CarStat(
                    label = stringResource(R.string.car_card_avg_consumption),
                    value = if (car.averageConsumption > 0) {
                        stringResource(
                            R.string.car_card_consumption_format,
                            car.averageConsumption.formatNumber(1),
                        )
                    } else {
                        stringResource(R.string.not_available)
                    },
                )
                CarStat(
                    label = stringResource(R.string.car_card_cost_per_km),
                    // Without distance this is a division by zero dressed up
                    // as a statistic, so it says nothing instead.
                    value = if (car.totalDistance > 0 && car.totalCost > 0) {
                        stringResource(
                            R.string.cost_per_km_format,
                            (car.totalCost / car.totalDistance).formatNumber(2),
                        )
                    } else {
                        stringResource(R.string.not_available)
                    },
                )
            }

            CardRule()

            // ── what to do about it ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CarAction(
                    icon = Icons.Default.LocalGasStation,
                    label = stringResource(R.string.car_card_refill),
                    contentDescription = stringResource(R.string.car_card_add_refill_cd),
                    onClick = onAddRefillClick,
                )
                CarAction(
                    icon = Icons.Default.Build,
                    label = stringResource(R.string.car_card_service),
                    contentDescription = stringResource(R.string.car_card_add_service_cd),
                    onClick = onAddServiceClick,
                )
            }
        }
    }
}

/** The hairline between the card's three bands, with its breathing room. */
@Composable
private fun CardRule() {
    val dimens = LocalAppDimens.current
    Spacer(Modifier.height(16.dp))
    HorizontalDivider(
        thickness = dimens.size.divider,
        color = LocalAppColorScheme.current.borderDefault,
    )
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun RowScope.CarStat(label: String, value: String) {
    val scheme = LocalAppColorScheme.current
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = scheme.contentSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = AppNumbers.compact,
            color = scheme.contentPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A tonal action button. Deliberately not a filled one: two filled buttons
 * side by side would both claim to be the card's primary action, and the
 * card's primary action is tapping the card itself.
 */
@Composable
private fun RowScope.CarAction(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(dimens.radius.button))
            .background(scheme.backgroundSecondary)
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = scheme.actionPrimary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = scheme.contentPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun DefaultPill() {
    val scheme = LocalAppColorScheme.current
    Text(
        text = stringResource(R.string.default_badge),
        style = MaterialTheme.typography.labelSmall,
        color = scheme.actionPrimary,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(scheme.actionSecondary)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}

// ── previews ─────────────────────────────────────────────────────────────

private fun previewCar(
    name: String = "Toyota Corolla",
    plate: String = "ΙΖΧ-4821",
    consumption: Double = 6.5,
    isDefault: Boolean = true,
) = Car(
    id = 1L,
    name = name,
    licensePlate = plate,
    currentOdometer = 145678.0,
    initialOdometer = 20000.0,
    totalCost = 15432.50,
    totalDistance = 125678.0,
    averageConsumption = consumption,
    isDefault = isDefault,
)

@Preview(name = "CarCard Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCarCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CarCard(
            car = previewCar(),
            onClick = {}, onAddRefillClick = {}, onAddServiceClick = {},
            modifier = Modifier.padding(18.dp),
        )
    }
}

@Preview(name = "CarCard Light", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCarCardLight() {
    CarTrackingAppTheme(darkTheme = false) {
        CarCard(
            car = previewCar(name = "Mercedes-Benz S-Class AMG", isDefault = false),
            onClick = {}, onAddRefillClick = {}, onAddServiceClick = {},
            modifier = Modifier.padding(18.dp),
        )
    }
}

@Preview(name = "CarCard - no data yet", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCarCardEmpty() {
    CarTrackingAppTheme(darkTheme = true) {
        CarCard(
            car = Car(
                id = 2L,
                name = "Tesla Model 3",
                licensePlate = "",
                currentOdometer = 5000.0,
                initialOdometer = 5000.0,
                totalCost = 0.0,
                totalDistance = 0.0,
                averageConsumption = 0.0,
            ),
            onClick = {}, onAddRefillClick = {}, onAddServiceClick = {},
            modifier = Modifier.padding(18.dp),
        )
    }
}
