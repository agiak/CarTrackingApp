package com.agcoding.cartrackingapp.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling speech-to-text recognition using Android's SpeechRecognizer API.
 *
 * This service provides a Flow-based interface for continuous speech recognition with support
 * for multiple languages (Greek and English). It handles the lifecycle of the SpeechRecognizer
 * and emits events during the recognition process.
 *
 * ## Key Features:
 * - Flow-based API for reactive speech recognition
 * - Support for Greek (el-GR) and English languages
 * - Extended timeouts for manual control (5 seconds silence tolerance)
 * - Partial results for live feedback
 * - Manual stop functionality for user-initiated completion
 * - Automatic cleanup on cancellation
 *
 * ## Usage Example:
 * ```kotlin
 * speechRecognitionService.startListening("el-GR")
 *     .collect { event ->
 *         when (event) {
 *             is SpeechRecognitionEvent.ReadyForSpeech -> { /* UI: Show listening */ }
 *             is SpeechRecognitionEvent.PartialResults -> { /* UI: Show interim text */ }
 *             is SpeechRecognitionEvent.Results -> { /* Process final text */ }
 *             is SpeechRecognitionEvent.Error -> { /* Handle error */ }
 *             else -> { /* Handle other events */ }
 *         }
 *     }
 *
 * // To stop manually (user presses stop button):
 * speechRecognitionService.stopListeningManually()
 * ```
 *
 * ## Configuration:
 * - **Silence Timeout**: 5 seconds (allows pauses during speech)
 * - **Max Duration**: 30 seconds (safety limit)
 * - **Partial Results**: Enabled (for live feedback)
 * - **Language Support**: Greek (el-GR), English (en-US), auto-detect
 *
 * @see SpeechRecognitionEvent for all possible events
 */
