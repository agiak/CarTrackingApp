package com.agcoding.cartrackingapp.presentation.cloud.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.cloud.domain.model.CloudError
import com.agcoding.cartrackingapp.cloud.domain.model.CloudException
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStage
import com.agcoding.cartrackingapp.cloud.domain.model.SyncSummary
import com.agcoding.cartrackingapp.cloud.domain.sync.CloudSync
import com.agcoding.cartrackingapp.domain.repository.DatabaseStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Which way the data is about to move.
 *
 * The sync itself is the same call in both directions, but the screen is not:
 * telling someone who has just signed in that their cars are being *uploaded*
 * describes the opposite of what they asked for and the opposite of what they
 * are waiting to see.
 */
enum class CloudSyncDirection { Upload, Download }

enum class CloudSyncStep {
    Working,
    Success,
    Failed,

    /** Done, with nothing to show for it — the screen should just leave. */
    Finished,
}

data class CloudSyncProgressUiState(
    val direction: CloudSyncDirection = CloudSyncDirection.Upload,
    val step: CloudSyncStep = CloudSyncStep.Working,
    /** 0f..1f, and never allowed to go backwards. */
    val progress: Float = 0f,
    val stage: SyncStage = SyncStage.CONNECTING,
    val summary: SyncSummary? = null,
    val error: CloudError? = null,
    /**
     * Whether this run has nothing to measure: a spinner for as long as the
     * call takes, and no bar.
     *
     * Null until the phone has been counted, because a bar that appears and
     * then turns into a spinner is worse than a spinner that stays.
     */
    val quiet: Boolean? = null,
)

/**
 * Drives the first sync after signing in or registering.
 *
 * There are two quite different jobs here. Handing a phone full of history to
 * a server takes long enough to be worth watching, so that gets the paced bar:
 * the displayed value is the smaller of what the sync reports and what the
 * clock allows, and the screen stays up for at least [MIN_SCREEN_MILLIS] so
 * the stage labels can be read.
 *
 * A new account on an empty phone is the other job, and it is not the same
 * one. There is nothing to send, so there is nothing to put a percentage on —
 * a bar there is an animation pretending to be a measurement. That case gets a
 * plain spinner, for exactly as long as the call takes, and then leaves.
 */
@HiltViewModel
class CloudSyncProgressViewModel @Inject constructor(
    private val cloudSync: CloudSync,
    private val databaseStatsRepository: DatabaseStatsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudSyncProgressUiState())
    val uiState: StateFlow<CloudSyncProgressUiState> = _uiState.asStateFlow()

    private var pacing: Job? = null
    private var work: Job? = null
    private var started = false
    private var direction = CloudSyncDirection.Upload

    /** Safe to call on every composition; only the first one does anything. */
    fun startOnce(direction: CloudSyncDirection) {
        if (started) return
        started = true
        this.direction = direction
        run()
    }

    fun retry() {
        if (_uiState.value.step == CloudSyncStep.Working) return
        run()
    }

    private fun run() {
        pacing?.cancel()
        work?.cancel()
        _uiState.value = CloudSyncProgressUiState(
            direction = direction,
            // A download has rows coming the other way by definition, so it
            // never needs counting and its bar can start on the first frame.
            quiet = if (direction == CloudSyncDirection.Download) false else null,
        )

        work = viewModelScope.launch {
            if (direction == CloudSyncDirection.Upload) {
                val nothingToSend = runCatching {
                    databaseStatsRepository.observeStats().first().isEmpty
                }.getOrDefault(false)
                _uiState.value = _uiState.value.copy(quiet = nothingToSend)
            }
            if (_uiState.value.quiet == true) runQuietly() else runWithBar()
        }
    }

    /**
     * One call, one spinner.
     *
     * No tick at the end either: the sign-up screen has just played its own,
     * and a success page reporting that nothing was uploaded is a page about
     * nothing standing between the user and the app.
     */
    private suspend fun runQuietly() {
        runCatching { cloudSync.syncNow() }.fold(
            onSuccess = { _uiState.value = _uiState.value.copy(step = CloudSyncStep.Finished) },
            onFailure = { fail(it) },
        )
    }

    private suspend fun runWithBar() {
        val startedAt = System.currentTimeMillis()
        var reported = 0f

        pacing = viewModelScope.launch {
            launch {
                cloudSync.progress
                    .filterNotNull()
                    // Left over from an earlier sync; it would slam the bar to
                    // 1f before this one has even connected.
                    .collect { if (it.stage != SyncStage.DONE) reported = maxOf(reported, it.fraction) }
            }
            while (true) {
                val elapsed = (System.currentTimeMillis() - startedAt).toFloat()
                // 0.97 so the bar never reaches the end on the clock alone —
                // only finishing the sync completes it.
                val paced = elapsed / MIN_SCREEN_MILLIS * 0.97f
                val shown = minOf(reported, paced)
                _uiState.value = _uiState.value.let { state ->
                    val next = maxOf(state.progress, shown)
                    state.copy(progress = next, stage = stageFor(next))
                }
                delay(FRAME_MILLIS)
            }
        }

        runCatching { cloudSync.syncNow() }.fold(
            onSuccess = { summary ->
                // Let the bar and its labels play out even if the sync was
                // instant. Only on the way to a tick: holding a bar that
                // is already going nowhere just delays bad news.
                delay(
                    (MIN_SCREEN_MILLIS - (System.currentTimeMillis() - startedAt))
                        .coerceAtLeast(0)
                )
                pacing?.cancel()
                _uiState.value = _uiState.value.copy(progress = 1f, stage = SyncStage.DONE)
                delay(DONE_PAUSE_MILLIS)
                _uiState.value = _uiState.value.copy(
                    step = CloudSyncStep.Success,
                    summary = summary,
                )
            },
            onFailure = { fail(it) },
        )
    }

    private fun fail(error: Throwable) {
        pacing?.cancel()
        _uiState.value = _uiState.value.copy(
            step = CloudSyncStep.Failed,
            error = (error as? CloudException)?.error ?: CloudError.UNKNOWN,
        )
    }

    /**
     * The label that belongs with a point of the bar.
     *
     * Read from the displayed fraction rather than from the stage the sync
     * reports, so the words and the bar can never disagree — the bar is paced
     * and the sync is not. These thresholds mirror the `from`/`to` spans in
     * SyncManager.sync(); changing one means changing the other.
     */
    private fun stageFor(fraction: Float): SyncStage = when {
        fraction < 0.05f -> SyncStage.CONNECTING
        fraction < 0.10f -> SyncStage.CHECKING
        fraction < 0.30f -> SyncStage.CARS
        fraction < 0.42f -> SyncStage.TRIPS
        fraction < 0.70f -> SyncStage.REFILLS
        fraction < 0.90f -> SyncStage.EXPENSES
        fraction < 0.96f -> SyncStage.CATEGORIES
        fraction < 1f -> SyncStage.FINISHING
        else -> SyncStage.DONE
    }

    private companion object {
        /** Long enough for every stage label to be read, short enough not to annoy. */
        const val MIN_SCREEN_MILLIS = 4_000L
        const val DONE_PAUSE_MILLIS = 600L
        const val FRAME_MILLIS = 40L
    }
}
