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
 * Service for handling speech-to-text recognition
 */
@Singleton
class SpeechRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var speechRecognizer: SpeechRecognizer? = null

    /**
     * Check if speech recognition is available on this device
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Start listening for speech input
     * Returns a Flow that emits speech recognition events
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
     * Manually stop listening (user-initiated)
     * This triggers final results processing
     */
    fun stopListeningManually() {
        android.util.Log.d("SpeechRecognition", "Manual stop requested by user")
        speechRecognizer?.stopListening()
    }

    /**
     * Stop listening and cleanup resources
     * Called on cancel/dismiss
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
     * Get device locale for speech recognition
     */
    fun getDeviceLocale(): String {
        val locale = Locale.getDefault()
        return "${locale.language}-${locale.country}"
    }
}

/**
 * Events emitted during speech recognition
 */
sealed class SpeechRecognitionEvent {
    object Starting : SpeechRecognitionEvent()
    object ReadyForSpeech : SpeechRecognitionEvent()
    object BeginningOfSpeech : SpeechRecognitionEvent()
    data class VolumeChanged(val volume: Float) : SpeechRecognitionEvent()
    object EndOfSpeech : SpeechRecognitionEvent()
    data class PartialResults(val text: String) : SpeechRecognitionEvent()
    data class Results(val text: String, val confidence: Float) : SpeechRecognitionEvent()
    data class Error(val message: String) : SpeechRecognitionEvent()
}