@Singleton
class SpeechRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * The active SpeechRecognizer instance.
     * Null when not actively recognizing speech.
     */
    private var speechRecognizer: SpeechRecognizer? = null

    /**
     * Checks if speech recognition is available on this device.
     *
     * This should be called before attempting to start speech recognition to ensure
     * the device supports the required features.
     *
     * @return true if speech recognition is available, false otherwise
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Starts listening for speech input and returns a Flow of recognition events.
     *
     * This method creates a new SpeechRecognizer instance and begins listening for speech.
     * It emits various events throughout the recognition lifecycle, allowing you to provide
     * real-time feedback to the user.
     *
     * ## Event Flow:
     * 1. **Starting** - Recognition initialization begins
     * 2. **ReadyForSpeech** - Recognizer is ready, user can start speaking
     * 3. **BeginningOfSpeech** - User has started speaking
     * 4. **PartialResults** - Interim recognition results (multiple emissions possible)
     * 5. **VolumeChanged** - Audio level changes (continuous emissions)
     * 6. **EndOfSpeech** - User stopped speaking (silence detected)
     * 7. **Results** - Final recognition result with confidence score
     * 8. **Error** - Recognition failed (see error message)
     *
     * ## Timeout Behavior:
     * - **5 seconds** of silence before considering speech complete
     * - **30 seconds** maximum recording duration
     * - Partial results emitted continuously during speech
     *
     * ## Language Support:
     * The service supports Greek and English. For Greek, it includes fallback locales:
     * - Primary: el-GR (Greek - Greece)
     * - Fallback: el (Greek generic)
     *
     * ## Lifecycle:
     * - The Flow automatically cleans up when cancelled
     * - Call [stopListeningManually] to trigger final results
     * - Call [stopListening] to cancel and cleanup
     *
     * @param languageCode The BCP-47 language code (e.g., "el-GR" for Greek, "en-US" for English).
     *                     Defaults to "el-GR" (Greek - Greece).
     * @return A cold Flow that emits [SpeechRecognitionEvent]s throughout the recognition process.
     *         The Flow completes when recognition finishes successfully or on error.
     * @see SpeechRecognitionEvent
     * @see stopListeningManually
     */
    fun startListening(languageCode: String = "el-GR"): Flow<SpeechRecognitionEvent> = callbackFlow {
        if (!isAvailable()) {
            trySend(SpeechRecognitionEvent.Error("Speech recognition not available on this device"))
            close()
            return@callbackFlow
        }

        // Create speech recognizer
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer

        // Create recognition intent with extended timeouts for manual control
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)

            // Ensure Greek language support
            if (languageCode.startsWith("el")) {
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
                // Add alternative Greek locale variations
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("el-GR", "el"))
            }

            // SIGNIFICANTLY INCREASED TIMEOUTS FOR MANUAL CONTROL
            // Allow 5 seconds of silence before considering speech possibly complete
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)

            // Maximum recording duration: 30 seconds (safety limit)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 30000L)

            // Enable partial results for live feedback
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)

            // Prefer continuous recognition (less aggressive stopping)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }

        // Set up recognition listener
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(SpeechRecognitionEvent.ReadyForSpeech)
            }

            override fun onBeginningOfSpeech() {
                trySend(SpeechRecognitionEvent.BeginningOfSpeech)
            }

            override fun onRmsChanged(rmsdB: Float) {
                trySend(SpeechRecognitionEvent.VolumeChanged(rmsdB))
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Not used
            }

            override fun onEndOfSpeech() {
                trySend(SpeechRecognitionEvent.EndOfSpeech)
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error: $error"
                }
                trySend(SpeechRecognitionEvent.Error(errorMessage))
                close()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

                if (!matches.isNullOrEmpty()) {
                    val bestMatch = matches[0]
                    val confidence = scores?.getOrNull(0) ?: 0.5f
                    trySend(SpeechRecognitionEvent.Results(bestMatch, confidence))
                } else {
                    trySend(SpeechRecognitionEvent.Error("No results"))
                }
                close()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    trySend(SpeechRecognitionEvent.PartialResults(matches[0]))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Not used
            }
        })

        // Start listening
        trySend(SpeechRecognitionEvent.Starting)
        recognizer.startListening(intent)

        // Cleanup when flow is cancelled
        awaitClose {
            stopListening()
        }
    }

    /**
     * Manually stops listening and triggers final results processing.
     *
     * This method should be called when the user explicitly indicates they are done speaking
     * (e.g., by pressing a "Stop" button). It triggers the speech recognizer to:
     * 1. Stop accepting new audio input
     * 2. Process any buffered audio
     * 3. Emit final recognition results via [SpeechRecognitionEvent.Results]
     *
     * ## Difference from [stopListening]:
     * - **stopListeningManually()**: Processes buffered audio and emits final results (graceful stop)
     * - **stopListening()**: Immediately cancels and cleans up without final results (hard stop)
     *
     * ## Usage:
     * Call this when the user presses a "Stop Recording" or "Done" button.
     * The Flow will receive a [SpeechRecognitionEvent.Results] event with the recognized text.
     *
     * @see stopListening for immediate cancellation
     * @see SpeechRecognitionEvent.Results
     */
    fun stopListeningManually() {
        android.util.Log.d("SpeechRecognition", "Manual stop requested by user")
        speechRecognizer?.stopListening()
    }

    /**
     * Stops listening immediately and cleans up all speech recognition resources.
     *
     * This method performs an immediate hard stop of the speech recognizer without
     * processing any buffered audio. It:
     * 1. Stops accepting audio input
     * 2. Cancels any pending recognition operations
     * 3. Destroys the SpeechRecognizer instance
     * 4. Releases all associated resources
     *
     * ## When to Use:
     * - When cancelling/dismissing the speech input UI
     * - When the Flow is cancelled (automatic cleanup)
     * - When you need to abort recognition immediately
     * - On screen destruction or navigation away
     *
     * ## Note:
     * This does NOT emit final results. If you want to process the current audio buffer
     * and get results, use [stopListeningManually] instead.
     *
     * This method is idempotent - calling it multiple times is safe.
     *
     * @see stopListeningManually for graceful stop with results
     */
    fun stopListening() {
        android.util.Log.d("SpeechRecognition", "Stopping and cleaning up speech recognizer")
        speechRecognizer?.apply {
            stopListening()
            cancel()
            destroy()
        }
        speechRecognizer = null
    }

    /**
     * Gets the device's current locale formatted for speech recognition.
     *
     * Returns the locale in BCP-47 format (e.g., "el-GR", "en-US") which is compatible
     * with Android's speech recognition API.
     *
     * ## Use Case:
     * Use this to automatically detect the user's language preference and pass it
     * to [startListening] for a localized experience.
     *
     * ## Example:
     * ```kotlin
     * val locale = speechRecognitionService.getDeviceLocale() // e.g., "el-GR"
     * speechRecognitionService.startListening(locale).collect { event -> ... }
     * ```
     *
     * @return The device locale in BCP-47 format (language-COUNTRY, e.g., "el-GR", "en-US")
     * @see startListening
     */
    fun getDeviceLocale(): String {
        val locale = Locale.getDefault()
        return "${locale.language}-${locale.country}"
    }
}

