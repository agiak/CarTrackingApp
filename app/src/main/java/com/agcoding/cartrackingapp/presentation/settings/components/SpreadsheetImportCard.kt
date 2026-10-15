package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.PrimaryButton
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.components.TonalButton
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers

/**
 * Bringing a spreadsheet in.
 *
 * The card used to be a wall: a header, a paragraph, a nested grey card with
 * three numbered lines in 12sp, then two cramped buttons and a two-line note
 * in 11sp — six blocks of text to explain two buttons. It is a sequence of
 * three steps, so it is drawn as three steps, each one a numbered row, and the
 * paragraph that restated them is gone.
 *
 * The order of the buttons follows the order of the steps: download the sample
 * first, import second. The import is the primary action because it is what
 * the card is for, but it is the thing you do *after* the other one, so it
 * sits below rather than beside — side by side at this width the two labels
 * truncated in Greek anyway.
 */
@Composable
fun SpreadsheetImportCard(
    isImporting: Boolean,
    isGeneratingSample: Boolean,
    onImport: () -> Unit,
    onGenerateSample: () -> Unit
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val busy = isImporting || isGeneratingSample

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.card))
            .background(scheme.backgroundCard)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(dimens.size.iconTile)
                    .clip(RoundedCornerShape(dimens.radius.iconTile))
                    .background(scheme.actionPrimary.copy(alpha = 0.16f))
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    tint = scheme.actionPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.spreadsheet_import_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.contentPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.spreadsheet_file_format),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.contentSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        SectionLabel(stringResource(R.string.spreadsheet_import_how_to_title))
        Spacer(modifier = Modifier.height(10.dp))

        ImportStep(1, stringResource(R.string.spreadsheet_import_step_1))
        Spacer(modifier = Modifier.height(10.dp))
        ImportStep(2, stringResource(R.string.spreadsheet_import_step_2))
        Spacer(modifier = Modifier.height(10.dp))
        ImportStep(3, stringResource(R.string.spreadsheet_import_step_3))

        Spacer(modifier = Modifier.height(18.dp))

        TonalButton(
            label = if (isGeneratingSample) {
                stringResource(R.string.spreadsheet_generating)
            } else {
                stringResource(R.string.spreadsheet_generate_sample)
            },
            onClick = onGenerateSample,
            enabled = !busy,
            icon = Icons.Default.Download,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        PrimaryButton(
            label = stringResource(R.string.spreadsheet_import_button),
            onClick = onImport,
            enabled = !busy,
            isBusy = isImporting,
            icon = Icons.Default.Upload,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.spreadsheet_import_note),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.contentDisabled
        )
    }
}

/**
 * A numbered step.
 *
 * The number is in the mono face, in a circle, so three of them line up as a
 * sequence rather than reading as three unrelated sentences that happen to
 * start with a digit.
 */
@Composable
private fun ImportStep(number: Int, text: String) {
    val scheme = LocalAppColorScheme.current

    Row(verticalAlignment = Alignment.Top) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(scheme.backgroundSecondary)
        ) {
            Text(
                text = number.toString(),
                style = AppNumbers.tag,
                color = scheme.contentSecondary,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.contentSecondary,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun SpreadsheetImportCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            SpreadsheetImportCard(
                isImporting = false,
                isGeneratingSample = false,
                onImport = {},
                onGenerateSample = {}
            )
        }
    }
}

@Preview(name = "Importing", showBackground = true, widthDp = 380)
@Composable
private fun SpreadsheetImportCardBusyPreview() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(18.dp)) {
            SpreadsheetImportCard(
                isImporting = true,
                isGeneratingSample = false,
                onImport = {},
                onGenerateSample = {}
            )
        }
    }
}
