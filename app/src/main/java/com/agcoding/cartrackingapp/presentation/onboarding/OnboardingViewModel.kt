package com.agcoding.cartrackingapp.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.cloud.domain.repository.CloudRepository
import com.agcoding.cartrackingapp.cloud.domain.sync.CloudSync
import com.agcoding.cartrackingapp.data.preferences.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OnboardingState {
    object Loading : OnboardingState()
    object ShowGuide : OnboardingState()
    object ShowPermissions : OnboardingState()
    object ShowImportPrompt : OnboardingState()

    /** "With an account, or without one?" — see [OnboardingAccountPromptScreen]. */
    object ShowAccountPrompt : OnboardingState()
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
                    completeOnboarding()
                }
            }
        }
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            val isOnboardingCompleted = onboardingPreferences.isOnboardingCompleted.first()

            _state.value = when {
                !isOnboardingCompleted -> OnboardingState.ShowGuide
                // Everyone gets asked about an account once, including people
                // who installed the app before there was one to offer.
                shouldAskAboutAccount() -> OnboardingState.ShowAccountPrompt
                else -> OnboardingState.Completed
            }
        }
    }

    private suspend fun shouldAskAboutAccount(): Boolean {
        val cloud = cloudRepository.state.first()
        return !cloud.accountPromptAnswered && !cloud.isSignedIn
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
            // After permissions we ask returning users whether they want to import
            // existing data before finishing onboarding.
            _state.value = OnboardingState.ShowImportPrompt
        }
    }

    /**
     * The import step is done. The account question comes after it, so the
     * user has already seen what the app does before being asked whether they
     * want it backed up.
     */
    fun onImportPromptFinished() {
        viewModelScope.launch {
            if (shouldAskAboutAccount()) {
                _state.value = OnboardingState.ShowAccountPrompt
            } else {
                completeOnboarding()
            }
        }
    }

    /** "Continue without an account" — remembered, so it is asked only once. */
    fun continueOffline() {
        viewModelScope.launch {
            cloudRepository.markAccountPromptAnswered()
            completeOnboarding()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted(true)
            _state.value = OnboardingState.Completed
        }
    }
}

