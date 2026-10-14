package com.agcoding.cartrackingapp.presentation.cloud.sync

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStage
import com.agcoding.cartrackingapp.cloud.domain.model.SyncSummary
import com.agcoding.cartrackingapp.presentation.cloud.emoji
import com.agcoding.cartrackingapp.presentation.cloud.labelRes
import com.agcoding.cartrackingapp.presentation.cloud.messageRes
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.SuccessCheckmark
import com.agcoding.cartrackingapp.presentation.theme.AppSuccess
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import kotlin.math.roundToInt

/**
 * The first sync after signing in or registering, watched.
 *
 * It exists because handing the user's whole history to a server — or waiting
 * for it to come back down — is the one moment where silence is unnerving: the
 * point is not that a percentage is informative, it is that the app is visibly
 * doing the thing it promised.
 *
 * When there is nothing to move, there is nothing to watch, and the screen
 * says so by being a spinner and then getting out of the way.
 */
@Composable
fun CloudSyncProgressScreen(
    direction: CloudSyncDirection,
    onFinished: () -> Unit,
    viewModel: CloudSyncProgressViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.startOnce(direction) }

    // Outside the AnimatedContent on purpose: a LaunchedEffect inside it is
    // composed again for the outgoing content during a transition, and this
    // one navigates.
    LaunchedEffect(uiState.step) {
        if (uiState.step == CloudSyncStep.Finished) onFinished()
    }

    // Backing out mid-sync would leave half the rows moved and no screen
    // watching the rest.
    BackHandler(
        enabled = uiState.step == CloudSyncStep.Working ||
            uiState.step == CloudSyncStep.Finished
    ) {}

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            // Finished shows the same thing Working does for the moment it
            // takes to leave; crossfading it with itself would dip to grey.
            targetState = if (uiState.step == CloudSyncStep.Finished) {
                CloudSyncStep.Working
            } else {
                uiState.step
            },
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "cloud-sync-step"
        ) { step ->
            when (step) {
                // quiet is null until the phone has been counted, and an
                // uncounted phone gets the spinner: it is the one of the two
                // that is never wrong.
                CloudSyncStep.Working, CloudSyncStep.Finished ->
                    if (uiState.quiet == false) WorkingPage(uiState) else QuietPage()

                CloudSyncStep.Success -> SuccessPage(uiState.direction, uiState.summary, onFinished)
                CloudSyncStep.Failed -> FailedPage(
                    direction = uiState.direction,
                    errorRes = uiState.error?.messageRes(),
                    onRetry = viewModel::retry,
                    onSkip = onFinished,
                )
            }
        }
    }
}

/**
 * The whole screen for a sync with nothing to measure.
 *
 * A new account on a phone with no records has nothing to send, and a bar
 * filling up over zero rows is an animation impersonating a measurement. This
 * says the one true thing instead: something is happening, wait.
 */
@Composable
private fun QuietPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            color = AppSuccess.color,
            strokeWidth = 5.dp,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.cloud_sync_preparing),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WorkingPage(uiState: CloudSyncProgressUiState) {
    // A second smoothing pass on top of the ViewModel's pacing: the state
    // updates in 40 ms steps, and tweening between them is what turns a
    // sequence of jumps into a bar that moves.
    val progress by animateFloatAsState(
        targetValue = uiState.progress,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "cloud-sync-progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${(progress * 100).roundToInt()}%",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = AppSuccess.color
        )

        Spacer(modifier = Modifier.height(20.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = AppSuccess.color,
            trackColor = AppSuccess.color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round,
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = uiState.stage,
            transitionSpec = {
                (slideInVertically { it / 2 } + fadeIn()) togetherWith
                    (slideOutVertically { -it / 2 } + fadeOut())
            },
            label = "cloud-sync-stage"
        ) { stage ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stage.emoji, fontSize = 36.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(stage.labelRes(uiState.direction)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.cloud_sync_keep_open),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SuccessPage(
    direction: CloudSyncDirection,
    summary: SyncSummary?,
    onFinished: () -> Unit,
) {
    val isDownload = direction == CloudSyncDirection.Download
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        SuccessCheckmark(size = 140.dp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(
                if (isDownload) {
                    R.string.cloud_sync_restored_title
                } else {
                    R.string.cloud_sync_success_title
                }
            ),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                if (isDownload) {
                    R.string.cloud_sync_restored_body
                } else {
                    R.string.cloud_sync_success_body
                }
            ),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (summary != null && summary.changedAnything) {
            Spacer(modifier = Modifier.height(24.dp))
            StyledCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SummaryLine("🚗", stringResource(R.string.cloud_summary_cars), summary.cars)
                    SummaryLine("🗺️", stringResource(R.string.cloud_summary_trips), summary.trips)
                    SummaryLine("⛽", stringResource(R.string.cloud_summary_refills), summary.refills)
                    SummaryLine("🧾", stringResource(R.string.cloud_summary_expenses), summary.expenses)
                    SummaryLine("🏷️", stringResource(R.string.cloud_summary_categories), summary.categories)
                    if (summary.received > 0) {
                        SummaryLine("📥", stringResource(R.string.cloud_summary_received), summary.received)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = stringResource(R.string.cloud_sync_continue),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun SummaryLine(emoji: String, label: String, count: Int) {
    if (count == 0) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toString(),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FailedPage(
    direction: CloudSyncDirection,
    errorRes: Int?,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "😕", fontSize = 48.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.cloud_sync_failed_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            // Says plainly that nothing was lost. A failed first upload looks
            // exactly like a failed save unless the screen rules that out —
            // and a failed restore looks like an empty account, which is a
            // worse thing to believe.
            text = stringResource(
                if (direction == CloudSyncDirection.Download) {
                    R.string.cloud_sync_failed_body_down
                } else {
                    R.string.cloud_sync_failed_body
                }
            ),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        errorRes?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(it),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(stringResource(R.string.cloud_sync_retry), fontSize = 16.sp)
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.cloud_sync_skip))
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PreviewCloudSyncWorking() {
    CarTrackingAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            WorkingPage(
                CloudSyncProgressUiState(
                    progress = 0.46f,
                    stage = SyncStage.REFILLS,
                    quiet = false,
                )
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PreviewCloudSyncDownloading() {
    CarTrackingAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            WorkingPage(
                CloudSyncProgressUiState(
                    direction = CloudSyncDirection.Download,
                    progress = 0.46f,
                    stage = SyncStage.REFILLS,
                    quiet = false,
                )
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PreviewCloudSyncQuiet() {
    CarTrackingAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            QuietPage()
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PreviewCloudSyncSuccess() {
    CarTrackingAppTheme {
        SuccessPage(
            direction = CloudSyncDirection.Upload,
            summary = SyncSummary(cars = 2, trips = 3, refills = 84, expenses = 19, categories = 12),
            onFinished = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PreviewCloudSyncFailed() {
    CarTrackingAppTheme {
        FailedPage(
            direction = CloudSyncDirection.Upload,
            errorRes = R.string.cloud_error_no_connection,
            onRetry = {},
            onSkip = {},
        )
    }
}
