package com.agcoding.cartrackingapp.presentation.settings
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

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
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
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
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToDeveloper: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
        ) {
            SettingsContent(
                modifier = Modifier.then(
                    if (isTablet) Modifier.fillMaxWidth(0.7f)
                    else Modifier.fillMaxWidth()
                )
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                SettingsGroupNavigationCard(
                    title = stringResource(R.string.settings_group_appearance_title),
                    subtitle = stringResource(R.string.settings_group_appearance_subtitle),
                    icon = Icons.Outlined.Palette,
                    onClick = onNavigateToAppearance
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsGroupNavigationCard(
                    title = stringResource(R.string.settings_group_data_storage_title),
                    subtitle = stringResource(R.string.settings_group_data_storage_subtitle),
                    icon = Icons.Outlined.Storage,
                    onClick = onNavigateToDataStorage
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsGroupNavigationCard(
                    title = stringResource(R.string.settings_group_expense_categories_title),
                    subtitle = stringResource(R.string.settings_group_expense_categories_subtitle),
                    icon = Icons.Outlined.Category,
                    onClick = onNavigateToExpenseCategories
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsGroupNavigationCard(
                    title = stringResource(R.string.settings_group_permissions_title),
                    subtitle = stringResource(R.string.settings_group_permissions_subtitle),
                    icon = Icons.Outlined.Security,
                    onClick = onNavigateToPermissions
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsGroupNavigationCard(
                    title = stringResource(R.string.settings_group_help_about_title),
                    subtitle = stringResource(R.string.settings_group_help_about_subtitle),
                    icon = Icons.Outlined.Info,
                    onClick = onNavigateToHelpAbout
                )

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

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CarTrackingAppTheme {
        SettingsScreen()
    }
}
