package com.agcoding.cartrackingapp.presentation.editrefill.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.Kpi
import com.agcoding.cartrackingapp.presentation.components.KpiGrid
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.util.formatNumber
import com.agcoding.cartrackingapp.util.parseLocalizedDouble

/**
 * What the typed figures work out to, live.
 *
 * These are not inputs, so they stop looking like one: the card used to be a
 * tinted box of two sentences — "Price per liter: 1,792 €" — in the same shape
 * and position as the fields above it. They are the same two numbers the
 * refill details screen shows as figures, so they are figures here too, in the
 * KPI grid the rest of the app reads them in.
 *
 * The whole block is absent until the amount and litres are both in. There is
 * no honest value to show before that, and a pair of dashes under a label is
 * worse than nothing: it suggests the app tried and failed.
 */
@Composable
fun CalculatedValuesCard(
    amountPaid: String,
    litersAdded: String,
    tripDistance: String,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    val amount = amountPaid.parseLocalizedDouble()
    val liters = litersAdded.parseLocalizedDouble()
    val distance = tripDistance.parseLocalizedDouble()

    if (amount == null || liters == null || liters <= 0) return

    val pricePerLiter = amount / liters
    val consumption = if (distance != null && distance > 0) {
        liters / distance * 100.0
    } else {
        null
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(dimens.spacing.sectionTop))
        SectionLabel(stringResource(R.string.calculated))
        Spacer(Modifier.height(10.dp))
        KpiGrid(
            items = buildList {
                add(
                    Kpi(
                        label = stringResource(R.string.price_per_liter_label),
                        value = pricePerLiter.formatNumber(3),
                        unit = "€/L"
                    )
                )
                if (consumption != null) {
                    add(
                        Kpi(
                            label = stringResource(R.string.consumption_label),
                            value = consumption.formatNumber(2),
                            unit = "L/100"
                        )
                    )
                }
            }
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Calculated — both", showBackground = true, widthDp = 390)
@Composable
private fun PreviewCalculatedBoth() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            CalculatedValuesCard(
                amountPaid = "70,43",
                litersAdded = "39,3",
                tripDistance = "641"
            )
        }
    }
}

/** No distance yet, so consumption cannot be worked out and is simply absent. */
@Preview(name = "Calculated — price only", showBackground = true, widthDp = 390)
@Composable
private fun PreviewCalculatedPriceOnly() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(Modifier.padding(18.dp)) {
            CalculatedValuesCard(
                amountPaid = "70,43",
                litersAdded = "39,3",
                tripDistance = ""
            )
        }
    }
}
