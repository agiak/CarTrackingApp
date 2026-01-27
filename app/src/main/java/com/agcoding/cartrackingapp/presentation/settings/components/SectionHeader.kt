package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 4.dp)
    )
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Section Header - Light", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSectionHeader() {
    CarTrackingAppTheme(darkTheme = false) {
        SectionHeader(
            title = "APPEARANCE",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Section Header - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSectionHeaderDark() {
    CarTrackingAppTheme(darkTheme = true) {
        SectionHeader(
            title = "DATA & STORAGE",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
