package de.yogaknete.app.presentation.screens.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yogaknete.app.domain.model.*
import de.yogaknete.app.domain.service.BackupExportService
import de.yogaknete.app.domain.service.BackupImportService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupExportService: BackupExportService,
    private val backupImportService: BackupImportService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()
    
    fun exportBackup(format: BackupFormat) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            val result = when (format) {
                BackupFormat.JSON -> backupExportService.exportFullBackup()
                BackupFormat.ZIP -> backupExportService.exportFullBackup() // TODO: Implement ZIP
            }
            
            when (result) {
                is BackupResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        lastBackupResult = result,
                        showSuccessMessage = true,
                        successMessage = "Backup gespeichert: ${result.fileName}"
                    )
                }
                is BackupResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        showErrorMessage = true,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
    
    fun validateAndPreviewImport(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            when (val validationResult = backupImportService.validateBackup(uri)) {
                is ValidationResult.Valid -> {
                    val backupData = backupImportService.getBackupPreview(uri)
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        showImportPreview = true,
                        previewData = backupData,
                        previewMetadata = validationResult.metadata,
                        selectedImportUri = uri
                    )
                }
                is ValidationResult.Invalid -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        showErrorMessage = true,
                        errorMessage = validationResult.reason
                    )
                }
            }
        }
    }
    
    fun confirmImport(strategy: ImportStrategy) {
        val uri = _uiState.value.selectedImportUri ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                showImportPreview = false
            )
            
            when (val result = backupImportService.importBackup(uri, strategy)) {
                is ImportResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        showSuccessMessage = true,
                        successMessage = "Import erfolgreich: ${result.importedCount.studios} Studios, " +
                                "${result.importedCount.classes} Kurse, ${result.importedCount.templates} Vorlagen, " +
                                "${result.importedCount.invoices} Rechnungen"
                    )
                }
                is ImportResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        showErrorMessage = true,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
    
    fun cancelImport() {
        _uiState.value = _uiState.value.copy(
            showImportPreview = false,
            previewData = null,
            previewMetadata = null,
            selectedImportUri = null
        )
    }
    
    fun selectFileForImport() {
        // This will be handled by the UI layer with ActivityResultContracts
        _uiState.value = _uiState.value.copy(showFilePicker = true)
    }
    
    fun onFileSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(showFilePicker = false)
        validateAndPreviewImport(uri)
    }
    
    fun dismissMessages() {
        _uiState.value = _uiState.value.copy(
            showSuccessMessage = false,
            showErrorMessage = false,
            successMessage = "",
            errorMessage = ""
        )
    }
}

data class BackupUiState(
    val isProcessing: Boolean = false,
    val showImportPreview: Boolean = false,
    val previewData: BackupData? = null,
    val previewMetadata: BackupMetadata? = null,
    val selectedImportUri: Uri? = null,
    val lastBackupResult: BackupResult.Success? = null,
    val recentBackups: List<BackupHistoryItem> = emptyList(),
    val showFilePicker: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val successMessage: String = "",
    val showErrorMessage: Boolean = false,
    val errorMessage: String = ""
)

data class BackupHistoryItem(
    val fileName: String,
    val filePath: String,
    val createdAt: String,
    val size: String
)

enum class BackupFormat {
    JSON, ZIP
}
