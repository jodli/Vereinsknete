package de.yogaknete.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.yogaknete.app.presentation.theme.YogaKneteTheme
import de.yogaknete.app.presentation.screens.onboarding.OnboardingFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YogaKneteTheme {
                OnboardingFlow(
                    onOnboardingComplete = {
                        // TODO: Navigate to main app (Week View)
                    }
                )
            }
        }
    }
}