/**
 * Events emitted during the speech recognition process.
 *
 * This sealed class represents all possible states and events that occur during
 * speech recognition. Events are emitted in a specific order, though some may be
 * emitted multiple times (e.g., PartialResults, VolumeChanged).
 *
 * ## Event Lifecycle:
 * ```
 * Starting → ReadyForSpeech → BeginningOfSpeech →
 * [VolumeChanged] (continuous) →
 * [PartialResults] (multiple) →
 * EndOfSpeech → Results
 * ```
 *
 * ## Error Handling:
 * If an error occurs at any point, an [Error] event is emitted and the Flow completes.
 *
 * ## Usage Example:
 * ```kotlin
 * speechRecognitionService.startListening().collect { event ->
 *     when (event) {
 *         is Starting -> showLoading()
 *         is ReadyForSpeech -> showListeningIndicator()
 *         is BeginningOfSpeech -> animateMicrophone()
 *         is VolumeChanged -> updateVolumeIndicator(event.volume)
 *         is PartialResults -> updateLiveText(event.text)
 *         is EndOfSpeech -> showProcessing()
 *         is Results -> processText(event.text, event.confidence)
 *         is Error -> showError(event.message)
 *     }
 * }
 * ```
 *
 * @see SpeechRecognitionService.startListening
 */
sealed class SpeechRecognitionEvent {
    /**
     * Recognition is initializing.
     *
     * Emitted immediately when [SpeechRecognitionService.startListening] is called.
     * The recognizer is being set up but not yet ready to accept audio.
     */
    object Starting : SpeechRecognitionEvent()

    /**
     * The recognizer is ready to accept audio input.
     *
     * This is emitted once the SpeechRecognizer has fully initialized and is
     * listening for audio. The user can now start speaking.
     *
     * **UI Suggestion**: Display "Listening..." or activate microphone animation.
     */
    object ReadyForSpeech : SpeechRecognitionEvent()

    /**
     * The user has started speaking.
     *
     * Emitted when the recognizer detects that speech has begun. This indicates
     * that audio input above the noise threshold has been detected.
     *
     * **UI Suggestion**: Provide visual feedback that speech is being captured
     * (e.g., pulsing microphone icon).
     */
    object BeginningOfSpeech : SpeechRecognitionEvent()

    /**
     * Audio volume level changed.
     *
     * Emitted continuously while listening. The volume parameter represents the
     * RMS (Root Mean Square) sound level in decibels.
     *
     * **UI Suggestion**: Animate volume bars or microphone icon based on volume level.
     *
     * @property volume The RMS sound level in dB. Typical range: 0-10 (quiet to loud)
     */
    data class VolumeChanged(val volume: Float) : SpeechRecognitionEvent()

    /**
     * The user stopped speaking (silence detected).
     *
     * Emitted when the recognizer detects the end of speech (silence for the
     * configured timeout period, default 5 seconds). The recognizer is now
     * processing the audio to produce final results.
     *
     * **UI Suggestion**: Show "Processing..." or a loading indicator.
     */
    object EndOfSpeech : SpeechRecognitionEvent()

    /**
     * Partial recognition results (interim text).
     *
     * Emitted multiple times during speech recognition to provide live feedback.
     * This text is not final and may change as more audio is processed.
     *
     * **UI Suggestion**: Display this text in a preview area to show what's
     * being recognized in real-time.
     *
     * @property text The interim recognized text (may be incomplete or inaccurate)
     */
    data class PartialResults(val text: String) : SpeechRecognitionEvent()

    /**
     * Final recognition results.
     *
     * Emitted once after speech processing is complete. This is the final
     * recognized text with an associated confidence score. The Flow completes
     * after this event.
     *
     * **UI Suggestion**: Use this text as the final input value.
     *
     * @property text The final recognized text
     * @property confidence Confidence score (0.0 to 1.0, where 1.0 is highest confidence).
     *                      Values above 0.7 generally indicate high quality recognition.
     */
    data class Results(val text: String, val confidence: Float) : SpeechRecognitionEvent()

    /**
     * An error occurred during recognition.
     *
     * Emitted when the recognition process fails for any reason. Common errors include:
     * - No speech detected
     * - Network timeout
     * - Audio recording error
     * - Insufficient permissions
     *
     * The Flow completes after this event.
     *
     * **UI Suggestion**: Display the error message to the user and allow retry.
     *
     * @property message Human-readable error description
     */
    data class Error(val message: String) : SpeechRecognitionEvent()
}
