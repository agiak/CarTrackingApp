package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * The heading above a group of settings, on every settings sub-screen.
 *
 * It used to be set in the accent colour, which put a line of brand colour
 * above each group and so competed with the controls below for the eye. A
 * section heading's job is to be findable when scanning for structure and
 * ignorable when reading content, so it is now the quietest text on the
 * screen — the shared [SectionLabel] used everywhere else.
 *
 * Several callers pass an already upper-cased string from strings.xml; the
 * label upper-cases again, which is harmless, and leaves those resources free
 * to be sentence-cased later without touching any screen.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    SectionLabel(text = title, modifier = modifier.padding(vertical = 4.dp))
}

@Preview(name = "Section header", showBackground = true, widthDp = 380)
@Composable
private fun PreviewSectionHeader() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            SectionHeader(title = "APPEARANCE")
            SectionHeader(title = "Data & storage")
        }
    }
}
