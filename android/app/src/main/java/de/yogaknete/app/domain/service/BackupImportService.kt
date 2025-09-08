package de.yogaknete.app.domain.service

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import de.yogaknete.app.domain.model.*
import de.yogaknete.app.domain.repository.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupImportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileRepository: UserProfileRepository,
    private val studioRepository: StudioRepository,
    private val classRepository: YogaClassRepository,
    private val templateRepository: ClassTemplateRepository,
    private val invoiceRepository: InvoiceRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun validateBackup(uri: Uri): ValidationResult {
        return try {
            val jsonContent = readFileContent(uri)
            val backupData = json.decodeFromString<BackupData>(jsonContent)
            
            // Validate version compatibility
            if (backupData.version > BackupData.CURRENT_VERSION) {
                return ValidationResult.Invalid(
                    "Backup-Version ${backupData.version} ist neuer als unterstützte Version ${BackupData.CURRENT_VERSION}"
                )
            }
            
            val metadata = BackupMetadata(
                version = backupData.version,
                exportDate = backupData.exportDate,
                appVersion = backupData.appVersion,
                entryCount = BackupEntryCount(
                    studios = backupData.studios.size,
                    classes = backupData.classes.size,
                    templates = backupData.templates.size,
                    invoices = backupData.invoices.size
                )
            )
            
            ValidationResult.Valid(metadata)
        } catch (e: Exception) {
            ValidationResult.Invalid("Ungültige Backup-Datei: ${e.message}")
        }
    }

    suspend fun importBackup(
        uri: Uri,
        strategy: ImportStrategy = ImportStrategy.MERGE
    ): ImportResult {
        return try {
            val jsonContent = readFileContent(uri)
            val backupData = json.decodeFromString<BackupData>(jsonContent)
            
            when (strategy) {
                ImportStrategy.REPLACE -> replaceAllData(backupData)
                ImportStrategy.MERGE -> mergeData(backupData)
                ImportStrategy.SELECTIVE -> selectiveImport(backupData)
            }
        } catch (e: Exception) {
            ImportResult.Error("Import fehlgeschlagen: ${e.message}")
        }
    }

    private suspend fun replaceAllData(backupData: BackupData): ImportResult {
        return try {
            // Clear existing data
            invoiceRepository.deleteAllInvoices()
            classRepository.deleteAllClasses()
            templateRepository.deleteAllTemplates()
            studioRepository.deleteAllStudios()
            
            // Import new data
            backupData.userProfile?.let { userProfileRepository.insertOrUpdate(it) }
            
            // Import studios first (referenced by classes and templates)
            backupData.studios.forEach { studio ->
                studioRepository.insertStudio(studio)
            }
            
            // Import templates
            backupData.templates.forEach { template ->
                templateRepository.insertTemplate(template)
            }
            
            // Import classes
            backupData.classes.forEach { yogaClass ->
                classRepository.insertClass(yogaClass)
            }
            
            // Import invoices
            backupData.invoices.forEach { invoice ->
                invoiceRepository.insertInvoice(invoice)
            }
            
            ImportResult.Success(
                BackupEntryCount(
                    studios = backupData.studios.size,
                    classes = backupData.classes.size,
                    templates = backupData.templates.size,
                    invoices = backupData.invoices.size
                )
            )
        } catch (e: Exception) {
            ImportResult.Error("Ersetzen fehlgeschlagen: ${e.message}")
        }
    }

    private suspend fun mergeData(backupData: BackupData): ImportResult {
        return try {
            var importedStudios = 0
            var importedClasses = 0
            var importedTemplates = 0
            var importedInvoices = 0
            
            // Merge user profile (overwrite existing)
            backupData.userProfile?.let { userProfileRepository.insertOrUpdate(it) }
            
            // Get existing data for comparison
            val existingStudios = studioRepository.getAllStudiosOnce().associateBy { it.name }
            val existingTemplates = templateRepository.getAllTemplatesOnce().associateBy { it.name }
            val existingInvoices = invoiceRepository.getAllInvoicesOnce().associateBy { it.invoiceNumber }
            
            // Merge studios (skip duplicates by name)
            backupData.studios.forEach { studio ->
                if (!existingStudios.containsKey(studio.name)) {
                    studioRepository.insertStudio(studio.copy(id = 0))
                    importedStudios++
                }
            }
            
            // Merge templates (skip duplicates by name)
            backupData.templates.forEach { template ->
                if (!existingTemplates.containsKey(template.name)) {
                    // Need to map studio ID if it changed
                    val mappedTemplate = template.copy(id = 0)
                    templateRepository.insertTemplate(mappedTemplate)
                    importedTemplates++
                }
            }
            
            // Import all classes (they are unique by date/time)
            backupData.classes.forEach { yogaClass ->
                classRepository.insertClass(yogaClass.copy(id = 0))
                importedClasses++
            }
            
            // Merge invoices (skip duplicates by invoice number)
            backupData.invoices.forEach { invoice ->
                if (!existingInvoices.containsKey(invoice.invoiceNumber)) {
                    invoiceRepository.insertInvoice(invoice.copy(id = 0))
                    importedInvoices++
                }
            }
            
            ImportResult.Success(
                BackupEntryCount(
                    studios = importedStudios,
                    classes = importedClasses,
                    templates = importedTemplates,
                    invoices = importedInvoices
                )
            )
        } catch (e: Exception) {
            ImportResult.Error("Zusammenführen fehlgeschlagen: ${e.message}")
        }
    }

    private suspend fun selectiveImport(backupData: BackupData): ImportResult {
        // For now, just do a merge - in the future this could show a UI for selection
        return mergeData(backupData)
    }

    private fun readFileContent(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Datei konnte nicht geöffnet werden")
        
        return BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.readText()
        }
    }

    suspend fun getBackupPreview(uri: Uri): BackupData? {
        return try {
            val jsonContent = readFileContent(uri)
            json.decodeFromString<BackupData>(jsonContent)
        } catch (e: Exception) {
            null
        }
    }
}
