package com.agcoding.cartrackingapp.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.preferences.AppLanguage
import com.agcoding.cartrackingapp.data.preferences.AppTheme
import com.agcoding.cartrackingapp.presentation.components.GroupedListContainer
import com.agcoding.cartrackingapp.presentation.components.GroupedDivider
import com.agcoding.cartrackingapp.presentation.components.GroupedRow
import com.agcoding.cartrackingapp.presentation.components.HeaderIconButton
import com.agcoding.cartrackingapp.presentation.components.RowAccent
import com.agcoding.cartrackingapp.presentation.components.ScreenHeader
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * Settings.
 *
 * Seven cards become three lists. A card per setting reads as seven unrelated
 * things that happen to be stacked; one card of hairline-separated rows reads
 * as a list, which is what it is — and it is what the eye can skim without
 * counting shadows. The grouping into General / Data / Privacy is what makes
 * "where would permissions live?" answerable before scrolling.
 *
 * The account comes out of the list and goes to the top as its own card. It is
 * the only row here that has a *state* worth reading at a glance — signed in
 * or not — and the only one with something to do rather than somewhere to go.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToDataStorage: () -> Unit = {},
    onNavigateToExpenseCategories: () -> Unit = {},
    onNavigateToHelpAbout: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToDeveloper: () -> Unit = {},
    onNavigateToCloudAccount: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreenContent(
        isSignedIn = uiState.isSignedIn,
        isDebugMode = uiState.isDebugMode,
        theme = uiState.appSettings.theme,
        language = uiState.appSettings.language,
        onNavigateToAppearance = onNavigateToAppearance,
        onNavigateToDataStorage = onNavigateToDataStorage,
        onNavigateToExpenseCategories = onNavigateToExpenseCategories,
        onNavigateToHelpAbout = onNavigateToHelpAbout,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToDeveloper = onNavigateToDeveloper,
        onNavigateToCloudAccount = onNavigateToCloudAccount,
    )
}

