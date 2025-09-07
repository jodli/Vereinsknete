package de.yogaknete.app.presentation.screens.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yogaknete.app.domain.model.Invoice
import de.yogaknete.app.domain.model.InvoiceSummary
import de.yogaknete.app.domain.model.PaymentStatus
import de.yogaknete.app.domain.repository.InvoiceRepository
import de.yogaknete.app.domain.repository.StudioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

data class InvoiceUiState(
    val selectedMonth: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber,
    val selectedYear: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year,
    val invoiceSummaries: List<InvoiceSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showMonthPicker: Boolean = false
)

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val studioRepository: StudioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()
    
    init {
        loadInvoiceSummaries()
    }
    
    fun loadInvoiceSummaries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val summaries = invoiceRepository.getInvoiceSummariesForMonth(
                    month = _uiState.value.selectedMonth,
                    year = _uiState.value.selectedYear
                )
                
                _uiState.update { 
                    it.copy(
                        invoiceSummaries = summaries,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Fehler beim Laden der Rechnungsübersicht: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun selectMonth(month: Int, year: Int) {
        _uiState.update { 
            it.copy(
                selectedMonth = month,
                selectedYear = year,
                showMonthPicker = false
            )
        }
        loadInvoiceSummaries()
    }
    
    fun navigateToPreviousMonth() {
        val currentMonth = _uiState.value.selectedMonth
        val currentYear = _uiState.value.selectedYear
        
        val (newMonth, newYear) = if (currentMonth == 1) {
            12 to currentYear - 1
        } else {
            currentMonth - 1 to currentYear
        }
        
        selectMonth(newMonth, newYear)
    }
    
    fun navigateToNextMonth() {
        val currentMonth = _uiState.value.selectedMonth
        val currentYear = _uiState.value.selectedYear
        
        val (newMonth, newYear) = if (currentMonth == 12) {
            1 to currentYear + 1
        } else {
            currentMonth + 1 to currentYear
        }
        
        selectMonth(newMonth, newYear)
    }
    
    fun toggleMonthPicker() {
        _uiState.update { it.copy(showMonthPicker = !it.showMonthPicker) }
    }
    
    fun createInvoice(summary: InvoiceSummary) {
        viewModelScope.launch {
            try {
                // Check if invoice already exists
                if (summary.hasExistingInvoice) {
                    _uiState.update { 
                        it.copy(error = "Rechnung für dieses Studio und diesen Monat existiert bereits")
                    }
                    return@launch
                }
                
                val invoiceNumber = invoiceRepository.generateInvoiceNumber(
                    year = summary.year,
                    month = summary.month
                )
                
                val invoice = Invoice(
                    studioId = summary.studioId,
                    invoiceNumber = invoiceNumber,
                    month = summary.month,
                    year = summary.year,
                    totalHours = summary.totalHours,
                    hourlyRate = summary.hourlyRate,
                    totalAmount = summary.totalAmount,
                    paymentStatus = PaymentStatus.PENDING,
                    createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                )
                
                val invoiceId = invoiceRepository.createInvoice(invoice)
                
                // Reload summaries to reflect the new invoice
                loadInvoiceSummaries()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Fehler beim Erstellen der Rechnung: ${e.message}")
                }
            }
        }
    }
    
    fun updatePaymentStatus(invoiceId: Long, status: PaymentStatus) {
        viewModelScope.launch {
            try {
                val paidAt = if (status == PaymentStatus.PAID) {
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                } else {
                    null
                }
                
                invoiceRepository.updatePaymentStatus(invoiceId, status, paidAt)
                loadInvoiceSummaries()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Fehler beim Aktualisieren des Zahlungsstatus: ${e.message}")
                }
            }
        }
    }
    
    fun updateInvoice(summary: InvoiceSummary) {
        viewModelScope.launch {
            try {
                if (summary.invoiceId == null) {
                    _uiState.update { 
                        it.copy(error = "Keine Rechnung zum Aktualisieren gefunden")
                    }
                    return@launch
                }
                
                // Get the existing invoice
                val existingInvoice = invoiceRepository.getInvoiceById(summary.invoiceId)
                if (existingInvoice != null) {
                    // Update with new totals
                    val updatedInvoice = existingInvoice.copy(
                        totalHours = summary.totalHours,
                        totalAmount = summary.totalAmount
                    )
                    invoiceRepository.updateInvoice(updatedInvoice)
                    
                    // Reload summaries to reflect the update
                    loadInvoiceSummaries()
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Fehler beim Aktualisieren der Rechnung: ${e.message}")
                }
            }
        }
    }
    
    fun deleteInvoice(invoiceId: Long) {
        viewModelScope.launch {
            try {
                val invoice = invoiceRepository.getInvoiceById(invoiceId)
                if (invoice != null) {
                    invoiceRepository.deleteInvoice(invoice)
                    loadInvoiceSummaries()
                    _uiState.update { 
                        it.copy(error = null)
                    }
                } else {
                    _uiState.update { 
                        it.copy(error = "Rechnung nicht gefunden")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Fehler beim Löschen der Rechnung: ${e.message}")
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Januar"
            2 -> "Februar"
            3 -> "März"
            4 -> "April"
            5 -> "Mai"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "August"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "Dezember"
            else -> ""
        }
    }
    
    fun getTotalMonthlyAmount(): Double {
        return _uiState.value.invoiceSummaries.sumOf { it.totalAmount }
    }
    
    fun getTotalMonthlyHours(): Double {
        return _uiState.value.invoiceSummaries.sumOf { it.totalHours }
    }
}
