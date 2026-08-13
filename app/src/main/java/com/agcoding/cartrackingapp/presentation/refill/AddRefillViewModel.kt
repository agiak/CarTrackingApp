package com.agcoding.cartrackingapp.presentation.refill

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.BuildConfig
import com.agcoding.cartrackingapp.data.local.LocationProvider
import com.agcoding.cartrackingapp.data.preferences.SettingsPreferences
import com.agcoding.cartrackingapp.data.voice.SpeechRecognitionEvent
import com.agcoding.cartrackingapp.data.voice.SpeechRecognitionService
import com.agcoding.cartrackingapp.domain.model.VoiceParsingResult
import com.agcoding.cartrackingapp.domain.model.VoiceRefillData
import com.agcoding.cartrackingapp.domain.usecase.refill.AddFuelRefillUseCase
import com.agcoding.cartrackingapp.domain.usecase.voice.ParseVoiceRefillUseCase
import com.agcoding.cartrackingapp.domain.validation.RefillValidator
import com.agcoding.cartrackingapp.shared.domain.result.Result
import com.agcoding.cartrackingapp.shared.ui.utils.simpleMessage
import com.agcoding.cartrackingapp.util.GeocodingUtil
import com.agcoding.cartrackingapp.util.parseLocalizedDouble
import com.agcoding.cartrackingapp.util.sanitizeDecimalInput
import com.agcoding.cartrackingapp.widget.QuickAddWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AddRefillViewModel @Inject constructor(
    private val addFuelRefillUseCase: AddFuelRefillUseCase,
    private val carRepository: com.agcoding.cartrackingapp.domain.repository.CarRepository,
    private val locationProvider: LocationProvider,
    private val speechRecognitionService: SpeechRecognitionService,
    private val parseVoiceRefillUseCase: ParseVoiceRefillUseCase,
    private val settingsPreferences: SettingsPreferences,
    @ApplicationContext private val context: android.content.Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var carId: Long = 0L

    private val _uiState = MutableStateFlow(AddRefillUiState())
    val uiState: StateFlow<AddRefillUiState> = _uiState.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    // Voice recognition state
    private val _voiceState = MutableStateFlow<VoiceRefillState>(VoiceRefillState.Idle)
    val voiceState: StateFlow<VoiceRefillState> = _voiceState.asStateFlow()

    val isVoiceAvailable: Boolean get() = speechRecognitionService.isAvailable()

    fun setCarId(id: Long) {
        carId = id

        // Load car's current odometer
        viewModelScope.launch {
            try {
                val car = carRepository.getCarById(carId).first()
                if (car != null) {
                    _uiState.value = _uiState.value.copy(
                        previousOdometer = car.currentOdometer,
                        odometer = car.currentOdometer.toInt().toString()
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AddRefillVM", "Unexpected error", e)
            }
        }

        // Populate with random values in debug mode
        if (BuildConfig.DEBUG) {
            populateRandomValues()
        }

        if (carId > 0 && locationProvider.hasLocationPermission()) {
            fetchLocation()
        }
    }

    private fun populateRandomValues() {
        val randomAmountPaid = Random.nextInt(30, 100).toString() + "," + Random.nextInt(0, 99).toString().padStart(2, '0')
        val randomLiters = Random.nextInt(25, 60).toString() + "," + Random.nextInt(0, 9).toString()
        val randomDistance = Random.nextInt(300, 700)

        // Calculate new odometer reading from previous + random distance
        val newOdometer = (_uiState.value.previousOdometer + randomDistance).toInt()

        _uiState.value = _uiState.value.copy(
            amountPaid = randomAmountPaid,
            litersAdded = randomLiters,
            odometer = newOdometer.toString(),
            selectedDateMillis = System.currentTimeMillis()
        )
    }

    fun updateAmountPaid(value: String) {
        val clean = sanitizeDecimalInput(value)
        val costError = RefillValidator.validateCost(context, clean)
        _uiState.value = _uiState.value.copy(
            amountPaid = clean,
            errorMessage = null,
            fieldErrors = if (costError != null) {
                _uiState.value.fieldErrors + ("cost" to costError)
            } else {
                _uiState.value.fieldErrors - "cost"
            }
        )
    }

    fun updateLitersAdded(value: String) {
        val clean = sanitizeDecimalInput(value)
        val litersError = RefillValidator.validateLiters(context, clean)
        _uiState.value = _uiState.value.copy(
            litersAdded = clean,
            errorMessage = null,
            fieldErrors = if (litersError != null) {
                _uiState.value.fieldErrors + ("liters" to litersError)
            } else {
                _uiState.value.fieldErrors - "liters"
            }
        )
    }

    fun updateTripDistance(value: String) {
        val clean = sanitizeDecimalInput(value)
        val distanceError = RefillValidator.validateDistance(context, clean)
        _uiState.value = _uiState.value.copy(
            tripDistance = clean,
            errorMessage = null,
            fieldErrors = if (distanceError != null) {
                _uiState.value.fieldErrors + ("distance" to distanceError)
            } else {
                _uiState.value.fieldErrors - "distance"
            }
        )
    }

    fun updateOdometer(value: String) {
        val odometerError = RefillValidator.validateOdometer(context, value, _uiState.value.previousOdometer)
        _uiState.value = _uiState.value.copy(
            odometer = value,
            errorMessage = null,
            fieldErrors = if (odometerError != null) {
                _uiState.value.fieldErrors + ("odometer" to odometerError)
            } else {
                _uiState.value.fieldErrors - "odometer"
            }
        )
    }

    fun updateNotes(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun showDatePicker() {
        _showDatePicker.value = true
    }

    fun hideDatePicker() {
        _showDatePicker.value = false
    }

    fun updateDate(dateMillis: Long) {
        _uiState.value = _uiState.value.copy(selectedDateMillis = dateMillis)
        hideDatePicker()
    }

    private fun fetchLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocation = true)
            val location = locationProvider.getCurrentLocation()
            _uiState.value = _uiState.value.copy(
                location = location,
                isLoadingLocation = false
            )

            // Reverse-geocode to a readable name. Don't clobber a name the user
            // already typed manually.
            if (location != null && _uiState.value.locationName.isBlank()) {
                _uiState.value = _uiState.value.copy(isLoadingLocationName = true)
                val name = GeocodingUtil.getAddressFromLocation(
                    context, location.latitude, location.longitude
                )
                _uiState.value = _uiState.value.copy(
                    locationName = if (_uiState.value.locationName.isBlank()) name.orEmpty()
                    else _uiState.value.locationName,
                    isLoadingLocationName = false
                )
            }
        }
    }

    fun updateLocationName(value: String) {
        _uiState.value = _uiState.value.copy(locationName = value)
    }

    fun saveRefill(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Use RefillValidator for comprehensive validation
        val validationResult = RefillValidator.validateRefill(
            context = context,
            liters = state.litersAdded,
            cost = state.amountPaid,
            distance = state.tripDistance
        )

        if (!validationResult.isValid) {
            // Separate consumption error from field errors
            val consumptionError = validationResult.errors["consumption"]
            val fieldErrors = validationResult.errors.filterKeys { it != "consumption" }

            _uiState.value = state.copy(
                fieldErrors = fieldErrors,
                errorMessage = consumptionError ?: fieldErrors.values.firstOrNull()
            )
            return
        }

        // Parse validated values
        val amount = state.amountPaid.parseLocalizedDouble() ?: 0.0
        val liters = state.litersAdded.parseLocalizedDouble() ?: 0.0
        val distance = state.tripDistance.parseLocalizedDouble() ?: 0.0

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null, fieldErrors = emptyMap())

            when (val result = addFuelRefillUseCase(
                carId = carId,
                amountPaid = amount,
                litersAdded = liters,
                tripDistance = distance,
                timestamp = state.selectedDateMillis,
                location = state.location,
                locationName = state.locationName.takeIf { it.isNotBlank() },
                notes = state.notes.takeIf { it.isNotBlank() },
            )) {
                is Result.Success -> {
                    QuickAddWidgetReceiver.updateWidgets(context)
                    _uiState.value = state.copy(isSaving = false)
                    resetForm()
                    onSuccess()
                }
                is Result.Error -> _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = result.error.simpleMessage,
                )
            }
        }
    }

    // ===== Voice Entry Methods =====

    /**
     * Start voice entry for refill data
     * Prioritizes Greek language support
     */
    fun startVoiceEntry() {
        if (!speechRecognitionService.isAvailable()) {
            _voiceState.value = VoiceRefillState.Error("Speech recognition not available on this device")
            return
        }

        _voiceState.value = VoiceRefillState.Listening("")

        viewModelScope.launch {
            // Get device locale, prefer Greek if available
            val deviceLocale = speechRecognitionService.getDeviceLocale()
            val languageCode = if (deviceLocale.startsWith("el")) {
                "el-GR" // Explicitly use Greek
            } else {
                deviceLocale
            }

            Timber.d( "Starting voice recognition with locale: $languageCode")

            speechRecognitionService.startListening(
                languageCode = languageCode
            ).collect { event ->
                handleSpeechRecognitionEvent(event)
            }
        }
    }

    /**
     * Handle speech recognition events
     */
    private suspend fun handleSpeechRecognitionEvent(event: SpeechRecognitionEvent) {
        when (event) {
            is SpeechRecognitionEvent.ReadyForSpeech -> {
                _voiceState.value = VoiceRefillState.Listening("")
            }
            is SpeechRecognitionEvent.BeginningOfSpeech -> {
                _voiceState.value = VoiceRefillState.Listening("")
            }
            is SpeechRecognitionEvent.PartialResults -> {
                _voiceState.value = VoiceRefillState.Listening(event.text)
            }
            is SpeechRecognitionEvent.Results -> {
                _voiceState.value = VoiceRefillState.Processing(event.text)
                parseVoiceTranscript(event.text)
            }
            is SpeechRecognitionEvent.Error -> {
                val errorMsg = when {
                    event.message.contains("No speech") -> "No speech detected. Please try again."
                    event.message.contains("permission") -> "Microphone permission required"
                    event.message.contains("Network") -> "Network error. Using offline parsing."
                    else -> event.message
                }
                _voiceState.value = VoiceRefillState.Error(errorMsg)
            }
            else -> { /* Ignore other events */ }
        }
    }

    /**
     * Parse voice transcript using LLM or regex
     * Uses the selected LLM model from settings
     */
    private suspend fun parseVoiceTranscript(transcript: String) {
        try {
            Timber.d( "Parsing transcript: '$transcript'")

            // Get the selected LLM model from preferences
            val selectedModel = settingsPreferences.llmModelFlow.first()
            Timber.d( "Selected LLM model: ${selectedModel.displayName} (${selectedModel.modelId})")

            // Get OpenAI API key from BuildConfig (if configured)
            // To configure: Add to local.properties: OPENAI_API_KEY=your_key_here
            // Then in build.gradle.kts: buildConfigField("String", "OPENAI_API_KEY", "\"${properties["OPENAI_API_KEY"]}\"")
            val apiKey: String? = try {
                BuildConfig.OPENAI_API_KEY.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null // API key not configured, will use regex fallback
            }

            val result = parseVoiceRefillUseCase(transcript, null, selectedModel)

            when (result) {
                is VoiceParsingResult.Success -> {
                    Timber.d( "Parse SUCCESS: ${result.data}")
                    _voiceState.value = VoiceRefillState.Parsed(result.data)
                }
                is VoiceParsingResult.LowConfidence -> {
                    Timber.d( "Parse LOW CONFIDENCE: ${result.data}")
                    _voiceState.value = VoiceRefillState.Parsed(result.data, lowConfidence = true)
                }
                is VoiceParsingResult.Error -> {
                    Timber.e( "Parse ERROR: ${result.message}")
                    _voiceState.value = VoiceRefillState.Error(result.message, result.transcript)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception parsing transcript: ${e.message}")
            _voiceState.value = VoiceRefillState.Error(
                "Failed to parse voice input: ${e.message}",
                transcript
            )
        }
    }

    /**
     * Confirm and apply parsed voice data to form
     * Uses US Locale to ensure period (.) as decimal separator
     */
    fun confirmVoiceParsedData() {
        val currentVoiceState = _voiceState.value
        if (currentVoiceState is VoiceRefillState.Parsed) {
            val data = currentVoiceState.data

            Timber.d( "Applying parsed data to form:")
            Timber.d( "  cost=${data.cost}, liters=${data.liters}, distance=${data.distance}")

            // Apply parsed data to form fields as raw comma-decimal values
            if (data.cost != null && data.cost > 0) {
                val formatted = data.cost.toString().replace('.', ',')
                Timber.d( "  Applying cost: $formatted")
                updateAmountPaid(formatted)
            }
            if (data.liters != null && data.liters > 0) {
                val formatted = data.liters.toString().replace('.', ',')
                Timber.d( "  Applying liters: $formatted")
                updateLitersAdded(formatted)
            }
            if (data.distance != null && data.distance > 0) {
                val formatted = data.distance.toInt().toString()
                Timber.d( "  Applying distance: $formatted")
                updateTripDistance(formatted)
            }

            // Reset voice state
            _voiceState.value = VoiceRefillState.Idle
        }
    }

    /**
     * Manually stop voice recording (user pressed Stop button)
     * This triggers the final results processing
     */
    fun stopVoiceRecording() {
        Timber.d( "User manually stopped recording")
        speechRecognitionService.stopListeningManually()
        // State will be updated when onResults callback fires
    }

    /**
     * Cancel voice entry (dismiss without processing)
     */
    fun cancelVoiceEntry() {
        Timber.d( "User cancelled voice entry")
        speechRecognitionService.stopListening()
        _voiceState.value = VoiceRefillState.Idle
    }

    /**
     * Stop voice listening (internal cleanup)
     */
    fun stopVoiceEntry() {
        speechRecognitionService.stopListening()
    }

    fun resetForm() {
        _uiState.value = AddRefillUiState()
        _showDatePicker.value = false
        _voiceState.value = VoiceRefillState.Idle
        speechRecognitionService.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionService.stopListening()
    }
}

/**
 * UI State for refill form
 */
data class AddRefillUiState(
    val amountPaid: String = "",
    val litersAdded: String = "",
    val tripDistance: String = "",
    val odometer: String = "",
    val previousOdometer: Double = 0.0,
    val notes: String = "",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val location: com.agcoding.cartrackingapp.domain.model.Location? = null,
    val locationName: String = "",
    val isLoadingLocation: Boolean = false,
    val isLoadingLocationName: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

/**
 * Voice recognition state
 */
sealed class VoiceRefillState {
    object Idle : VoiceRefillState()
    data class Listening(val partialText: String) : VoiceRefillState()
    data class Processing(val transcript: String) : VoiceRefillState()
    data class Parsed(val data: VoiceRefillData, val lowConfidence: Boolean = false) : VoiceRefillState()
    data class Error(val message: String, val transcript: String = "") : VoiceRefillState()
}

