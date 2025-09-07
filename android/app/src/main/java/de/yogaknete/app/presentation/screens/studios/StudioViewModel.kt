package de.yogaknete.app.presentation.screens.studios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.repository.StudioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudioUiState(
    val studios: List<Studio> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingStudio: Studio? = null,
    val successMessage: String? = null
)

@HiltViewModel
class StudioViewModel @Inject constructor(
    private val studioRepository: StudioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()
    
    init {
        loadStudios()
    }
    
    private fun loadStudios() {
        viewModelScope.launch {
            studioRepository.getAllStudios()
                .collect { studioList ->
                    _uiState.update { 
                        it.copy(
                            studios = studioList,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }
    
    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }
    
    fun showEditDialog(studio: Studio) {
        _uiState.update { it.copy(editingStudio = studio) }
    }
    
    fun hideEditDialog() {
        _uiState.update { it.copy(editingStudio = null) }
    }
    
    fun addStudio(
        name: String,
        contactPerson: String,
        email: String,
        phone: String,
        street: String,
        postalCode: String,
        city: String,
        hourlyRate: Double
    ) {
        viewModelScope.launch {
            try {
                val studio = Studio(
                    name = name,
                    contactPerson = contactPerson,
                    email = email,
                    phone = phone,
                    street = street,
                    postalCode = postalCode,
                    city = city,
                    hourlyRate = hourlyRate
                )
                studioRepository.saveStudio(studio)
                _uiState.update { 
                    it.copy(
                        showAddDialog = false,
                        successMessage = "Studio erfolgreich hinzugefügt"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Fehler beim Hinzufügen des Studios: ${e.message}")
                }
            }
        }
    }
    
    fun updateStudio(
        studio: Studio,
        name: String,
        contactPerson: String,
        email: String,
        phone: String,
        street: String,
        postalCode: String,
        city: String,
        hourlyRate: Double
    ) {
        viewModelScope.launch {
            try {
                val updatedStudio = studio.copy(
                    name = name,
                    contactPerson = contactPerson,
                    email = email,
                    phone = phone,
                    street = street,
                    postalCode = postalCode,
                    city = city,
                    hourlyRate = hourlyRate
                )
                studioRepository.updateStudio(updatedStudio)
                _uiState.update { 
                    it.copy(
                        editingStudio = null,
                        successMessage = "Studio erfolgreich aktualisiert"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Fehler beim Aktualisieren des Studios: ${e.message}")
                }
            }
        }
    }
    
    fun deleteStudio(studio: Studio) {
        viewModelScope.launch {
            try {
                studioRepository.deleteStudio(studio)
                _uiState.update { 
                    it.copy(successMessage = "Studio erfolgreich gelöscht")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Fehler beim Löschen des Studios: ${e.message}")
                }
            }
        }
    }
    
    fun toggleStudioActive(studio: Studio) {
        viewModelScope.launch {
            try {
                val updatedStudio = studio.copy(isActive = !studio.isActive)
                studioRepository.updateStudio(updatedStudio)
                _uiState.update { 
                    it.copy(
                        successMessage = if (updatedStudio.isActive) 
                            "Studio aktiviert" 
                        else 
                            "Studio deaktiviert"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Fehler beim Aktualisieren des Studio-Status: ${e.message}")
                }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { 
            it.copy(error = null, successMessage = null)
        }
    }
}
