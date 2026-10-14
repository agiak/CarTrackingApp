package com.agcoding.cartrackingapp.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.presentation.cloud.auth.CloudAuthMode
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    onNavigateToAuth: (CloudAuthMode) -> Unit = {},
    openedByDeepLink: Boolean = false,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(openedByDeepLink) {
        if (openedByDeepLink) viewModel.onOpenedByDeepLink()
    }

    AnimatedContent(
        targetState = state,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "onboarding_transition"
    ) { currentState ->
        when (currentState) {
            is OnboardingState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is OnboardingState.ShowGuide -> {
                OnboardingGuideScreen(
                    viewModel = viewModel,
                    onSkip = {
                        viewModel.skipGuide()
                    },
                    onComplete = {
                        viewModel.nextSlide()
                    }
                )
            }

            is OnboardingState.ShowPermissions -> {
                PermissionsScreen(
                    viewModel = viewModel
                )
            }

            is OnboardingState.ShowTheme -> {
                OnboardingThemeScreen(
                    onContinue = {
                        viewModel.onThemeChosen()
                    }
                )
            }

            is OnboardingState.ShowAccountPrompt -> {
                OnboardingAccountPromptScreen(
                    // Told which door was taken before leaving, because the
                    // answer arrives back as a session, not as a result, and
                    // a sign-in and a sign-up continue differently.
                    onCreateAccount = {
                        viewModel.onAuthRequested(CloudAuthMode.SignUp)
                        onNavigateToAuth(CloudAuthMode.SignUp)
                    },
                    onSignIn = {
                        viewModel.onAuthRequested(CloudAuthMode.SignIn)
                        onNavigateToAuth(CloudAuthMode.SignIn)
                    },
                    onContinueOffline = { viewModel.continueOffline() },
                    dataSyncAvailable = viewModel.dataSyncAvailable,
                )
            }

            is OnboardingState.ShowImportPrompt -> {
                OnboardingImportPromptScreen(
                    onFinished = {
                        viewModel.onImportPromptFinished()
                    }
                )
            }

            is OnboardingState.Completed -> {
                // Navigate to home exactly once. Calling this straight from
                // composition re-fired it on every recomposition (and during the
                // AnimatedContent transition), stacking duplicate home entries on
                // the back stack — which is why the system Back button needed
                // several presses to exit the app (issue #18).
                LaunchedEffect(Unit) {
                    onOnboardingComplete()
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    CarTrackingAppTheme {
        OnboardingScreen(onOnboardingComplete = {})
    }
}

