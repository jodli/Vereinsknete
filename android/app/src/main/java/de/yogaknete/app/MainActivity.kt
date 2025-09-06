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
import de.yogaknete.app.presentation.screens.invoice.InvoiceListScreen
import de.yogaknete.app.presentation.screens.settings.UserProfileEditScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YogaKneteTheme {
                val navController = rememberNavController()
                var showOnboarding by remember { mutableStateOf(true) }
                
                if (showOnboarding) {
                    OnboardingFlow(
                        onOnboardingComplete = {
                            showOnboarding = false
                        }
                    )
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = "week"
                    ) {
                        composable("week") {
                            WeekViewScreen(
                                onNavigateToInvoice = {
                                    navController.navigate("invoices")
                                },
                                onNavigateToTemplates = {
                                    navController.navigate("templates")
                                },
                                onNavigateToProfile = {
                                    navController.navigate("profile_edit")
                                }
                            )
                        }
                        composable("templates") {
                            TemplateManagementScreen(
                                onNavigateBack = {
                                    navController.navigateUp()
                                }
                            )
                        }
                        composable("invoices") {
                            InvoiceListScreen(
                                navController = navController
                            )
                        }
                        composable("invoice_detail/{invoiceId}") { backStackEntry ->
                            // Placeholder for invoice detail/PDF generation screen (Sprint 3.2)
                            // val invoiceId = backStackEntry.arguments?.getString("invoiceId")
                        }
                        composable("profile_edit") {
                            UserProfileEditScreen(
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}
