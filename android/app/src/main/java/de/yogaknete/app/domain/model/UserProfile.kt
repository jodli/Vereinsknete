package de.yogaknete.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Long = 1L, // Single user profile
    val name: String,
    val defaultHourlyRate: Double, // Default hourly rate in EUR
    val isOnboardingComplete: Boolean = false
)
