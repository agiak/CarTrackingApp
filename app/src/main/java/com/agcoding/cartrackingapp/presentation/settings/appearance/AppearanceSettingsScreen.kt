package com.agcoding.cartrackingapp.presentation.settings.appearance
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.settings.SettingsViewModel
import com.agcoding.cartrackingapp.presentation.settings.components.AppearanceCard
import com.agcoding.cartrackingapp.presentation.settings.components.ColorPaletteCard
import com.agcoding.cartrackingapp.presentation.settings.components.LanguageCard
import com.agcoding.cartrackingapp.presentation.settings.components.SectionHeader
import com.agcoding.cartrackingapp.presentation.settings.components.SettingsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPalette by viewModel.colorPalettePreferences.selectedPaletteFlow.collectAsState(initial = ColorPalette.SYSTEM)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_group_appearance_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()

        // Use centered content with max width on tablets for better readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
        ) {
            SettingsContent(
                modifier = Modifier.then(
                    if (isTablet) Modifier.fillMaxWidth(0.7f) // 70% width on tablets
                    else Modifier.fillMaxWidth()
                )
            ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Theme Section
            SectionHeader(title = stringResource(R.string.settings_section_appearance))

            AppearanceCard(
                currentTheme = uiState.appSettings.theme,
                onThemeChange = viewModel::updateTheme
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Language Section
            SectionHeader(title = stringResource(R.string.settings_section_language))

            LanguageCard(
                selectedLanguage = uiState.appSettings.language,
                onLanguageSelected = viewModel::updateLanguage
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Color Palette Section
            ColorPaletteCard(
                selectedPalette = selectedPalette,
                onPaletteSelected = viewModel::updateColorPalette
            )

            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppearanceSettingsScreenPreview() {
    CarTrackingAppTheme {
        AppearanceSettingsScreen(onNavigateBack = {})
    }
}
