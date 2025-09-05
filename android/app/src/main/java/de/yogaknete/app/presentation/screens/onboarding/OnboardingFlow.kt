package de.yogaknete.app.presentation.screens.onboarding

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel

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
    var userName by remember { mutableStateOf("") }
    var userHourlyRate by remember { mutableStateOf(0.0) }
    
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
                onContinue = { name, hourlyRate ->
                    userName = name
                    userHourlyRate = hourlyRate
                    currentStep = OnboardingStep.STUDIOS
                },
                onBack = {
                    currentStep = OnboardingStep.WELCOME
                }
            )
        }
        
        OnboardingStep.STUDIOS -> {
            StudioAdditionScreen(
                defaultHourlyRate = userHourlyRate,
                onContinue = { studios ->
                    viewModel.completeOnboarding(
                        userName = userName,
                        userHourlyRate = userHourlyRate,
                        studios = studios
                    )
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
