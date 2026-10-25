package com.agcoding.cartrackingapp.presentation.cloud.sync

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import com.agcoding.cartrackingapp.presentation.components.AppTextButton
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sell
import com.agcoding.cartrackingapp.presentation.components.GroupedDivider
import com.agcoding.cartrackingapp.presentation.components.GroupedListContainer
import com.agcoding.cartrackingapp.presentation.components.GroupedRow
import com.agcoding.cartrackingapp.presentation.components.PrimaryButton
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.agcoding.cartrackingapp.presentation.components.TransferProgress
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

/** The five tables that actually move rows, in the order the sync walks them. */
private val DATA_STAGES = listOf(
    SyncStage.CARS,
    SyncStage.TRIPS,
    SyncStage.REFILLS,
    SyncStage.EXPENSES,
    SyncStage.CATEGORIES,
)

@Composable
private fun WorkingPage(uiState: CloudSyncProgressUiState) {
    // No tween here: the ViewModel paces the state in 40 ms steps and
    // TransferProgress smooths between them itself, so a second pass at this
    // level would only add lag between the real figure and the one on screen.
    val scheme = LocalAppColorScheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TransferProgress(progress = uiState.progress)

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = uiState.stage,
            transitionSpec = {
                (slideInVertically { it / 2 } + fadeIn()) togetherWith
                    (slideOutVertically { -it / 2 } + fadeOut())
            },
            label = "cloud-sync-stage"
        ) { stage ->
            Text(
                text = stringResource(stage.labelRes(uiState.direction)),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                color = scheme.contentPrimary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.cloud_sync_keep_open),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = scheme.contentSecondary,
        )

        Spacer(modifier = Modifier.height(28.dp))

        // The ring says how far; this says what. Together they answer the two
        // questions anyone staring at a sync has, and the list is also the
        // reassurance that something specific is happening rather than a bar
        // filling for its own sake.
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            DATA_STAGES.forEach { stage ->
                StageRow(
                    label = stringResource(stage.labelRes(uiState.direction)),
                    state = when {
                        uiState.stage.ordinal > stage.ordinal -> StageState.Done
                        uiState.stage.ordinal == stage.ordinal -> StageState.Active
                        else -> StageState.Pending
                    },
                )
            }
        }
    }
}

private enum class StageState { Pending, Active, Done }

@Composable
private fun StageRow(label: String, state: StageState) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.button))
            // Only the row being worked on gets a surface. Giving every row
            // one would make the list a stack of identical cards, which is
            // exactly the thing the active row needs to stand out from.
            .background(if (state == StageState.Active) scheme.backgroundCard else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(18.dp),
        ) {
            when (state) {
                StageState.Done -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = scheme.statusSuccess,
                    modifier = Modifier.size(18.dp),
                )
                StageState.Active -> CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = scheme.actionPrimary,
                    modifier = Modifier.size(15.dp),
                )
                StageState.Pending -> Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(scheme.borderDefault),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = when (state) {
                StageState.Pending -> scheme.contentDisabled
                else -> scheme.contentPrimary
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
            style = MaterialTheme.typography.titleLarge,
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
            // Icon tiles rather than emoji: an emoji is whatever the phone's
            // font decides it is, which on this screen meant five different
            // drawing styles in one card.
            GroupedListContainer {
                val lines = listOf(
                    Triple(Icons.Default.DirectionsCar, R.string.cloud_summary_cars, summary.cars),
                    Triple(Icons.Default.Route, R.string.cloud_summary_trips, summary.trips),
                    Triple(Icons.Default.LocalGasStation, R.string.cloud_summary_refills, summary.refills),
                    Triple(Icons.Default.Receipt, R.string.cloud_summary_expenses, summary.expenses),
                    Triple(Icons.Default.Sell, R.string.cloud_summary_categories, summary.categories),
                    Triple(Icons.Default.Download, R.string.cloud_summary_received, summary.received),
                ).filter { it.third > 0 }

                lines.forEachIndexed { index, (icon, labelRes, count) ->
                    if (index > 0) GroupedDivider()
                    GroupedRow(
                        title = stringResource(labelRes),
                        icon = icon,
                        trailing = {
                            Text(
                                text = count.toString(),
                                style = AppNumbers.compact,
                                color = LocalAppColorScheme.current.contentPrimary,
                            )
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            label = stringResource(R.string.cloud_sync_continue),
            onClick = onFinished,
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun FailedPage(
    direction: CloudSyncDirection,
    errorRes: Int?,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
) {
    val scheme = LocalAppColorScheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // An icon tile rather than an emoji. "\uD83D\uDE15" renders as a different
        // drawing on every phone, and on a failure screen the one thing that
        // must not look improvised is the failure.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(scheme.categoryService.copy(alpha = 0.16f)),
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = scheme.categoryService,
                modifier = Modifier.size(44.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.cloud_sync_failed_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = scheme.contentPrimary
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
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = scheme.categoryService
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            label = stringResource(R.string.cloud_sync_retry),
            onClick = onRetry,
        )
        Spacer(modifier = Modifier.height(4.dp))
        AppTextButton(
            label = stringResource(R.string.cloud_sync_skip),
            onClick = onSkip,
        )
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
