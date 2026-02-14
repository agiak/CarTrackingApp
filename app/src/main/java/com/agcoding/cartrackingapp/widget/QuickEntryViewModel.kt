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
import com.agcoding.cartrackingapp.domain.usecase.voice.ParseVoiceRefillUseCase
import com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val speechRecognitionService: SpeechRecognitionService,
    private val parseVoiceRefillUseCase: ParseVoiceRefillUseCase,
    private val settingsPreferences: SettingsPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

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
                // Auto-select if only one car or if carId was provided
                if (cars.isNotEmpty()) {
                    if (carId != -1L) {
                        val car = cars.find { it.id == carId }
                        if (car != null) {
                            selectCar(car)
                        } else if (cars.size == 1) {
                            selectCar(cars.first())
                        }
                    } else if (cars.size == 1) {
                        selectCar(cars.first())
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
                val refill = FuelRefill(
                    carId = carId,
                    litersAdded = liters,
                    amountPaid = cost,
                    pricePerLiter = cost / liters,
                    tripDistance = distance,
                    odometerReading = 0.0,
                    fuelConsumption = if (distance > 0) (liters / distance) * 100 else 0.0,
                    timestamp = timestamp
                )
                refillRepository.insertRefill(refill)
                onSuccess(cost, timestamp)
            } catch (e: Exception) {
                e.printStackTrace()
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
                val expense = Expense(
                    carId = carId,
                    category = category,
                    amount = cost,
                    notes = notes,
                    timestamp = timestamp
                )
                expenseRepository.insertExpense(expense)
                onSuccess(cost, timestamp)
            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }
    }

    // ========== Voice Recognition Methods ==========

    /**
     * Start voice entry - begins speech recognition
     */
    fun startVoiceEntry() {
        android.util.Log.d("QuickVoiceEntry", "Starting voice entry")
        _voiceState.value = VoiceRefillState.Listening(partialText = "")

        viewModelScope.launch {
            speechRecognitionService.startListening().collect { event ->
                when (event) {
                    is SpeechRecognitionEvent.PartialResults -> {
                        android.util.Log.d("QuickVoiceEntry", "Partial: ${event.text}")
                        _voiceState.value = VoiceRefillState.Listening(partialText = event.text)
                    }
                    is SpeechRecognitionEvent.Results -> {
                        android.util.Log.d("QuickVoiceEntry", "Final: ${event.text}")
                        parseVoiceTranscript(event.text)
                    }
                    is SpeechRecognitionEvent.Error -> {
                        android.util.Log.e("QuickVoiceEntry", "Error: ${event.message}")
                        _voiceState.value = VoiceRefillState.Error(event.message)
                    }
                    SpeechRecognitionEvent.ReadyForSpeech -> {
                        android.util.Log.d("QuickVoiceEntry", "Ready for speech")
                    }
                    SpeechRecognitionEvent.EndOfSpeech -> {
                        android.util.Log.d("QuickVoiceEntry", "End of speech")
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
        android.util.Log.d("QuickVoiceEntry", "Stopping voice recording manually")
        speechRecognitionService.stopListeningManually()
    }

    /**
     * Cancel voice entry
     */
    fun cancelVoiceEntry() {
        android.util.Log.d("QuickVoiceEntry", "Canceling voice entry")
        speechRecognitionService.stopListening()
        _voiceState.value = VoiceRefillState.Idle
        parsedVoiceData = null
    }

    /**
     * Parse voice transcript using LLM or regex
     */
    private fun parseVoiceTranscript(transcript: String) {
        android.util.Log.d("QuickVoiceEntry", "Parsing transcript: '$transcript'")

        if (transcript.isBlank()) {
            _voiceState.value = VoiceRefillState.Error("No speech detected")
            return
        }

        _voiceState.value = VoiceRefillState.Processing(transcript)

        viewModelScope.launch {
            try {
                // Get the selected LLM model from preferences
                val selectedModel = settingsPreferences.llmModelFlow.first()
                android.util.Log.d("QuickVoiceEntry", "Selected LLM model: ${selectedModel.displayName}")

                // Get OpenAI API key from BuildConfig
                val apiKey: String? = try {
                    BuildConfig.OPENAI_API_KEY.takeIf { it.isNotBlank() }
                } catch (e: Exception) {
                    null
                }

                val result = parseVoiceRefillUseCase(transcript, apiKey, selectedModel)

                when (result) {
                    is VoiceParsingResult.Success -> {
                        android.util.Log.d("QuickVoiceEntry", "Parse SUCCESS: ${result.data}")
                        parsedVoiceData = result.data
                        _voiceState.value = VoiceRefillState.Parsed(result.data)
                    }
                    is VoiceParsingResult.LowConfidence -> {
                        android.util.Log.d("QuickVoiceEntry", "Low confidence: ${result.data}")
                        parsedVoiceData = result.data
                        _voiceState.value = VoiceRefillState.Parsed(result.data)
                    }
                    is VoiceParsingResult.Error -> {
                        android.util.Log.e("QuickVoiceEntry", "Parse ERROR: ${result.message}")
                        _voiceState.value = VoiceRefillState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("QuickVoiceEntry", "Exception during parsing: ${e.message}", e)
                _voiceState.value = VoiceRefillState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Confirm parsed voice data
     */
    fun confirmVoiceParsedData() {
        android.util.Log.d("QuickVoiceEntry", "Confirming parsed data")
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
