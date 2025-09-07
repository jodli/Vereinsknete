package de.yogaknete.app.presentation.screens.invoice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yogaknete.app.domain.model.Invoice
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.repository.InvoiceRepository
import de.yogaknete.app.domain.repository.StudioRepository
import de.yogaknete.app.domain.repository.UserProfileRepository
import de.yogaknete.app.domain.repository.YogaClassRepository
import de.yogaknete.app.domain.service.InvoicePdfService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoiceDetailUiState(
    val invoice: Invoice? = null,
    val studio: Studio? = null,
    val userProfile: UserProfile? = null,
    val yogaClasses: List<YogaClass> = emptyList(),
    val actualTotalHours: Double = 0.0,  // Calculated from actual classes
    val actualTotalAmount: Double = 0.0, // Calculated from actual classes
    val isLoading: Boolean = true,
    val error: String? = null,
    val isPdfGenerating: Boolean = false,
    val pdfPath: String? = null
)

@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val invoiceRepository: InvoiceRepository,
    private val studioRepository: StudioRepository,
    private val userProfileRepository: UserProfileRepository,
    private val yogaClassRepository: YogaClassRepository,
    private val pdfService: InvoicePdfService
) : ViewModel() {
    
    private val invoiceId: Long = checkNotNull(savedStateHandle.get<String>("invoiceId")?.toLongOrNull())
    
    private val _uiState = MutableStateFlow(InvoiceDetailUiState())
    val uiState: StateFlow<InvoiceDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadInvoiceDetails()
    }
    
    private fun loadInvoiceDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load invoice
                val invoice = invoiceRepository.getInvoiceById(invoiceId)
                if (invoice == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Rechnung nicht gefunden"
                        )
                    }
                    return@launch
                }
                
                // Load studio
                val studio = studioRepository.getStudioById(invoice.studioId)
                
                // Load user profile
                val userProfile = userProfileRepository.getUserProfileOnce()
                
                // Load yoga classes for this invoice
                val yogaClasses = yogaClassRepository.getClassesForInvoice(
                    studioId = invoice.studioId,
                    month = invoice.month,
                    year = invoice.year
                ).filter { it.status == de.yogaknete.app.domain.model.ClassStatus.COMPLETED }
                
                // Calculate actual totals from the classes
                val actualTotalHours = yogaClasses.sumOf { it.durationHours }
                val actualTotalAmount = actualTotalHours * invoice.hourlyRate
                
                _uiState.update { 
                    it.copy(
                        invoice = invoice,
                        studio = studio,
                        userProfile = userProfile,
                        yogaClasses = yogaClasses,
                        actualTotalHours = actualTotalHours,
                        actualTotalAmount = actualTotalAmount,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Fehler beim Laden der Rechnung: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun generatePdf(activity: android.app.Activity) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.invoice == null || state.studio == null || state.userProfile == null) {
                _uiState.update { 
                    it.copy(error = "Fehlende Daten für PDF-Generierung")
                }
                return@launch
            }
            
            _uiState.update { it.copy(isPdfGenerating = true, error = null) }
            
            try {
                // Create an updated invoice with actual totals for PDF generation
                val invoiceForPdf = state.invoice.copy(
                    totalHours = state.actualTotalHours,
                    totalAmount = state.actualTotalAmount
                )
                
                pdfService.generateAndPrintPdf(
                    activity = activity,
                    invoice = invoiceForPdf,
                    userProfile = state.userProfile,
                    studio = state.studio,
                    yogaClasses = state.yogaClasses
                )
                _uiState.update { it.copy(isPdfGenerating = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isPdfGenerating = false,
                        error = "Fehler beim Generieren der PDF: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
