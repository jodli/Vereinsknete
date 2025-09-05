package de.yogaknete.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import de.yogaknete.app.presentation.theme.YogaKneteTheme
import de.yogaknete.app.presentation.screens.onboarding.OnboardingFlow
import de.yogaknete.app.presentation.screens.week.WeekViewScreen
import de.yogaknete.app.presentation.screens.templates.TemplateManagementScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YogaKneteTheme {
                var showOnboarding by remember { mutableStateOf(true) }
                var currentScreen by remember { mutableStateOf("week") }
                
                if (showOnboarding) {
                    OnboardingFlow(
                        onOnboardingComplete = {
                            showOnboarding = false
                        }
                    )
                } else {
                    when (currentScreen) {
                        "week" -> {
                            WeekViewScreen(
                                onNavigateToInvoice = {
                                    // TODO: Navigate to invoice screen
                                },
                                onNavigateToTemplates = {
                                    currentScreen = "templates"
                                }
                            )
                        }
                        "templates" -> {
                            TemplateManagementScreen(
                                onNavigateBack = {
                                    currentScreen = "week"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
