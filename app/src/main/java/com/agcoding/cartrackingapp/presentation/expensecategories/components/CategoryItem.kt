package com.agcoding.cartrackingapp.presentation.expensecategories.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun CategoryItem(
    name: String,
    isCustom: Boolean,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (isCustom && onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_category),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Category Item - Predefined", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryItemPredefined() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoryItem(
            name = "Fuel",
            isCustom = false,
            onDelete = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Category Item - Custom", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryItemCustom() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoryItem(
            name = "Car Wash",
            isCustom = true,
            onDelete = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Category Item - Long Name", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryItemLongName() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoryItem(
            name = "Extended Warranty & Service Plan",
            isCustom = true,
            onDelete = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Category Item - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCategoryItemDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CategoryItem(
            name = "Tire Replacement",
            isCustom = true,
            onDelete = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
