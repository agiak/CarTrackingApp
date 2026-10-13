package com.agcoding.cartrackingapp.presentation.cloud.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.cloud.domain.model.CloudError
import com.agcoding.cartrackingapp.cloud.domain.model.CloudException
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStage
import com.agcoding.cartrackingapp.cloud.domain.model.SyncSummary
import com.agcoding.cartrackingapp.cloud.domain.sync.CloudSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CloudSyncStep { Working, Success, Failed }

data class CloudSyncProgressUiState(
    val step: CloudSyncStep = CloudSyncStep.Working,
    /** 0f..1f, and never allowed to go backwards. */
    val progress: Float = 0f,
    val stage: SyncStage = SyncStage.CONNECTING,
    val summary: SyncSummary? = null,
    val error: CloudError? = null,
)

/**
 * Drives the first upload.
 *
 * The bar shows real progress, but never faster than a readable pace. A first
 * upload of a handful of cars finishes in a few hundred milliseconds, and a
 * bar that jumps straight to 100% reads as a glitch rather than as work done —
 * so the displayed value is the smaller of what the sync reports and what the
 * clock allows, and the screen stays up for at least [MIN_SCREEN_MILLIS].
 */
@HiltViewModel
class CloudSyncProgressViewModel @Inject constructor(
    private val cloudSync: CloudSync,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudSyncProgressUiState())
    val uiState: StateFlow<CloudSyncProgressUiState> = _uiState.asStateFlow()

    private var pacing: Job? = null
    private var started = false

    /** Safe to call on every composition; only the first one does anything. */
    fun startOnce() {
        if (started) return
        started = true
        run()
    }

    fun retry() {
        if (_uiState.value.step == CloudSyncStep.Working) return
        run()
    }

    private fun run() {
        _uiState.value = CloudSyncProgressUiState()

        val startedAt = System.currentTimeMillis()
        var reported = 0f

        pacing?.cancel()
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

        viewModelScope.launch {
            val result = runCatching { cloudSync.syncNow() }
            result.fold(
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
                onFailure = { error ->
                    pacing?.cancel()
                    _uiState.value = _uiState.value.copy(
                        step = CloudSyncStep.Failed,
                        error = (error as? CloudException)?.error ?: CloudError.UNKNOWN,
                    )
                }
            )
        }
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
