package com.agcoding.cartrackingapp.domain.usecase.voice

import com.agcoding.cartrackingapp.domain.model.VoiceRefillData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Watches the live transcript while the user dictates a refill and reports the
 * moment all three fields — cost, liters and distance — have been captured, so
 * recording can end on its own instead of waiting for the Stop button.
 *
 * ## Why a stability window
 * Partial transcripts are revised as the user keeps speaking: while saying
 * "εκατό" the recognizer may briefly report "ένα", which would parse as a
 * complete (but wrong) reading. So a complete reading is not acted on
 * immediately — it must stay unchanged for [stabilityWindowMillis]. Any change
 * cancels the pending stop and restarts the window, which means auto-stop only
 * fires once the user has actually gone quiet on a full set of values.
 *
 * The detector fires at most once per session; call [reset] when a new listening
 * session starts or the current one is cancelled.
 *
 * @param scope coroutine scope owning the pending-stop job (the ViewModel's)
 * @param parser used for the offline, non-suspending parse of each partial
 * @param stabilityWindowMillis how long a complete reading must stay unchanged
 * @param onAutoStop invoked once, with the reading that triggered it
 */
class VoiceAutoStopDetector(
    private val scope: CoroutineScope,
    private val parser: ParseVoiceRefillUseCase,
    private val stabilityWindowMillis: Long = DEFAULT_STABILITY_WINDOW_MILLIS,
    private val onAutoStop: (VoiceRefillData) -> Unit
) {

    /** Pending stop, scheduled while a complete reading holds steady. */
    private var pendingJob: Job? = null

    /** The reading [pendingJob] is waiting on, to detect revisions. */
    private var pendingData: VoiceRefillData? = null

    /** Guards against firing twice within one listening session. */
    private var fired: Boolean = false

    /**
     * Feeds the latest partial transcript to the detector.
     *
     * @return the fields recognised so far, so the UI can show live progress.
     */
    fun onPartialTranscript(transcript: String): VoiceRefillData {
        val parsed = parser.parseLocally(transcript)
        if (fired) return parsed

        if (!parsed.isComplete()) {
            cancelPending()
            return parsed
        }

        // Same values as the pending reading: let the window keep running.
        if (parsed == pendingData && pendingJob?.isActive == true) return parsed

        cancelPending()
        pendingData = parsed
        pendingJob = scope.launch {
            delay(stabilityWindowMillis)
            if (!fired) {
                fired = true
                onAutoStop(parsed)
            }
        }
        return parsed
    }

    /** Clears all state so the next listening session starts fresh. */
    fun reset() {
        fired = false
        cancelPending()
    }

    private fun cancelPending() {
        pendingJob?.cancel()
        pendingJob = null
        pendingData = null
    }

    companion object {
        /**
         * Long enough for the recognizer to revise a half-spoken number, short
         * enough that the stop still feels like a direct response to finishing.
         */
        const val DEFAULT_STABILITY_WINDOW_MILLIS = 1_200L
    }
}
