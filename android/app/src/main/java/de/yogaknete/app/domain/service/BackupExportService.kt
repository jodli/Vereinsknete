package de.yogaknete.app.domain.service

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.yogaknete.app.domain.model.*
import de.yogaknete.app.domain.repository.*
import kotlinx.datetime.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupExportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileRepository: UserProfileRepository,
    private val studioRepository: StudioRepository,
    private val classRepository: YogaClassRepository,
    private val templateRepository: ClassTemplateRepository,
    private val invoiceRepository: InvoiceRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportFullBackup(): BackupResult {
        return try {
            // Collect all data
            val userProfile = userProfileRepository.getUserProfileOnce()
            val studios = studioRepository.getAllStudiosOnce()
            val classes = classRepository.getAllClassesOnce()
            val templates = templateRepository.getAllTemplatesOnce()
            val invoices = invoiceRepository.getAllInvoicesOnce()

            // Create backup data object
            val backupData = BackupData(
                version = BackupData.CURRENT_VERSION,
                exportDate = Clock.System.now(),
                appVersion = "1.0.0",
                userProfile = userProfile,
                studios = studios,
                classes = classes,
                templates = templates,
                invoices = invoices
            )

            // Convert to JSON
            val jsonString = json.encodeToString(backupData)
            
            // Generate filename with timestamp
            val timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .replace(":", "-")
                .replace(".", "-")
                .replace("T", "_")
            val fileName = "yogaknete_backup_$timestamp.json"
            
            // Save to Downloads folder
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToDownloadsMediaStore(jsonString, fileName)
            } else {
                saveToDownloadsLegacy(jsonString, fileName)
            }
            
            result
        } catch (e: Exception) {
            BackupResult.Error("Backup fehlgeschlagen: ${e.message}")
        }
    }

    private fun saveToDownloadsMediaStore(content: String, fileName: String): BackupResult {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return BackupResult.Error("Datei konnte nicht erstellt werden")

            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            BackupResult.Success(
                filePath = uri.toString(),
                fileName = fileName
            )
        } catch (e: Exception) {
            BackupResult.Error("Speichern fehlgeschlagen: ${e.message}")
        }
    }

    private fun saveToDownloadsLegacy(content: String, fileName: String): BackupResult {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            
            BackupResult.Success(
                filePath = file.absolutePath,
                fileName = fileName
            )
        } catch (e: Exception) {
            BackupResult.Error("Speichern fehlgeschlagen: ${e.message}")
        }
    }

    suspend fun getBackupMetadata(userProfile: UserProfile?): BackupMetadata {
        val studios = studioRepository.getAllStudiosOnce()
        val classes = classRepository.getAllClassesOnce()
        val templates = templateRepository.getAllTemplatesOnce()
        val invoices = invoiceRepository.getAllInvoicesOnce()

        return BackupMetadata(
            version = BackupData.CURRENT_VERSION,
            exportDate = Clock.System.now(),
            appVersion = "1.0.0",
            entryCount = BackupEntryCount(
                studios = studios.size,
                classes = classes.size,
                templates = templates.size,
                invoices = invoices.size
            )
        )
    }
}
