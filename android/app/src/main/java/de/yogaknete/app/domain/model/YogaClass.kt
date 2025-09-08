package de.yogaknete.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "yoga_classes",
    foreignKeys = [
        ForeignKey(
            entity = Studio::class,
            parentColumns = ["id"],
            childColumns = ["studioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studioId"])
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
    val notes: String = "",
    val creationSource: CreationSource = CreationSource.MANUAL,
    val sourceTemplateId: Long? = null // Link to template if auto-created or from template
)

@Serializable
enum class ClassStatus {
    SCHEDULED,      // Planned/upcoming
    COMPLETED,      // Successfully taught
    CANCELLED       // Cancelled/didn't happen
}

@Serializable
enum class CreationSource {
    MANUAL,         // User created directly
    TEMPLATE,       // Created from template (quick-add)
    AUTO           // Auto-scheduled from recurring template
}
