package com.agcoding.cartrackingapp.presentation.cloud.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.cloud.domain.model.CloudError
import com.agcoding.cartrackingapp.cloud.domain.model.CloudException
import com.agcoding.cartrackingapp.cloud.domain.model.CloudMode
import com.agcoding.cartrackingapp.cloud.domain.model.CloudState
import com.agcoding.cartrackingapp.cloud.domain.model.SyncProgress
import com.agcoding.cartrackingapp.cloud.domain.repository.CloudRepository
import com.agcoding.cartrackingapp.cloud.domain.sync.CloudSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which confirmation dialog, if any, is open. */
enum class CloudAccountDialog { SignOut, DeleteAccount }

data class CloudAccountUiState(
    val cloudState: CloudState = CloudState(),
    val mode: CloudMode = CloudMode.OFFLINE_MODE,
    val displayName: String? = null,
    val progress: SyncProgress? = null,
    val isBusy: Boolean = false,
    val dialog: CloudAccountDialog? = null,
    /** The "also wipe this phone" tick inside the open dialog. */
    val wipeLocalData: Boolean = false,
    val error: CloudError? = null,
    /** A finished action worth a snackbar; consumed by the screen. */
    val message: CloudAccountMessage? = null,
    /** See [CloudSync.movesData]. */
    val dataSyncAvailable: Boolean = true,
)

enum class CloudAccountMessage { SignedOut, AccountDeleted, Synced, NothingToSync }

@HiltViewModel
class CloudAccountViewModel @Inject constructor(
    private val cloudRepository: CloudRepository,
    private val cloudSync: CloudSync,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CloudAccountUiState(dataSyncAvailable = cloudSync.movesData)
    )
    val uiState: StateFlow<CloudAccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cloudRepository.state.collect { state ->
                _uiState.value = _uiState.value.copy(cloudState = state)
                if (state.isSignedIn && _uiState.value.displayName == null) loadDisplayName()
            }
        }
        viewModelScope.launch {
            cloudRepository.mode.collect { mode ->
                _uiState.value = _uiState.value.copy(mode = mode)
            }
        }
        viewModelScope.launch {
            cloudSync.progress.collect { progress ->
                _uiState.value = _uiState.value.copy(progress = progress)
            }
        }
    }

    private fun loadDisplayName() {
        viewModelScope.launch {
            // Cosmetic: the screen is perfectly usable showing only the email,
            // so a failure here is not worth an error message.
            runCatching { cloudRepository.displayName() }.getOrNull()?.let { name ->
                _uiState.value = _uiState.value.copy(displayName = name)
            }
        }
    }

    fun syncNow() {
        if (_uiState.value.isBusy) return
        _uiState.value = _uiState.value.copy(isBusy = true, error = null)
        viewModelScope.launch {
            try {
                val summary = cloudSync.syncNow()
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    message = if (summary.changedAnything) {
                        CloudAccountMessage.Synced
                    } else {
                        CloudAccountMessage.NothingToSync
                    }
                )
            } catch (e: CloudException) {
                _uiState.value = _uiState.value.copy(isBusy = false, error = e.error)
            }
        }
    }

    fun openDialog(dialog: CloudAccountDialog) {
        _uiState.value = _uiState.value.copy(dialog = dialog, wipeLocalData = false, error = null)
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(dialog = null, wipeLocalData = false)
    }

    fun setWipeLocalData(wipe: Boolean) {
        _uiState.value = _uiState.value.copy(wipeLocalData = wipe)
    }

    fun confirmSignOut() = runAccountAction(CloudAccountMessage.SignedOut) { wipe ->
        cloudRepository.signOut(wipeLocalData = wipe)
    }

    fun confirmDeleteAccount() = runAccountAction(CloudAccountMessage.AccountDeleted) { wipe ->
        cloudRepository.deleteAccount(wipeLocalData = wipe)
    }

    /**
     * Sign-out and delete-account differ by one call. Both close the dialog
     * only on success — a sign-out refused because of unsent changes has to
     * leave the user looking at the reason, not back on the previous screen.
     */
    private fun runAccountAction(
        success: CloudAccountMessage,
        action: suspend (wipe: Boolean) -> Unit,
    ) {
        val wipe = _uiState.value.wipeLocalData
        _uiState.value = _uiState.value.copy(isBusy = true, error = null)
        viewModelScope.launch {
            try {
                action(wipe)
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    dialog = null,
                    wipeLocalData = false,
                    displayName = null,
                    message = success,
                )
            } catch (e: CloudException) {
                _uiState.value = _uiState.value.copy(isBusy = false, error = e.error)
            }
        }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
