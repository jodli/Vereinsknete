package de.yogaknete.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Navigation destinations
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object WeekView : Screen("week_view")
    object Profile : Screen("profile")
    object Studios : Screen("studios")
}

@Composable
fun YogaKneteNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            // TODO: Implement onboarding screen
            OnboardingPlaceholder()
        }
        
        composable(Screen.WeekView.route) {
            // TODO: Implement week view screen
            WeekViewPlaceholder()
        }
        
        composable(Screen.Profile.route) {
            // TODO: Implement profile screen
            ProfilePlaceholder()
        }
        
        composable(Screen.Studios.route) {
            // TODO: Implement studios screen
            StudiosPlaceholder()
        }
    }
}

// Placeholder composables - to be implemented in Week 1
@Composable
private fun OnboardingPlaceholder() {
    // Placeholder implementation
}

@Composable
private fun WeekViewPlaceholder() {
    // Placeholder implementation
}

@Composable
private fun ProfilePlaceholder() {
    // Placeholder implementation
}

@Composable
private fun StudiosPlaceholder() {
    // Placeholder implementation
}
