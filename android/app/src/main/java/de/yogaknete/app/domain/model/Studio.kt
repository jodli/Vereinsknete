package de.yogaknete.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "studios")
data class Studio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val contactPerson: String = "",
    val email: String = "",
    val phone: String = "",
    val street: String = "",
    val postalCode: String = "",
    val city: String = "",
    val hourlyRate: Double, // EUR per hour
    val isActive: Boolean = true
)
