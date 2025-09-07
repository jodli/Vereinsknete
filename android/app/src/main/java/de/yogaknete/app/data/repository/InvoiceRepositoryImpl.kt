package de.yogaknete.app.data.repository

import de.yogaknete.app.data.local.InvoiceDao
import de.yogaknete.app.domain.model.Invoice
import de.yogaknete.app.domain.model.InvoiceSummary
import de.yogaknete.app.domain.model.PaymentStatus
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    
    override suspend fun createInvoice(invoice: Invoice): Long {
        return invoiceDao.insertInvoice(invoice)
    }
    
    override suspend fun updateInvoice(invoice: Invoice) {
        invoiceDao.updateInvoice(invoice)
    }
    
    override suspend fun deleteInvoice(invoice: Invoice) {
        invoiceDao.deleteInvoice(invoice)
    }
    
    override suspend fun getInvoiceById(invoiceId: Long): Invoice? {
        return invoiceDao.getInvoiceById(invoiceId)
    }
    
    override fun getInvoicesByMonth(month: Int, year: Int): Flow<List<Invoice>> {
        return invoiceDao.getInvoicesByMonth(month, year)
    }
    
    override suspend fun getInvoiceByStudioAndMonth(
        studioId: Long,
        month: Int,
        year: Int
    ): Invoice? {
        return invoiceDao.getInvoiceByStudioAndMonth(studioId, month, year)
    }
    
    override fun getAllInvoices(): Flow<List<Invoice>> {
        return invoiceDao.getAllInvoices()
    }
    
    override fun getInvoicesByStatus(status: PaymentStatus): Flow<List<Invoice>> {
        return invoiceDao.getInvoicesByStatus(status)
    }
    
    override suspend fun updatePaymentStatus(
        invoiceId: Long,
        status: PaymentStatus,
        paidAt: LocalDateTime?
    ) {
        invoiceDao.updatePaymentStatus(invoiceId, status, paidAt)
    }
    
    override suspend fun getInvoiceSummariesForMonth(
        month: Int,
        year: Int
    ): List<InvoiceSummary> {
        return invoiceDao.getInvoiceSummariesForMonth(month, year)
    }
    
    override suspend fun getClassesForInvoice(
        studioId: Long,
        month: Int,
        year: Int
    ): List<YogaClass> {
        return invoiceDao.getClassesForInvoice(studioId, month, year)
    }
    
    override suspend fun generateInvoiceNumber(year: Int, month: Int): String {
        // Get the count of invoices for this year to generate sequential number
        val yearCount = invoiceDao.getInvoiceCountForYear(year)
        val sequentialNumber = (yearCount + 1).toString().padStart(3, '0')
        
        // Format: YYYY-XXX (e.g., 2025-005)
        return "$year-$sequentialNumber"
    }
    
    override suspend fun invoiceExists(studioId: Long, month: Int, year: Int): Boolean {
        return invoiceDao.getInvoiceByStudioAndMonth(studioId, month, year) != null
    }
}
