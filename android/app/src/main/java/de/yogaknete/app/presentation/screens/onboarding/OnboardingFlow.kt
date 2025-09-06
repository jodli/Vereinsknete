package de.yogaknete.app.presentation.screens.onboarding

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import de.yogaknete.app.domain.model.UserProfile

enum class OnboardingStep {
    WELCOME,
    USER_PROFILE,
    STUDIOS
}

@Composable
fun OnboardingFlow(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val onboardingComplete by viewModel.onboardingComplete.collectAsState()
    
    // Check if onboarding is already complete
    LaunchedEffect(onboardingComplete) {
        if (onboardingComplete) {
            onOnboardingComplete()
        }
    }
    
    when (currentStep) {
        OnboardingStep.WELCOME -> {
            WelcomeScreen(
                onContinue = {
                    currentStep = OnboardingStep.USER_PROFILE
                }
            )
        }
        
        OnboardingStep.USER_PROFILE -> {
            UserProfileSetupScreen(
                onContinue = { profile ->
                    userProfile = profile
                    currentStep = OnboardingStep.STUDIOS
                },
                onBack = {
                    currentStep = OnboardingStep.WELCOME
                }
            )
        }
        
        OnboardingStep.STUDIOS -> {
            StudioAdditionScreen(
                defaultHourlyRate = userProfile?.defaultHourlyRate ?: 30.0,
                onContinue = { studios ->
                    userProfile?.let { profile ->
                        viewModel.completeOnboarding(
                            userProfile = profile,
                            studios = studios
                        )
                    }
                },
                onBack = {
                    currentStep = OnboardingStep.USER_PROFILE
                }
            )
        }
    }
    
    // Show loading overlay if needed
    if (isLoading) {
        // You can add a loading overlay here if desired
    }
}
