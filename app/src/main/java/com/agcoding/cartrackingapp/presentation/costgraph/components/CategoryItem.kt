package com.agcoding.cartrackingapp.presentation.costgraph.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.domain.model.CostCategory
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber

@Composable
fun CategoryItem(category: CostCategory) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(category.color))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = category.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${category.percentage.formatNumber(1)}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = category.amount.formatMoney(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Category Item - Fuel", showBackground = true, widthDp = 350)
@Composable
private fun PreviewCategoryItemFuel() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoryItem(
            category = CostCategory(
                name = "Fuel",
                amount = 850.50,
                percentage = 65.3,
                color = 0xFF4CAF50.toInt()
            )
        )
    }
}

@Preview(name = "Category Item - Service", showBackground = true, widthDp = 350)
@Composable
private fun PreviewCategoryItemService() {
    CarTrackingAppTheme(darkTheme = false) {
        CategoryItem(
            category = CostCategory(
                name = "Service",
                amount = 320.00,
                percentage = 24.5,
                color = 0xFF2196F3.toInt()
            )
        )
    }
}

@Preview(name = "Category Item - Dark", showBackground = true, widthDp = 350)
@Composable
private fun PreviewCategoryItemDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CategoryItem(
            category = CostCategory(
                name = "Other",
                amount = 135.25,
                percentage = 10.2,
                color = 0xFFFF9800.toInt()
            )
        )
    }
}
