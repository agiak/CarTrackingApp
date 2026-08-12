package com.agcoding.cartrackingapp.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    object Completed : OnboardingState()
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Loading)
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _currentSlideIndex = MutableStateFlow(0)
    val currentSlideIndex: StateFlow<Int> = _currentSlideIndex.asStateFlow()

    val slides = OnboardingContent.slides
    val permissions = AppPermissions.permissions

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            val isOnboardingCompleted = onboardingPreferences.isOnboardingCompleted.first()

            if (isOnboardingCompleted) {
                _state.value = OnboardingState.Completed
            } else {
                _state.value = OnboardingState.ShowGuide
            }
        }
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

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted(true)
            _state.value = OnboardingState.Completed
        }
    }
}

