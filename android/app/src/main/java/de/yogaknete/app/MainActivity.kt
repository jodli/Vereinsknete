package de.yogaknete.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import de.yogaknete.app.presentation.theme.YogaKneteTheme
import de.yogaknete.app.presentation.screens.onboarding.OnboardingFlow
import de.yogaknete.app.presentation.screens.week.WeekViewScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YogaKneteTheme {
                var showOnboarding by remember { mutableStateOf(true) }
                
                if (showOnboarding) {
                    OnboardingFlow(
                        onOnboardingComplete = {
                            showOnboarding = false
                        }
                    )
                } else {
                    WeekViewScreen(
                        onNavigateToInvoice = {
                            // TODO: Navigate to invoice screen
                        }
                    )
                }
            }
        }
    }
}
