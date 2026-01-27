package com.agcoding.cartrackingapp.presentation.expensecategories.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_category)) },
        text = {
            Column {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.category_name)) },
                    placeholder = { Text(stringResource(R.string.category_name_placeholder)) },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        categoryName.isBlank() -> {
                            errorMessage = "Category name cannot be empty"
                        }
                        categoryName.length < 2 -> {
                            errorMessage = "Category name too short"
                        }
                        categoryName.length > 50 -> {
                            errorMessage = "Category name too long"
                        }
                        else -> {
                            onConfirm(categoryName.trim())
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.add))
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

@Preview(name = "Add Category Dialog - Empty", showBackground = true)
@Composable
private fun PreviewAddCategoryDialogEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        AddCategoryDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(name = "Add Category Dialog - Dark", showBackground = true)
@Composable
private fun PreviewAddCategoryDialogDark() {
    CarTrackingAppTheme(darkTheme = true) {
        AddCategoryDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}
