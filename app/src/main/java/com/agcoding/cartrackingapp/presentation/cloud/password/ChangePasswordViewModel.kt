package com.agcoding.cartrackingapp.presentation.cloud.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.cloud.domain.model.CloudError
import com.agcoding.cartrackingapp.cloud.domain.model.CloudException
import com.agcoding.cartrackingapp.cloud.domain.repository.CloudRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val password: String = "",
    val repeated: String = "",
    val passwordVisible: Boolean = false,
    val isBusy: Boolean = false,
    val error: CloudError? = null,
    /** Set when the two fields do not match; not a server error. */
    val mismatch: Boolean = false,
    val done: Boolean = false,
) {
    val canSubmit: Boolean
        get() = !isBusy && password.isNotBlank() && repeated.isNotBlank()
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val cloudRepository: CloudRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null, mismatch = false)
    }

    fun onRepeatedChange(value: String) {
        _uiState.value = _uiState.value.copy(repeated = value, error = null, mismatch = false)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        if (state.password != state.repeated) {
            _uiState.value = state.copy(mismatch = true)
            return
        }
        if (state.password.length < MIN_PASSWORD_LENGTH) {
            _uiState.value = state.copy(error = CloudError.WEAK_PASSWORD)
            return
        }

        _uiState.value = state.copy(isBusy = true, error = null)
        viewModelScope.launch {
            try {
                cloudRepository.changePassword(state.password)
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    password = "",
                    repeated = "",
                    done = true,
                )
            } catch (e: CloudException) {
                _uiState.value = _uiState.value.copy(isBusy = false, error = e.error)
            }
        }
    }

    fun consumeDone() {
        _uiState.value = _uiState.value.copy(done = false)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
