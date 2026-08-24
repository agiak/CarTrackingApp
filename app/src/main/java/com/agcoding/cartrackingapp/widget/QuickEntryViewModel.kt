package com.agcoding.cartrackingapp.widget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.BuildConfig
import com.agcoding.cartrackingapp.data.preferences.SettingsPreferences
import com.agcoding.cartrackingapp.data.voice.SpeechRecognitionEvent
import com.agcoding.cartrackingapp.data.voice.SpeechRecognitionService
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.VoiceParsingResult
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.usecase.expense.AddExpenseUseCase
import com.agcoding.cartrackingapp.domain.usecase.refill.AddFuelRefillUseCase
import com.agcoding.cartrackingapp.domain.usecase.voice.ParseVoiceRefillUseCase
import com.agcoding.cartrackingapp.domain.usecase.voice.VoiceAutoStopDetector
import com.agcoding.cartrackingapp.domain.validation.RefillValidator
import com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState
import com.agcoding.cartrackingapp.shared.domain.result.Result
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

@HiltViewModel
class QuickEntryViewModel @Inject constructor(
    private val refillRepository: RefillRepository,
    private val carRepository: CarRepository,
    private val expenseRepository: ExpenseRepository,
    private val addFuelRefillUseCase: AddFuelRefillUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val speechRecognitionService: SpeechRecognitionService,
    private val parseVoiceRefillUseCase: ParseVoiceRefillUseCase,
    private val settingsPreferences: SettingsPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isVoiceAvailable: Boolean get() = speechRecognitionService.isAvailable()

    /**
     * Ends the recording by itself once cost, liters and distance have all been
     * heard, exactly as if the user had pressed Stop.
     */
    private val voiceAutoStopDetector = VoiceAutoStopDetector(
        scope = viewModelScope,
        parser = parseVoiceRefillUseCase
    ) { data ->
        Timber.d("Auto-stopping voice entry: all fields captured -> $data")
        speechRecognitionService.stopListeningManually()
    }

    private var carId: Long = -1L

    private val _carName = MutableStateFlow<String?>(null)
    val carName: StateFlow<String?> = _carName.asStateFlow()

    private val _allCars = MutableStateFlow<List<com.agcoding.cartrackingapp.domain.model.Car>>(emptyList())
    val allCars: StateFlow<List<com.agcoding.cartrackingapp.domain.model.Car>> = _allCars.asStateFlow()

    private val _selectedCar = MutableStateFlow<com.agcoding.cartrackingapp.domain.model.Car?>(null)
    val selectedCar: StateFlow<com.agcoding.cartrackingapp.domain.model.Car?> = _selectedCar.asStateFlow()

    // Voice recognition state
    private val _voiceState = MutableStateFlow<VoiceRefillState>(VoiceRefillState.Idle)
    val voiceState: StateFlow<VoiceRefillState> = _voiceState.asStateFlow()

    // Parsed voice data for pre-filling form
    private var parsedVoiceData: com.agcoding.cartrackingapp.domain.model.VoiceRefillData? = null

    fun setCarId(id: Long) {
        carId = id
    }

    fun loadCarName() {
        viewModelScope.launch {
            carRepository.getCarById(carId).collect { car ->
                _carName.value = car?.name
            }
        }
    }

    fun loadAllCars() {
        viewModelScope.launch {
            carRepository.getAllCars().collect { cars ->
                _allCars.value = cars
                // Auto-select logic priority:
                // 1. If carId was provided, use it
                // 2. If not, try to select default car
                // 3. If no default, select first car if only one exists
                if (cars.isNotEmpty()) {
                    if (carId != -1L) {
                        val car = cars.find { it.id == carId }
                        if (car != null) {
                            selectCar(car)
                        } else if (cars.size == 1) {
                            selectCar(cars.first())
                        }
                    } else {
                        // Try to select default car
                        val defaultCar = cars.find { it.isDefault }
                        if (defaultCar != null) {
                            selectCar(defaultCar)
                        } else if (cars.size == 1) {
                            selectCar(cars.first())
                        }
                    }
                }
            }
        }
    }

    fun selectCar(car: com.agcoding.cartrackingapp.domain.model.Car) {
        _selectedCar.value = car
        carId = car.id
        _carName.value = car.name
    }

    fun saveQuickRefill(
        liters: Double,
        cost: Double,
        distance: Double,
        timestamp: Long,
        onSuccess: (amount: Double, timestamp: Long) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Validate inputs using the same validator as the main app
                val validationResult = RefillValidator.validateRefill(
                    context = context,
                    liters = liters.toString(),
                    cost = cost.toString(),
                    distance = distance.toString()
                )

                if (!validationResult.isValid) {
                    Timber.w("Validation failed: ${validationResult.errors}")
                    onError()
                    return@launch
                }

                when (addFuelRefillUseCase(
                    carId = carId,
                    amountPaid = cost,
                    litersAdded = liters,
                    tripDistance = distance,
                    timestamp = timestamp,
                    location = null,
                    notes = null,
                )) {
                    is Result.Success -> {
                        QuickAddWidgetReceiver.updateWidgets(context)
                        onSuccess(cost, timestamp)
                    }
                    is Result.Error -> onError()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving refill")
                onError()
            }
        }
    }

    fun saveQuickExpense(
        cost: Double,
        category: String,
        notes: String?,
        timestamp: Long,
        onSuccess: (amount: Double, timestamp: Long) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Use the same UseCase as the main app to ensure consistency
                when (addExpenseUseCase(
                    carId = carId,
                    category = category,
                    amount = cost,
                    timestamp = timestamp,
                    notes = notes,
                )) {
                    is Result.Success -> {
                        QuickAddWidgetReceiver.updateWidgets(context)
                        onSuccess(cost, timestamp)
                    }
                    is Result.Error -> onError()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving expense")
                onError()
            }
        }
    }

    // ========== Voice Recognition Methods ==========

    /**
     * Start voice entry - begins speech recognition
     */
    fun startVoiceEntry() {
        Timber.d("Starting voice entry")
        voiceAutoStopDetector.reset()
        _voiceState.value = VoiceRefillState.Listening(partialText = "")

        viewModelScope.launch {
            speechRecognitionService.startListening().collect { event ->
                when (event) {
                    is SpeechRecognitionEvent.PartialResults -> {
                        Timber.d("Partial: ${event.text}")
                        // Also drives the auto-stop: recording ends on its own once
                        // all three fields have been heard and the reading settles.
                        val captured = voiceAutoStopDetector.onPartialTranscript(event.text)
                        _voiceState.value = VoiceRefillState.Listening(
                            partialText = event.text,
                            captured = captured
                        )
                    }
                    is SpeechRecognitionEvent.Results -> {
                        Timber.d("Final: ${event.text}")
                        voiceAutoStopDetector.reset()
                        parseVoiceTranscript(event.text, event.alternatives)
                    }
                    is SpeechRecognitionEvent.Error -> {
                        Timber.e("Error: ${event.message}")
                        voiceAutoStopDetector.reset()
                        _voiceState.value = VoiceRefillState.Error(event.message)
                    }
                    SpeechRecognitionEvent.ReadyForSpeech -> {
                        Timber.d("Ready for speech")
                    }
                    SpeechRecognitionEvent.EndOfSpeech -> {
                        Timber.d("End of speech")
                    }
                    else -> {
                        // Ignore other events (Starting, BeginningOfSpeech, VolumeChanged)
                    }
                }
            }
        }
    }

    /**
     * Stop voice recording manually
     * This triggers the final result processing and parsing
     */
    fun stopVoiceRecording() {
        Timber.d("Stopping voice recording manually")
        voiceAutoStopDetector.reset()
        speechRecognitionService.stopListeningManually()
    }

    /**
     * Cancel voice entry
     */
    fun cancelVoiceEntry() {
        Timber.d("Canceling voice entry")
        voiceAutoStopDetector.reset()
        speechRecognitionService.stopListening()
        _voiceState.value = VoiceRefillState.Idle
        parsedVoiceData = null
    }

    /**
     * Parse voice transcript using LLM or regex
     */
    private fun parseVoiceTranscript(
        transcript: String,
        alternatives: List<String> = emptyList()
    ) {
        Timber.d("Parsing transcript: '$transcript' (${alternatives.size} alternatives)")

        if (transcript.isBlank()) {
            _voiceState.value = VoiceRefillState.Error("No speech detected")
            return
        }

        _voiceState.value = VoiceRefillState.Processing(transcript)

        viewModelScope.launch {
            try {
                // Get the selected LLM model from preferences
                val selectedModel = settingsPreferences.llmModelFlow.first()
                Timber.d("Selected LLM model: ${selectedModel.displayName}")

                // Get OpenAI API key from BuildConfig
                val apiKey: String? = try {
                    BuildConfig.OPENAI_API_KEY.takeIf { it.isNotBlank() }
                } catch (e: Exception) {
                    null
                }

                val result = parseVoiceRefillUseCase(transcript, apiKey, selectedModel, alternatives)

                when (result) {
                    is VoiceParsingResult.Success -> {
                        Timber.d("Parse SUCCESS: ${result.data}")
                        parsedVoiceData = result.data
                        _voiceState.value = VoiceRefillState.Parsed(result.data)
                    }
                    is VoiceParsingResult.LowConfidence -> {
                        Timber.d("Low confidence: ${result.data}")
                        parsedVoiceData = result.data
                        _voiceState.value = VoiceRefillState.Parsed(result.data)
                    }
                    is VoiceParsingResult.Error -> {
                        Timber.e("Parse ERROR: ${result.message}")
                        _voiceState.value = VoiceRefillState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during parsing")
                _voiceState.value = VoiceRefillState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Confirm parsed voice data
     */
    fun confirmVoiceParsedData() {
        Timber.d("Confirming parsed data")
        _voiceState.value = VoiceRefillState.Idle
    }

    /**
     * Get parsed voice data
     */
    fun getParsedVoiceData(): com.agcoding.cartrackingapp.domain.model.VoiceRefillData? {
        return parsedVoiceData
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionService.stopListening()
    }
}
