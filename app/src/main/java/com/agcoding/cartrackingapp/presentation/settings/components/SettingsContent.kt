package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.DeviceUtils

/**
 * Settings content component that wraps the settings content in a scrollable column.
 * This is a general composable that provides consistent layout for settings.
 * Only applies horizontal padding on actual tablets, not phones in landscape mode.
 */
@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isTablet = DeviceUtils.isTablet()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = if (isTablet) 24.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Settings Content - Phone", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewSettingsContentPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        SettingsContent(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "APPEARANCE")
            Text(text = "Theme: SYSTEM", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "LANGUAGE")
            Text(text = "Language: ENGLISH", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "DATA & STORAGE")
            Text(text = "Total: 2.5 MB (100 records)", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(name = "Settings Content - Tablet", showBackground = true, widthDp = 800, heightDp = 600)
@Composable
private fun PreviewSettingsContentTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        SettingsContent(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "APPEARANCE")
            Text(text = "Theme: LIGHT", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "PREFERENCES")
            Text(text = "Notifications: Enabled", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(name = "Settings Content - Dark", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewSettingsContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        SettingsContent(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "APPEARANCE")
            Text(text = "Theme: DARK", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "DEVELOPER OPTIONS")
            Text(text = "Debug Mode Active", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
