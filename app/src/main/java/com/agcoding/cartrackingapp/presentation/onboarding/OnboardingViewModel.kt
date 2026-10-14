package com.agcoding.cartrackingapp.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.cloud.domain.repository.CloudRepository
import com.agcoding.cartrackingapp.cloud.domain.sync.CloudSync
import com.agcoding.cartrackingapp.data.preferences.OnboardingPreferences
import com.agcoding.cartrackingapp.presentation.cloud.auth.CloudAuthMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The first-run sequence, in order:
 *
 *   guide → permissions → theme → account → import → done
 *
 * Import comes last on purpose. It is the only step that asks the user to go
 * and find a file, and nobody goes looking for a file for an app they have not
 * decided to keep yet. By this point they have seen what it does, granted what
 * it needs and made it look the way they want.
 */
sealed class OnboardingState {
    object Loading : OnboardingState()
    object ShowGuide : OnboardingState()
    object ShowPermissions : OnboardingState()

    /** Pick dark/light and a colour, before seeing any of the app. */
    object ShowTheme : OnboardingState()

    /** "With an account, or without one?" — see [OnboardingAccountPromptScreen]. */
    object ShowAccountPrompt : OnboardingState()
    object ShowImportPrompt : OnboardingState()
    object Completed : OnboardingState()
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
    private val cloudRepository: CloudRepository,
    cloudSync: CloudSync,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** Passed through to the prompt so it does not over-promise; see CloudSync. */
    val dataSyncAvailable: Boolean = cloudSync.movesData

    /**
     * The app was opened by tapping a notification or a widget, so it is on
     * its way somewhere specific. Asking about accounts in the middle of that
     * would be answering a question nobody asked — and it would leave the
     * prompt sitting underneath, waiting to appear when they press back.
     */
    private var openedByDeepLink = false

    /**
     * Whether this pass through the account step should go on to offer an
     * import.
     *
     * True on a genuine first run, and again after a sign-out, because both
     * are moments when the phone may be about to start from nothing. False for
     * the one-off account question shown to someone who installed the app
     * before accounts existed: that person is already using the app, and
     * dropping them into "do you have a backup?" would be asking about a
     * problem they do not have.
     */
    private var offerImport = false

    /**
     * Re-entered from the account screen after signing out, rather than opened
     * as the first screen of the app.
     *
     * Kept as a flag because [checkOnboardingStatus] is suspended on DataStore
     * while the screen asks for this, and either of the two can finish first.
     */
    private var restartAtAccount = false

