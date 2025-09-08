package de.yogaknete.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Long = 1L, // Single user profile
    val name: String,
    val street: String = "",
    val postalCode: String = "",
    val city: String = "",
    val taxId: String = "", // Steuernummer or USt-IdNr
    val phone: String = "",
    val email: String = "",
    val bankName: String = "",
    val iban: String = "",
    val bic: String = "", // Optional BIC/SWIFT code
    val defaultHourlyRate: Double, // Default hourly rate in EUR
    val isOnboardingComplete: Boolean = false
)
