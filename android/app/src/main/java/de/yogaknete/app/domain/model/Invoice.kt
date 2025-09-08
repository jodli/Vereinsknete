package de.yogaknete.app.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Represents an invoice for a specific studio and month
 */
@Serializable
@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = Studio::class,
            parentColumns = ["id"],
            childColumns = ["studioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studioId: Long,
    val invoiceNumber: String, // Format: YYYY-MM-XXX (e.g., 2024-03-001)
    val month: Int, // 1-12
    val year: Int,
    val totalHours: Double,
    val hourlyRate: Double, // Store the rate at time of invoice creation
    val totalAmount: Double, // totalHours * hourlyRate
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val createdAt: LocalDateTime,
    val paidAt: LocalDateTime? = null,
    val notes: String = "",
    val pdfPath: String? = null // Path to generated PDF file
)

@Serializable
enum class PaymentStatus {
    PENDING,    // Invoice created but not paid
    PAID,       // Payment received
    OVERDUE,    // Payment is late
    CANCELLED   // Invoice was cancelled
}

/**
 * Data class for invoice summary calculations
 */
data class InvoiceSummary(
    val studioId: Long,
    val studioName: String,
    val month: Int,
    val year: Int,
    val totalHours: Double,
    val completedClasses: Int,
    val hourlyRate: Double,
    val totalAmount: Double,
    val hasExistingInvoice: Boolean = false,
    val invoiceId: Long? = null,
    val paymentStatus: PaymentStatus? = null
)
