package com.agcoding.cartrackingapp.presentation.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.settings.components.SettingsContent
import com.agcoding.cartrackingapp.presentation.settings.components.SettingsGroupNavigationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToDataStorage: () -> Unit = {},
    onNavigateToExpenseCategories: () -> Unit = {},
    onNavigateToHelpAbout: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToDeveloper: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
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

            // Appearance & Localization
            SettingsGroupNavigationCard(
                title = stringResource(R.string.settings_group_appearance_title),
                subtitle = stringResource(R.string.settings_group_appearance_subtitle),
                icon = Icons.Outlined.Palette,
                onClick = onNavigateToAppearance
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Data & Storage
            SettingsGroupNavigationCard(
                title = stringResource(R.string.settings_group_data_storage_title),
                subtitle = stringResource(R.string.settings_group_data_storage_subtitle),
                icon = Icons.Outlined.Storage,
                onClick = onNavigateToDataStorage
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Expense Categories
            SettingsGroupNavigationCard(
                title = stringResource(R.string.settings_group_expense_categories_title),
                subtitle = stringResource(R.string.settings_group_expense_categories_subtitle),
                icon = Icons.Outlined.Category,
                onClick = onNavigateToExpenseCategories
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Help & About
            SettingsGroupNavigationCard(
                title = stringResource(R.string.settings_group_help_about_title),
                subtitle = stringResource(R.string.settings_group_help_about_subtitle),
                icon = Icons.Outlined.Info,
                onClick = onNavigateToHelpAbout
            )

            // Developer Options (only visible in debug builds)
            if (uiState.isDebugMode) {
                Spacer(modifier = Modifier.height(12.dp))

                SettingsGroupNavigationCard(
                    title = stringResource(R.string.settings_group_developer_title),
                    subtitle = stringResource(R.string.settings_group_developer_subtitle),
                    icon = Icons.Outlined.DeveloperMode,
                    onClick = onNavigateToDeveloper
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
