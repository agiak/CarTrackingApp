package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.preferences.AppLanguage
import com.agcoding.cartrackingapp.presentation.components.GroupedDivider
import com.agcoding.cartrackingapp.presentation.components.GroupedListContainer
import com.agcoding.cartrackingapp.presentation.components.GroupedRow
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Which language the app speaks.
 *
 * The card used to carry its own header — an icon circle, "App language", and
 * "Choose your language" — directly under a section heading that already said
 * LANGUAGE. Three ways of saying the same word before the first option. What
 * is left is the list.
 *
 * Radio buttons rather than the old coloured dot: this is a one-of-many
 * choice, and a dot that is merely a different colour when selected does not
 * say that picking one unpicks the other.
 */
@Composable
fun LanguageCard(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    GroupedListContainer {
        AppLanguage.entries.forEachIndexed { index, language ->
            // No icon tiles here, so the hairline runs the full width: an
            // inset divider would be stepping around something that is not
            // there, and the rows' text would not line up with it.
            if (index > 0) GroupedDivider(startInset = false)
            GroupedRow(
                title = language.displayName(),
                onClick = { onLanguageSelected(language) },
                trailing = {
                    RadioButton(
                        selected = selectedLanguage == language,
                        onClick = { onLanguageSelected(language) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun AppLanguage.displayName(): String = stringResource(
    when (this) {
        AppLanguage.ENGLISH -> R.string.language_english
        AppLanguage.GREEK -> R.string.language_greek
    }
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewLanguageCard() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            LanguageCard(selectedLanguage = AppLanguage.GREEK, onLanguageSelected = {})
        }
    }
}