    /**
     * Which button sent the user to the auth screen.
     *
     * Signing in means their data is already on the server and about to come
     * down, so offering to import a file afterwards would invite them to merge
     * a stale export into a fresh restore. Registering means the account starts
     * empty, which is exactly when a file is worth asking about.
     */
    private var authMode: CloudAuthMode? = null

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Loading)
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _currentSlideIndex = MutableStateFlow(0)
    val currentSlideIndex: StateFlow<Int> = _currentSlideIndex.asStateFlow()

    val slides = OnboardingContent.slides
    val permissions = AppPermissions.permissions

    init {
        checkOnboardingStatus()

        // Signing in happens on a screen of its own, which this one navigates
        // to and then waits on. Watching the session is how it finds out the
        // answer came back, rather than trying to hand a result back through
        // the navigation stack.
        viewModelScope.launch {
            cloudRepository.state.collect { cloud ->
                if (_state.value is OnboardingState.ShowAccountPrompt && cloud.isSignedIn) {
                    onAccountStepFinished()
                }
            }
        }
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            val isOnboardingCompleted = onboardingPreferences.isOnboardingCompleted.first()

            // The screen asked for the account step while this was waiting on
            // DataStore; that answer wins, it is the more specific one.
            if (restartAtAccount) return@launch

            _state.value = when {
                !isOnboardingCompleted -> {
                    offerImport = true
                    OnboardingState.ShowGuide
                }
                // Everyone gets asked about an account once, including people
                // who installed the app before there was one to offer.
                shouldAskAboutAccount() -> OnboardingState.ShowAccountPrompt
                else -> OnboardingState.Completed
            }
        }
    }

    private suspend fun shouldAskAboutAccount(): Boolean {
        if (openedByDeepLink) return false
        val cloud = cloudRepository.state.first()
        return !cloud.accountPromptAnswered && !cloud.isSignedIn
    }

    /**
     * Checked in both directions on purpose: this arrives from the first
     * composition while [checkOnboardingStatus] is suspended on DataStore, and
     * either of the two can finish first.
     */
    fun onOpenedByDeepLink() {
        openedByDeepLink = true
        if (_state.value is OnboardingState.ShowAccountPrompt) {
            completeOnboarding()
        }
    }

    /**
     * Signing out drops the user back here, at the question they answered when
     * they first installed: account, or no account?
     *
     * It is the same question because they are in the same position again, and
     * leaving them on an account screen that now describes no account would
     * make them go and find the way back in themselves. The import offer comes
     * after it for the same reason it does on a first run — a sign-out is the
     * one moment a user is most likely to be holding a file they want back.
     *
     * Overrides whatever [checkOnboardingStatus] decided, and is remembered so
     * that a decision still in flight cannot undo it.
     */
    fun restartAtAccountStep() {
        if (restartAtAccount) return
        restartAtAccount = true
        offerImport = true
        authMode = null
        _state.value = OnboardingState.ShowAccountPrompt
    }

    fun nextSlide() {
        val currentIndex = _currentSlideIndex.value
        if (currentIndex < slides.size - 1) {
            _currentSlideIndex.value = currentIndex + 1
        } else {
            // Last slide completed, go to permissions
            _state.value = OnboardingState.ShowPermissions
        }
    }

    fun previousSlide() {
        val currentIndex = _currentSlideIndex.value
        if (currentIndex > 0) {
            _currentSlideIndex.value = currentIndex - 1
        }
    }

    fun skipGuide() {
        _state.value = OnboardingState.ShowPermissions
    }

    fun goToSlide(index: Int) {
        if (index in slides.indices) {
            _currentSlideIndex.value = index
        }
    }

    fun onPermissionsHandled() {
        viewModelScope.launch {
            onboardingPreferences.setPermissionsRequested(true)
            _state.value = OnboardingState.ShowTheme
        }
    }

    /** The theme step has no wrong answer, so it has no skip — only "done". */
    fun onThemeChosen() {
        viewModelScope.launch {
            if (shouldAskAboutAccount()) {
                _state.value = OnboardingState.ShowAccountPrompt
            } else {
                continueAfterAccount()
            }
        }
    }

    /** Remembered before navigating away, so the return can be routed. */
    fun onAuthRequested(mode: CloudAuthMode) {
        authMode = mode
    }

    /** "Continue without an account" — remembered, so it is asked only once. */
    fun continueOffline() {
        viewModelScope.launch {
            cloudRepository.markAccountPromptAnswered()
            authMode = null
            onAccountStepFinished()
        }
    }

    private fun onAccountStepFinished() {
        // A sign-in is a restore; the server is about to fill this phone, and
        // an import on top of that is a way to end up with everything twice.
        if (authMode == CloudAuthMode.SignIn) {
            completeOnboarding()
            return
        }
        continueAfterAccount()
    }

    /** See [offerImport] for who gets the import step and who does not. */
    private fun continueAfterAccount() {
        if (offerImport) {
            _state.value = OnboardingState.ShowImportPrompt
        } else {
            completeOnboarding()
        }
    }

    /** The import step is the last one; there is nothing after it to ask. */
    fun onImportPromptFinished() {
        completeOnboarding()
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted(true)
            _state.value = OnboardingState.Completed
        }
    }
}
