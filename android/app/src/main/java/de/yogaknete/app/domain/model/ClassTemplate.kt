package de.yogaknete.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Template for recurring yoga classes
 * Allows quick creation of classes with pre-filled data
 */
@Entity(
    tableName = "class_templates",
    foreignKeys = [
        ForeignKey(
            entity = Studio::class,
            parentColumns = ["id"],
            childColumns = ["studioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ClassTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,           // e.g., "Montag Abend Yoga"
    val studioId: Long,
    val className: String,      // e.g., "Hatha Yoga"
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationHours: Double = 1.25, // Default 1.25 hours (75 minutes)
    val isActive: Boolean = true,
    val autoSchedule: Boolean = false, // Auto-create weekly
    val lastScheduledDate: LocalDate? = null // Track last auto-scheduled date
)
