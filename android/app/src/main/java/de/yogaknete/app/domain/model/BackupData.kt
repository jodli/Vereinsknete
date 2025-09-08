package de.yogaknete.app.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import de.yogaknete.app.data.local.entities.ClassTemplate

/**
 * Data class representing a complete backup of the app's database
 */
@Serializable
data class BackupData(
    val version: Int = 1,
    val exportDate: Instant,
    val appVersion: String = "1.0.0",
    val userProfile: UserProfile?,
    val studios: List<Studio>,
    val classes: List<YogaClass>,
    val templates: List<ClassTemplate>,
    val invoices: List<Invoice>
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class BackupMetadata(
    val version: Int,
    val exportDate: Instant,
    val appVersion: String,
    val entryCount: BackupEntryCount
)

@Serializable
data class BackupEntryCount(
    val studios: Int,
    val classes: Int,
    val templates: Int,
    val invoices: Int
)

sealed class BackupResult {
    data class Success(val filePath: String, val fileName: String) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class ImportResult {
    data class Success(val importedCount: BackupEntryCount) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

enum class ImportStrategy {
    REPLACE,  // Clear existing and import
    MERGE,    // Keep existing and add new
    SELECTIVE // User chooses what to import
}

sealed class ValidationResult {
    data class Valid(val metadata: BackupMetadata) : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}
