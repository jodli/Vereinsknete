package de.yogaknete.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.yogaknete.app.domain.model.Studio
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * Vorlage für wiederkehrende Kurse
 * Ermöglicht schnelles Erstellen von Kursen mit vordefinierten Daten
 */
@Serializable
@Entity(
    tableName = "class_templates",
    foreignKeys = [
        ForeignKey(
            entity = Studio::class,
            parentColumns = ["id"],
            childColumns = ["studioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studioId"]),
        Index(value = ["dayOfWeek"]),
        Index(value = ["isActive"])
    ]
)
data class ClassTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String, // z.B. "Montag Morgen Yoga Flow"
    val studioId: Long,
    val className: String, // z.B. "Vinyasa Flow"
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val duration: Double = 1.25, // Standard: 1.25 Stunden
    val isActive: Boolean = true
)