@Composable
private fun SettingsScreenContent(
    isSignedIn: Boolean,
    isDebugMode: Boolean,
    theme: AppTheme,
    language: AppLanguage,
    onNavigateToAppearance: () -> Unit,
    onNavigateToDataStorage: () -> Unit,
    onNavigateToExpenseCategories: () -> Unit,
    onNavigateToHelpAbout: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToDeveloper: () -> Unit,
    onNavigateToCloudAccount: () -> Unit,
) {
    val dimens = LocalAppDimens.current
    val margin = dimens.spacing.screenHorizontal

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenHeader(
                title = stringResource(R.string.settings),
                actions = {
                    HeaderIconButton(
                        icon = Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.tab_notifications_cd),
                        onClick = onNavigateToNotifications,
                    )
                },
            )

            Column(modifier = Modifier.padding(horizontal = margin)) {
                AccountCard(isSignedIn = isSignedIn, onClick = onNavigateToCloudAccount)

                SettingsSection(stringResource(R.string.settings_section_general)) {
                    GroupedRow(
                        title = stringResource(R.string.settings_group_appearance_title),
                        // The current state, not a description of the screen:
                        // "Dark, English" answers the question that sends most
                        // people in here without them having to go in.
                        subtitle = "${theme.displayName()} · ${language.displayName()}",
                        icon = Icons.Outlined.Palette,
                        onClick = onNavigateToAppearance,
                    )
                    GroupedDivider()
                    GroupedRow(
                        title = stringResource(R.string.settings_group_help_about_title),
                        subtitle = stringResource(R.string.settings_group_help_about_subtitle),
                        icon = Icons.Outlined.Info,
                        accent = RowAccent.Service,
                        onClick = onNavigateToHelpAbout,
                    )
                }

                SettingsSection(stringResource(R.string.settings_section_data)) {
                    GroupedRow(
                        title = stringResource(R.string.settings_group_data_storage_title),
                        subtitle = stringResource(R.string.settings_group_data_storage_subtitle),
                        icon = Icons.Outlined.Storage,
                        onClick = onNavigateToDataStorage,
                    )
                    GroupedDivider()
                    GroupedRow(
                        title = stringResource(R.string.settings_group_expense_categories_title),
                        subtitle = stringResource(R.string.settings_group_expense_categories_subtitle),
                        icon = Icons.Outlined.Category,
                        accent = RowAccent.Other,
                        onClick = onNavigateToExpenseCategories,
                    )
                }

                SettingsSection(stringResource(R.string.settings_section_privacy)) {
                    GroupedRow(
                        title = stringResource(R.string.settings_group_permissions_title),
                        subtitle = stringResource(R.string.settings_group_permissions_subtitle),
                        icon = Icons.Outlined.Security,
                        onClick = onNavigateToPermissions,
                    )
                    if (isDebugMode) {
                        GroupedDivider()
                        GroupedRow(
                            title = stringResource(R.string.settings_group_developer_title),
                            subtitle = stringResource(R.string.settings_group_developer_subtitle),
                            icon = Icons.Outlined.DeveloperMode,
                            accent = RowAccent.Other,
                            onClick = onNavigateToDeveloper,
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

/** A labelled group of rows, with the space above it that separates sections. */
@Composable
private fun SettingsSection(
    label: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Spacer(Modifier.height(LocalAppDimens.current.spacing.sectionTop))
    SectionLabel(label)
    Spacer(Modifier.height(10.dp))
    GroupedListContainer(content = content)
}

/**
 * The account, as a card rather than a row.
 *
 * Signed out it carries the only call to action on the screen; signed in it is
 * a doorway like the rest. Either way it says which of the two is true, which
 * is the thing a user comes to Settings to check after a sync looks wrong.
 */
@Composable
private fun AccountCard(isSignedIn: Boolean, onClick: () -> Unit) {
    val scheme = LocalAppColorScheme.current

    StyledCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(scheme.actionSecondary),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = scheme.actionPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_group_cloud_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.contentPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        if (isSignedIn) R.string.cloud_signed_in_message
                        else R.string.settings_group_cloud_subtitle
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.contentSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.width(12.dp))

            if (isSignedIn) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = scheme.contentDisabled,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(LocalAppDimens.current.radius.button))
                        .background(scheme.actionPrimary)
                        .clickable(onClick = onClick)
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = stringResource(R.string.cloud_sign_in_action),
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.actionContent,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppTheme.displayName(): String = stringResource(
    when (this) {
        AppTheme.SYSTEM -> R.string.theme_name_system
        AppTheme.LIGHT -> R.string.theme_name_light
        AppTheme.DARK -> R.string.theme_name_dark
    }
)

@Composable
private fun AppLanguage.displayName(): String = stringResource(
    when (this) {
        AppLanguage.ENGLISH -> R.string.language_english
        AppLanguage.GREEK -> R.string.language_greek
    }
)

// ── previews ─────────────────────────────────────────────────────────────

@Preview(name = "Settings — signed out", showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        SettingsScreenContent(
            isSignedIn = false,
            isDebugMode = true,
            theme = AppTheme.DARK,
            language = AppLanguage.GREEK,
            onNavigateToAppearance = {}, onNavigateToDataStorage = {},
            onNavigateToExpenseCategories = {}, onNavigateToHelpAbout = {},
            onNavigateToNotifications = {}, onNavigateToPermissions = {},
            onNavigateToDeveloper = {}, onNavigateToCloudAccount = {},
        )
    }
}

@Preview(name = "Settings — signed in", showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenSignedInPreview() {
    CarTrackingAppTheme(darkTheme = false) {
        SettingsScreenContent(
            isSignedIn = true,
            isDebugMode = false,
            theme = AppTheme.SYSTEM,
            language = AppLanguage.ENGLISH,
            onNavigateToAppearance = {}, onNavigateToDataStorage = {},
            onNavigateToExpenseCategories = {}, onNavigateToHelpAbout = {},
            onNavigateToNotifications = {}, onNavigateToPermissions = {},
            onNavigateToDeveloper = {}, onNavigateToCloudAccount = {},
        )
    }
}
