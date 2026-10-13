package com.agcoding.cartrackingapp.presentation.cloud.auth

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
import java.util.Locale
import javax.inject.Inject

enum class CloudAuthMode { SignIn, SignUp }

/** What the screen should do once the call came back. */
enum class CloudAuthOutcome {
    /** Straight in. Close the screen. */
    SignedIn,

    /** Signed in with the emailed temporary password; go pick a real one. */
    MustChangePassword,
}

data class CloudAuthUiState(
    val mode: CloudAuthMode = CloudAuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val passwordVisible: Boolean = false,
    val isBusy: Boolean = false,
    val error: CloudError? = null,
    val outcome: CloudAuthOutcome? = null,

    val showResetDialog: Boolean = false,
    val resetEmail: String = "",
    val isSendingReset: Boolean = false,
    val resetSent: Boolean = false,
) {
    val canSubmit: Boolean
        get() = !isBusy && email.isNotBlank() && password.isNotBlank()
}

@HiltViewModel
class CloudAuthViewModel @Inject constructor(
    private val cloudRepository: CloudRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudAuthUiState())
    val uiState: StateFlow<CloudAuthUiState> = _uiState.asStateFlow()

    fun setMode(mode: CloudAuthMode) {
        _uiState.value = _uiState.value.copy(mode = mode, error = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun onDisplayNameChange(value: String) {
        _uiState.value = _uiState.value.copy(displayName = value)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        // Checked here rather than left to the server: a round trip to be told
        // the email has no "@" in it is a round trip wasted, and on a bad
        // connection it is a long one.
        val localProblem = when {
            !state.email.contains('@') -> CloudError.INVALID_EMAIL
            state.password.length < MIN_PASSWORD_LENGTH -> CloudError.WEAK_PASSWORD
            else -> null
        }
        // Only on sign-up: an existing account may well predate this rule, and
        // refusing to let someone sign in with their own password would be
        // absurd.
        if (localProblem != null &&
            (state.mode == CloudAuthMode.SignUp || localProblem == CloudError.INVALID_EMAIL)
        ) {
            _uiState.value = state.copy(error = localProblem)
            return
        }

        _uiState.value = state.copy(isBusy = true, error = null)
        viewModelScope.launch {
            try {
                when (state.mode) {
                    CloudAuthMode.SignIn -> cloudRepository.signIn(state.email, state.password)
                    CloudAuthMode.SignUp -> cloudRepository.signUp(
                        email = state.email,
                        password = state.password,
                        displayName = state.displayName,
                    )
                }
                val mustChange = cloudRepository.requireSession().mustChangePassword
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    password = "",
                    outcome = if (mustChange) {
                        CloudAuthOutcome.MustChangePassword
                    } else {
                        CloudAuthOutcome.SignedIn
                    }
                )
            } catch (e: CloudException) {
                _uiState.value = _uiState.value.copy(isBusy = false, error = e.error)
            }
        }
    }

    // ── forgotten password ────────────────────────────────────────────────

    fun openResetDialog() {
        _uiState.value = _uiState.value.copy(
            showResetDialog = true,
            resetEmail = _uiState.value.email,
            resetSent = false,
            error = null,
        )
    }

    fun closeResetDialog() {
        _uiState.value = _uiState.value.copy(showResetDialog = false, isSendingReset = false)
    }

    fun onResetEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(resetEmail = value, error = null)
    }

    fun sendReset() {
        val state = _uiState.value
        if (state.isSendingReset || !state.resetEmail.contains('@')) {
            _uiState.value = state.copy(error = CloudError.INVALID_EMAIL)
            return
        }

        _uiState.value = state.copy(isSendingReset = true, error = null)
        viewModelScope.launch {
            try {
                // The mail is written in the app's language, which after
                // AppCompatDelegate.setApplicationLocales is what the default
                // locale reports.
                cloudRepository.requestPasswordReset(
                    email = state.resetEmail,
                    language = Locale.getDefault().language.lowercase(Locale.ROOT),
                )
                _uiState.value = _uiState.value.copy(
                    isSendingReset = false,
                    showResetDialog = false,
                    resetSent = true,
                    email = state.resetEmail,
                )
            } catch (e: CloudException) {
                _uiState.value = _uiState.value.copy(isSendingReset = false, error = e.error)
            }
        }
    }

    fun consumeOutcome() {
        _uiState.value = _uiState.value.copy(outcome = null)
    }

    fun consumeResetSent() {
        _uiState.value = _uiState.value.copy(resetSent = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private companion object {
        /** Supabase's own minimum, and the one the signup function enforces. */
        const val MIN_PASSWORD_LENGTH = 6
    }
}
