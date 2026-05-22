package com.agcoding.cartrackingapp.presentation.refilldetails

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.usecase.refill.DeleteRefillUseCase
import com.agcoding.cartrackingapp.domain.usecase.refill.GetRefillDetailsUseCase
import com.agcoding.cartrackingapp.domain.usecase.refill.RefillDetails
import com.agcoding.cartrackingapp.shared.domain.result.Result
import com.agcoding.cartrackingapp.util.GeocodingUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RefillDetailsUiState {
    object Loading : RefillDetailsUiState()
    data class Success(
        val details: RefillDetails,
        val addressString: String? = null
    ) : RefillDetailsUiState()
    data class Error(val message: String) : RefillDetailsUiState()
}

@HiltViewModel
class RefillDetailsViewModel @Inject constructor(
    private val getRefillDetailsUseCase: GetRefillDetailsUseCase,
    private val deleteRefillUseCase: DeleteRefillUseCase,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val refillId: Long = savedStateHandle.get<Long>("refillId") ?: 0L

    private val _uiState = MutableStateFlow<RefillDetailsUiState>(RefillDetailsUiState.Loading)
    val uiState: StateFlow<RefillDetailsUiState> = _uiState.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    init {
        loadRefillDetails()
    }

    private fun loadRefillDetails() {
        viewModelScope.launch {
            _uiState.value = RefillDetailsUiState.Loading

            getRefillDetailsUseCase(refillId)
                .catch { e ->
                    _uiState.value = RefillDetailsUiState.Error(
                        e.message ?: "Failed to load refill details"
                    )
                }
                .collect { details ->
                    if (details != null) {
                        _uiState.value = RefillDetailsUiState.Success(details)

                        // Fetch address if location is available
                        details.refill.location?.let { location ->
                            fetchAddress(location.latitude, location.longitude, details)
                        }
                    } else {
                        _uiState.value = RefillDetailsUiState.Error("Refill not found")
                    }
                }
        }
    }

    private fun fetchAddress(latitude: Double, longitude: Double, details: RefillDetails) {
        viewModelScope.launch {
            val address = GeocodingUtil.getAddressFromLocation(context, latitude, longitude)
            _uiState.value = RefillDetailsUiState.Success(details, address)
        }
    }

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun deleteRefill(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (deleteRefillUseCase(refillId)) {
                is Result.Success -> { hideDeleteDialog(); onSuccess() }
                is Result.Error -> hideDeleteDialog()
            }
        }
    }

    fun retry() {
        loadRefillDetails()
    }
}

