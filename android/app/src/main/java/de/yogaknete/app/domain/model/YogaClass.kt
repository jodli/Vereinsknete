package de.yogaknete.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "yoga_classes",
    foreignKeys = [
        ForeignKey(
            entity = Studio::class,
            parentColumns = ["id"],
            childColumns = ["studioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class YogaClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studioId: Long,
    val title: String, // e.g., "Hatha Yoga", "Yoga für Anfänger"
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val durationHours: Double, // Calculated duration in hours
    val status: ClassStatus = ClassStatus.SCHEDULED,
    val notes: String = ""
)

enum class ClassStatus {
    SCHEDULED,      // Planned/upcoming
    COMPLETED,      // Successfully taught
    CANCELLED       // Cancelled/didn't happen
}
