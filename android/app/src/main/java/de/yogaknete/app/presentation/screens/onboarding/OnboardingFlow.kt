package de.yogaknete.app.presentation.screens.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.presentation.components.ImportPreviewDialog

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
    val restoreState by viewModel.restoreState.collectAsState()
    val previewMetadata by viewModel.previewMetadata.collectAsState()
    val restoreErrorMessage by viewModel.restoreErrorMessage.collectAsState()

    // File picker for backup restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onBackupFileSelected(it) }
    }

    // Check if onboarding is already complete
    LaunchedEffect(onboardingComplete) {
        if (onboardingComplete) {
            onOnboardingComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentStep) {
            OnboardingStep.WELCOME -> {
                WelcomeScreen(
                    onContinue = {
                        currentStep = OnboardingStep.USER_PROFILE
                    },
                    onRestoreBackup = {
                        filePickerLauncher.launch("application/json")
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

        // Loading overlay for restore
        if (restoreState == RestoreState.PROCESSING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Bitte warten...")
                    }
                }
            }
        }
    }

    // Import preview dialog
    if (restoreState == RestoreState.PREVIEW) {
        previewMetadata?.let { metadata ->
            ImportPreviewDialog(
                metadata = metadata,
                onConfirm = { strategy -> viewModel.confirmRestore(strategy) },
                onDismiss = { viewModel.cancelRestore() }
            )
        }
    }

    // Error dialog
    if (restoreState == RestoreState.ERROR) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportError() },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Fehler") },
            text = { Text(restoreErrorMessage ?: "Unbekannter Fehler") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissImportError() }) {
                    Text("OK")
                }
            }
        )
    }

    // Show loading overlay if needed
    if (isLoading) {
        // You can add a loading overlay here if desired
    }
}
