package de.yogaknete.app.data.local

import androidx.room.*
import de.yogaknete.app.domain.model.Invoice
import de.yogaknete.app.domain.model.InvoiceSummary
import de.yogaknete.app.domain.model.PaymentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    
    @Insert
    suspend fun insertInvoice(invoice: Invoice): Long
    
    @Update
    suspend fun updateInvoice(invoice: Invoice)
    
    @Delete
    suspend fun deleteInvoice(invoice: Invoice)
    
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceById(invoiceId: Long): Invoice?
    
    @Query("SELECT * FROM invoices WHERE month = :month AND year = :year")
    fun getInvoicesByMonth(month: Int, year: Int): Flow<List<Invoice>>
    
    @Query("SELECT * FROM invoices WHERE studioId = :studioId AND month = :month AND year = :year LIMIT 1")
    suspend fun getInvoiceByStudioAndMonth(studioId: Long, month: Int, year: Int): Invoice?
    
    @Query("SELECT * FROM invoices ORDER BY year DESC, month DESC")
    fun getAllInvoices(): Flow<List<Invoice>>
    
    @Query("SELECT * FROM invoices WHERE paymentStatus = :status")
    fun getInvoicesByStatus(status: PaymentStatus): Flow<List<Invoice>>
    
    @Query("UPDATE invoices SET paymentStatus = :status, paidAt = :paidAt WHERE id = :invoiceId")
    suspend fun updatePaymentStatus(invoiceId: Long, status: PaymentStatus, paidAt: kotlinx.datetime.LocalDateTime?)
    
    @Query("SELECT COUNT(*) FROM invoices WHERE year = :year AND month = :month")
    suspend fun getInvoiceCountForMonth(year: Int, month: Int): Int
    
    @Query("""
        SELECT 
            COUNT(*) as count
        FROM invoices 
        WHERE year = :year
    """)
    suspend fun getInvoiceCountForYear(year: Int): Int
    
    /**
     * Get summary of completed classes for invoice generation
     * This query calculates totals from yoga_classes table for each studio in a given month
     */
    @Query("""
        SELECT 
            s.id as studioId,
            s.name as studioName,
            :month as month,
            :year as year,
            COALESCE(SUM(yc.durationHours), 0.0) as totalHours,
            COUNT(yc.id) as completedClasses,
            s.hourlyRate as hourlyRate,
            COALESCE(SUM(yc.durationHours), 0.0) * s.hourlyRate as totalAmount,
            CASE WHEN i.id IS NOT NULL THEN 1 ELSE 0 END as hasExistingInvoice,
            i.id as invoiceId,
            i.paymentStatus as paymentStatus
        FROM studios s
        LEFT JOIN yoga_classes yc ON s.id = yc.studioId 
            AND yc.status = 'COMPLETED'
            AND strftime('%Y', yc.startTime) = CAST(:year AS TEXT)
            AND strftime('%m', yc.startTime) = printf('%02d', :month)
        LEFT JOIN invoices i ON s.id = i.studioId 
            AND i.month = :month 
            AND i.year = :year
        WHERE s.isActive = 1
        GROUP BY s.id, s.name, s.hourlyRate, i.id, i.paymentStatus
        HAVING COUNT(yc.id) > 0 OR i.id IS NOT NULL
        ORDER BY s.name
    """)
    suspend fun getInvoiceSummariesForMonth(month: Int, year: Int): List<InvoiceSummary>
    
    /**
     * Get classes for a specific invoice
     */
    @Query("""
        SELECT yc.* FROM yoga_classes yc
        WHERE yc.studioId = :studioId 
            AND yc.status = 'COMPLETED'
            AND strftime('%Y', yc.startTime) = CAST(:year AS TEXT)
            AND strftime('%m', yc.startTime) = printf('%02d', :month)
        ORDER BY yc.startTime
    """)
    suspend fun getClassesForInvoice(studioId: Long, month: Int, year: Int): List<de.yogaknete.app.domain.model.YogaClass>
    
    /**
     * Check if invoice number already exists
     */
    @Query("SELECT COUNT(*) > 0 FROM invoices WHERE invoiceNumber = :invoiceNumber")
    suspend fun invoiceNumberExists(invoiceNumber: String): Boolean
}
