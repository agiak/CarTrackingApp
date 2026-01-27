package com.agcoding.cartrackingapp.presentation.expensecategories.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun DeleteCategoryDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_category)) },
        text = {
            Text(
                stringResource(
                    R.string.delete_category_confirm,
                    categoryName
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Delete Category Dialog - Light", showBackground = true)
@Composable
private fun PreviewDeleteCategoryDialog() {
    CarTrackingAppTheme(darkTheme = false) {
        DeleteCategoryDialog(
            categoryName = "Car Wash",
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(name = "Delete Category Dialog - Long Name", showBackground = true)
@Composable
private fun PreviewDeleteCategoryDialogLongName() {
    CarTrackingAppTheme(darkTheme = false) {
        DeleteCategoryDialog(
            categoryName = "Extended Warranty & Service Plan",
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(name = "Delete Category Dialog - Dark", showBackground = true)
@Composable
private fun PreviewDeleteCategoryDialogDark() {
    CarTrackingAppTheme(darkTheme = true) {
        DeleteCategoryDialog(
            categoryName = "Tire Replacement",
            onDismiss = {},
            onConfirm = {}
        )
    }
}
