package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories

/**
 * How an expense is badged in a list row: servicing gets the wrench, everything
 * else gets the receipt.
 *
 * Both icons already carry those meanings elsewhere in the app — the wrench is
 * the "Services" figure on the expense summary card and the maintenance anomaly
 * marker, the receipt is the expense entry in the FAB menu and the expense
 * filter chip — so this only stops the two from sharing one glyph in the lists.
 *
 * The classification is [ExpenseCategories.isServiceCategory], the same
 * predicate the statistics use, so a row can never show a wrench for an expense
 * the "Services" total left out.
 */
fun expenseCategoryIcon(category: String): ImageVector =
    if (ExpenseCategories.isServiceCategory(category)) Icons.Default.Build else Icons.Default.Receipt

/**
 * Container and glyph colours matching [expenseCategoryIcon], so the two kinds
 * are told apart by colour as well as shape when scanning a long list.
 */
data class ExpenseCategoryColors(val container: Color, val icon: Color)

@Composable
fun expenseCategoryColors(category: String): ExpenseCategoryColors =
    ExpenseCategoryColors(
        container = MaterialTheme.colorScheme.primaryContainer,
        icon = MaterialTheme.colorScheme.primary
    )
