package com.agcoding.cartrackingapp.presentation.expensedetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories
import com.agcoding.cartrackingapp.presentation.components.GroupedDivider
import com.agcoding.cartrackingapp.presentation.components.GroupedListContainer
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.components.TonalButton
import com.agcoding.cartrackingapp.presentation.components.expenseCategoryIcon
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryOther
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService
import com.agcoding.cartrackingapp.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * One expense.
 *
 * The hero takes the colour of the expense's own category — rose for servicing,
 * lilac for everything else — rather than the one accent every card used to
 * wear. Expenses are the part of the app where "what kind" is the first
 * question, and the screen can answer it before a word is read. The same two
 * colours already mean the same two things on the cost bar and in the
 * statistics, so this is the category's colour arriving where it was missing
 * rather than a new one being invented.
 *
 * Car, category and date were three facts with nothing between them; they are
 * one grouped list. Edit and Delete move from the app bar down to where the
 * thumb is — they stay in the bar too, because that is where someone who knows
 * the app will reach, but on a screen whose whole job is "look at this one
 * thing and then act on it" the actions should not be the smallest targets on
 * it.
 */
@Composable
internal fun ExpenseDetailsContent(
    expense: Expense,
    carName: String,
    modifier: Modifier = Modifier,
    carPlate: String? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val datePattern = stringResource(R.string.date_format_full_with_time)
    val dateFormat = remember(datePattern) {
        SimpleDateFormat(datePattern, Locale.getDefault())
    }
    val isService = remember(expense.category) {
        ExpenseCategories.isServiceCategory(expense.category)
    }
    val accent = if (isService) scheme.categoryService else scheme.categoryOther
    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()

    Box(
        modifier = modifier,
        contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .then(if (isTablet) Modifier.fillMaxWidth(0.7f) else Modifier.fillMaxWidth())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.spacing.screenHorizontal)
                .padding(top = 4.dp, bottom = 28.dp)
        ) {
            CategoryHero(
                expense = expense,
                accent = accent,
                dateText = dateFormat.format(Date(expense.timestamp))
            )

            Spacer(Modifier.height(dimens.spacing.sectionTop))
            SectionLabel(stringResource(R.string.additional_information))
            Spacer(Modifier.height(10.dp))
            GroupedListContainer {
                DetailRow(
                    icon = Icons.Default.DirectionsCar,
                    label = stringResource(R.string.expense_details_car),
                    value = if (carPlate.isNullOrBlank()) carName else "$carName · $carPlate"
                )
                GroupedDivider()
                DetailRow(
                    icon = Icons.Default.Category,
                    label = stringResource(R.string.expense_category),
                    value = expense.category,
                    valueTint = accent
                )
                GroupedDivider()
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = stringResource(R.string.date_time),
                    value = dateFormat.format(Date(expense.timestamp))
                )
            }

            expense.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Spacer(Modifier.height(dimens.spacing.sectionTop))
                SectionLabel(stringResource(R.string.notes))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.contentPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.radius.tile))
                        .background(scheme.backgroundCard)
                        .padding(16.dp)
                )
            }

            if (onEdit != null || onDelete != null) {
                Spacer(Modifier.height(dimens.spacing.sectionTop))
                Row {
                    if (onEdit != null) {
                        TonalButton(
                            label = stringResource(R.string.edit),
                            onClick = onEdit,
                            icon = Icons.Default.Edit,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (onEdit != null && onDelete != null) {
                        Spacer(Modifier.width(10.dp))
                    }
                    if (onDelete != null) {
                        TonalButton(
                            label = stringResource(R.string.delete),
                            onClick = onDelete,
                            icon = Icons.Default.Delete,
                            contentColor = scheme.statusError,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * The amount, on the category's colour.
 *
 * A tint rather than a fill: at full strength the rose would be a block of
 * colour with the figure fighting it, and the amount is what the card is for.
 */
@Composable
private fun CategoryHero(expense: Expense, accent: Color, dateText: String) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val text = expense.amount.formatMoney()
    val cut = text.indexOfFirst { it == ',' || it == '.' }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.hero))
            .background(accent.copy(alpha = 0.18f))
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(accent)
        ) {
            Icon(
                imageVector = expenseCategoryIcon(expense.category),
                contentDescription = null,
                // Ink on the category colour, which is a light tint in both
                // themes, so the glyph needs the dark end rather than onPrimary.
                tint = Color(0xFF231405),
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = expense.category,
            style = MaterialTheme.typography.titleSmall,
            color = accent,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            if (cut > 0) {
                Text(
                    text = text.substring(0, cut),
                    style = AppNumbers.summary,
                    color = scheme.contentPrimary,
                    maxLines = 1
                )
                Text(
                    text = text.substring(cut),
                    style = AppNumbers.heroFraction,
                    color = scheme.contentSecondary,
                    modifier = Modifier.padding(bottom = 5.dp),
                    maxLines = 1
                )
            } else {
                Text(
                    text = text,
                    style = AppNumbers.summary,
                    color = scheme.contentPrimary,
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.contentSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueTint: Color? = null
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
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
                color = valueTint ?: scheme.contentPrimary
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

private val previewService = Expense(
    id = 1,
    carId = 1,
    category = "Brakes",
    amount = 239.72,
    timestamp = 1_760_000_000_000L,
    notes = "Front pads and discs replaced at the dealer. Next check in 30.000 km."
)

@Preview(name = "Expense — service", showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewExpenseDetailsService() {
    CarTrackingAppTheme(darkTheme = true) {
        ExpenseDetailsContent(
            expense = previewService,
            carName = "Toyota Corolla",
            carPlate = "ABC-1234",
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview(name = "Expense — other", showBackground = true, widthDp = 390, heightDp = 760)
@Composable
private fun PreviewExpenseDetailsOther() {
    CarTrackingAppTheme(darkTheme = true) {
        ExpenseDetailsContent(
            expense = previewService.copy(
                category = "Parking",
                amount = 12.0,
                notes = null
            ),
            carName = "Toyota Corolla",
            carPlate = "ABC-1234",
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview(name = "Expense — light", showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewExpenseDetailsLight() {
    CarTrackingAppTheme(darkTheme = false) {
        ExpenseDetailsContent(
            expense = previewService,
            carName = "Toyota Corolla",
            carPlate = "ABC-1234",
            onEdit = {},
            onDelete = {}
        )
    }
}
