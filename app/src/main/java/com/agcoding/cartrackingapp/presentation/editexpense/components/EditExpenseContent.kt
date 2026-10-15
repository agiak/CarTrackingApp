package com.agcoding.cartrackingapp.presentation.editexpense.components

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories
import com.agcoding.cartrackingapp.presentation.components.AmountInputCard
import com.agcoding.cartrackingapp.presentation.components.LicensePlateBadge
import com.agcoding.cartrackingapp.presentation.components.PrimaryButton
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.components.ThousandsSeparatorTransformation
import com.agcoding.cartrackingapp.presentation.components.TonalButton
import com.agcoding.cartrackingapp.presentation.components.expenseCategoryIcon
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryOther
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService

/**
 * Editing an expense.
 *
 * The form is three fields, and only one of them is the point: the amount.
 * It used to be the second of four identical boxes, under a bare line of
 * headline text naming the category. Now the category is a header row with its
 * own colour and icon — the same rose or lilac the details screen uses — and
 * the amount is the hero field, in the card the add-expense form already uses
 * for the same job.
 *
 * The category is shown and not offered, with a line saying why. Moving an
 * expense between categories moves it between the service and other totals,
 * which is a different operation from fixing a typo in a price, and a picker
 * sitting in an edit form invites it to be done by accident.
 */
@Composable
fun EditExpenseContent(
    category: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    selectedDate: Long,
    onShowDatePicker: () -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    carName: String = "",
    carPlate: String = "",
    errorMessage: String? = null,
    isDirty: Boolean = true,
    onDiscard: (() -> Unit)? = null
) {
    val dimens = LocalAppDimens.current

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(4.dp))
        CategoryHeader(
            category = category,
            carName = carName,
            carPlate = carPlate,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(18.dp))
        AmountInputCard(
            value = amount,
            onValueChange = onAmountChange,
            label = stringResource(R.string.expense_amount_eur),
            suffix = "€",
            placeholder = stringResource(R.string.amount_placeholder),
            visualTransformation = ThousandsSeparatorTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(dimens.spacing.sectionTop))
        SectionLabel(stringResource(R.string.additional_information))
        Spacer(Modifier.height(10.dp))
        ExpenseDateField(
            selectedDate = selectedDate,
            onShowDatePicker = onShowDatePicker,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        ExpenseNotesField(
            notes = notes,
            onNotesChange = onNotesChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))
        CategoryLockedNote()

        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            FormError(errorMessage)
        }

        Spacer(Modifier.height(dimens.spacing.sectionTop))
        Row {
            if (onDiscard != null && isDirty) {
                TonalButton(
                    label = stringResource(R.string.edit_discard),
                    onClick = onDiscard,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
            }
            PrimaryButton(
                label = stringResource(R.string.edit_save_changes),
                onClick = onSaveClick,
                enabled = amount.isNotBlank() && isDirty,
                isBusy = isSaving,
                modifier = Modifier.weight(1.4f)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

/**
 * What is being edited: the category, in its colour, and the car it is on.
 */
@Composable
private fun CategoryHeader(
    category: String,
    carName: String,
    carPlate: String,
    modifier: Modifier = Modifier
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val isService = remember(category) { ExpenseCategories.isServiceCategory(category) }
    val accent = if (isService) scheme.categoryService else scheme.categoryOther

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(dimens.radius.tile))
            .background(accent.copy(alpha = 0.14f))
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(dimens.size.iconTile)
                .clip(RoundedCornerShape(dimens.radius.iconTile))
                .background(accent)
        ) {
            Icon(
                imageVector = expenseCategoryIcon(category),
                contentDescription = null,
                // The category colours are light tints in both themes, so a
                // glyph on one of them needs the dark end to stay legible.
                tint = androidx.compose.ui.graphics.Color(0xFF231405),
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (carName.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = carName,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.contentSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (carPlate.isNotBlank()) {
            Spacer(Modifier.width(10.dp))
            LicensePlateBadge(plate = carPlate)
        }
    }
}

/** Why there is no category picker on this screen. */
@Composable
private fun CategoryLockedNote() {
    val scheme = LocalAppColorScheme.current
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(horizontal = 4.dp)) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = scheme.contentDisabled,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.edit_expense_category_locked),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.contentDisabled
        )
    }
}

/** Whatever went wrong with saving, said where the eye already is. */
@Composable
private fun FormError(message: String) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.tile))
            .background(scheme.statusError.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = scheme.statusError,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.statusError
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Edit expense — service", showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewEditExpenseService() {
    CarTrackingAppTheme(darkTheme = true) {
        PreviewForm(category = "Brakes", dirty = true, error = null)
    }
}

@Preview(name = "Edit expense — untouched", showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewEditExpenseUntouched() {
    CarTrackingAppTheme(darkTheme = true) {
        PreviewForm(category = "Parking", dirty = false, error = null)
    }
}

@Preview(name = "Edit expense — error", showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewEditExpenseError() {
    CarTrackingAppTheme(darkTheme = false) {
        PreviewForm(category = "Insurance", dirty = true, error = "Please enter a valid amount")
    }
}

@Composable
private fun PreviewForm(category: String, dirty: Boolean, error: String?) {
    var amount by remember { mutableStateOf("239,72") }
    var notes by remember { mutableStateOf("Front pads and discs replaced at the dealer.") }

    EditExpenseContent(
        category = category,
        amount = amount,
        onAmountChange = { amount = it },
        selectedDate = 1_760_000_000_000L,
        onShowDatePicker = {},
        notes = notes,
        onNotesChange = { notes = it },
        isSaving = false,
        onSaveClick = {},
        carName = "Toyota Corolla",
        carPlate = "ABC-1234",
        errorMessage = error,
        isDirty = dirty,
        onDiscard = {},
        modifier = Modifier.padding(horizontal = 18.dp)
    )
}
