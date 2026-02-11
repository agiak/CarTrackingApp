package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun CustomizationCard(
    onManageExpenseCategoriesClick: () -> Unit
) {
    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        tintAlpha = 0.3f
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Expense Categories Row
            SettingsRow(
                icon = Icons.Default.Category,
                iconBackgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.customization_expense_categories),
                subtitle = stringResource(R.string.customization_expense_categories_desc),
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = onManageExpenseCategoriesClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCustomizationCardLight() {
    CarTrackingAppTheme(darkTheme = false) {
        CustomizationCard(
            onManageExpenseCategoriesClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCustomizationCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CustomizationCard(
            onManageExpenseCategoriesClick = {}
        )
    }
}

