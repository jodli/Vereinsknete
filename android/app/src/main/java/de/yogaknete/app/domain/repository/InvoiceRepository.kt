package de.yogaknete.app.domain.repository

import de.yogaknete.app.domain.model.Invoice
import de.yogaknete.app.domain.model.InvoiceSummary
import de.yogaknete.app.domain.model.PaymentStatus
import de.yogaknete.app.domain.model.YogaClass
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface InvoiceRepository {
    
    /**
     * Create a new invoice
     */
    suspend fun createInvoice(invoice: Invoice): Long
    
    /**
     * Update an existing invoice
     */
    suspend fun updateInvoice(invoice: Invoice)
    
    /**
     * Delete an invoice
     */
    suspend fun deleteInvoice(invoice: Invoice)
    
    /**
     * Get invoice by ID
     */
    suspend fun getInvoiceById(invoiceId: Long): Invoice?
    
    /**
     * Get all invoices for a specific month
     */
    fun getInvoicesByMonth(month: Int, year: Int): Flow<List<Invoice>>
    
    /**
     * Get invoice for a specific studio and month
     */
    suspend fun getInvoiceByStudioAndMonth(studioId: Long, month: Int, year: Int): Invoice?
    
    /**
     * Get all invoices
     */
    fun getAllInvoices(): Flow<List<Invoice>>
    
    /**
     * Get invoices by payment status
     */
    fun getInvoicesByStatus(status: PaymentStatus): Flow<List<Invoice>>
    
    /**
     * Update payment status of an invoice
     */
    suspend fun updatePaymentStatus(invoiceId: Long, status: PaymentStatus, paidAt: LocalDateTime? = null)
    
    /**
     * Get invoice summaries for a month (includes calculation of totals from classes)
     */
    suspend fun getInvoiceSummariesForMonth(month: Int, year: Int): List<InvoiceSummary>
    
    /**
     * Get all completed classes for an invoice
     */
    suspend fun getClassesForInvoice(studioId: Long, month: Int, year: Int): List<YogaClass>
    
    /**
     * Generate a unique invoice number
     */
    suspend fun generateInvoiceNumber(year: Int, month: Int): String
    
    /**
     * Check if an invoice already exists for a studio and month
     */
    suspend fun invoiceExists(studioId: Long, month: Int, year: Int): Boolean
    
    suspend fun getAllInvoicesOnce(): List<Invoice>
    
    suspend fun insertInvoice(invoice: Invoice): Long
    
    suspend fun deleteAllInvoices()
}
