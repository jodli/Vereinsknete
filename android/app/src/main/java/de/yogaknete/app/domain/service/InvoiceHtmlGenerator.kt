package de.yogaknete.app.domain.service

import de.yogaknete.app.domain.model.Invoice
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.model.YogaClass
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class InvoiceHtmlGenerator @Inject constructor() {
    
    private val currencyFormat = DecimalFormat("#,##0.00")
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy 'um' HH:mm 'Uhr'", Locale.GERMANY)
    
    fun generateInvoiceHtml(
        invoice: Invoice,
        userProfile: UserProfile,
        studio: Studio,
        yogaClasses: List<YogaClass>
    ): String {
        val currentDate = Date()
        val dueDate = Calendar.getInstance().apply {
            time = currentDate
            add(Calendar.DAY_OF_MONTH, 14) // 14 days payment term
        }.time
        
        return """
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Rechnung ${invoice.invoiceNumber}</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            line-height: 1.6;
            color: #333;
            background: white;
            padding: 40px;
            margin: 0;
        }
        
        .invoice-container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
        }
        
        .header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 40px;
            padding-bottom: 20px;
            border-bottom: 2px solid #333;
        }
        
        .sender-info {
            font-size: 12px;
            line-height: 1.4;
        }
        
        .invoice-title {
            text-align: right;
        }
        
        .invoice-title h1 {
            font-size: 32px;
            color: #333;
            margin-bottom: 5px;
        }
        
        .invoice-number {
            font-size: 14px;
            color: #666;
        }
        
        .addresses {
            display: flex;
            justify-content: space-between;
            margin-bottom: 40px;
        }
        
        .address-block {
            width: 45%;
        }
        
        .address-label {
            font-size: 10px;
            color: #666;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 8px;
        }
        
        .address-content {
            font-size: 14px;
            line-height: 1.6;
        }
        
        .address-content strong {
            display: block;
            font-size: 16px;
            margin-bottom: 4px;
        }
        
        .invoice-meta {
            margin-bottom: 40px;
        }
        
        .meta-table {
            width: 100%;
            font-size: 14px;
        }
        
        .meta-table td {
            padding: 5px 0;
        }
        
        .meta-table td:first-child {
            width: 150px;
            color: #666;
        }
        
        .meta-table td:last-child {
            font-weight: 500;
        }
        
        .services-section {
            margin-bottom: 40px;
        }
        
        .section-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 15px;
            color: #333;
        }
        
        .services-description {
            font-size: 14px;
            color: #666;
            margin-bottom: 15px;
        }
        
        .services-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }
        
        .services-table thead {
            background: #f5f5f5;
        }
        
        .services-table th {
            padding: 10px;
            font-weight: 600;
            color: #333;
            border-bottom: 2px solid #ddd;
        }
        
        .services-table th:first-child {
            text-align: left;
        }
        
        .services-table td {
            padding: 8px 10px;
            border-bottom: 1px solid #eee;
        }
        
        .services-table tbody tr:hover {
            background: #fafafa;
        }
        
        .text-right {
            text-align: right;
        }
        
        .text-center {
            text-align: center;
        }
        
        .totals-section {
            margin-top: 20px;
            padding-top: 20px;
            border-top: 2px solid #333;
        }
        
        .totals-table {
            width: 100%;
            font-size: 14px;
        }
        
        .totals-table td {
            padding: 5px 0;
        }
        
        .totals-table td:first-child {
            text-align: right;
            padding-right: 20px;
        }
        
        .totals-table td:last-child {
            text-align: right;
            width: 150px;
        }
        
        .total-row {
            font-size: 18px;
            font-weight: bold;
            color: #333;
        }
        
        .payment-section {
            margin-top: 40px;
            padding: 20px;
            background: #f9f9f9;
            border-radius: 5px;
            page-break-inside: avoid;
            break-inside: avoid;
        }
        
        .payment-title {
            font-size: 16px;
            font-weight: 600;
            margin-bottom: 15px;
            color: #333;
        }
        
        .payment-details {
            font-size: 14px;
            line-height: 1.8;
        }
        
        .payment-details p {
            margin-bottom: 8px;
        }
        
        .footer {
            margin-top: 60px;
            padding-top: 20px;
            border-top: 1px solid #ddd;
            text-align: center;
            font-size: 12px;
            color: #666;
        }
        
        .footer p {
            margin-bottom: 5px;
        }
        
        @media print {
            body {
                padding: 0;
                margin: 0;
            }
            
            .invoice-container {
                max-width: 100%;
            }
            
            .services-table tbody tr:hover {
                background: transparent;
            }
            
            @page {
                margin: 20mm;
                size: A4;
            }
        }
    </style>
</head>
<body>
    <div class="invoice-container">
        <!-- Header with sender info and invoice title -->
        <div class="header">
            <div class="sender-info">
                ${userProfile.name}<br>
                ${if (userProfile.street.isNotEmpty()) "${userProfile.street}<br>" else ""}
                ${if (userProfile.postalCode.isNotEmpty() || userProfile.city.isNotEmpty()) "${userProfile.postalCode} ${userProfile.city}<br>" else ""}
                ${if (userProfile.phone.isNotEmpty()) "Tel: ${userProfile.phone}<br>" else ""}
                ${if (userProfile.email.isNotEmpty()) "E-Mail: ${userProfile.email}<br>" else ""}
                ${if (userProfile.taxId.isNotEmpty()) "Steuernummer: ${userProfile.taxId}" else ""}
            </div>
            <div class="invoice-title">
                <h1>RECHNUNG</h1>
                <div class="invoice-number">${invoice.invoiceNumber}</div>
            </div>
        </div>
        
        <!-- Addresses -->
        <div class="addresses">
            <div class="address-block">
                <div class="address-label">Rechnungssteller</div>
                <div class="address-content">
                    <strong>${userProfile.name}</strong>
                    ${if (userProfile.street.isNotEmpty()) "${userProfile.street}<br>" else ""}
                    ${if (userProfile.postalCode.isNotEmpty() || userProfile.city.isNotEmpty()) "${userProfile.postalCode} ${userProfile.city}" else ""}
                </div>
            </div>
            
            <div class="address-block">
                <div class="address-label">Rechnungsempfänger</div>
                <div class="address-content">
                    <strong>${studio.name}</strong>
                    ${if (studio.contactPerson.isNotEmpty()) "${studio.contactPerson}<br>" else ""}
                    ${if (studio.street.isNotEmpty()) "${studio.street}<br>" else ""}
                    ${if (studio.postalCode.isNotEmpty() || studio.city.isNotEmpty()) "${studio.postalCode} ${studio.city}" else ""}
                </div>
            </div>
        </div>
        
        <!-- Invoice metadata -->
        <div class="invoice-meta">
            <table class="meta-table">
                <tr>
                    <td>Rechnungsnummer:</td>
                    <td>${invoice.invoiceNumber}</td>
                </tr>
                <tr>
                    <td>Rechnungsdatum:</td>
                    <td>${dateFormat.format(currentDate)}</td>
                </tr>
            </table>
        </div>
        
        <!-- Services section -->
        <div class="services-section">
            <h2 class="section-title">Leistungsübersicht</h2>
            <p class="services-description">
                Yoga-Unterricht im ${getMonthName(invoice.month)} ${invoice.year}
            </p>
            
            <table class="services-table">
                <thead>
                    <tr>
                        <th>Datum</th>
                        <th>Uhrzeit</th>
                        <th class="text-center">Dauer</th>
                        <th class="text-right">Stundensatz</th>
                        <th class="text-right">Betrag</th>
                    </tr>
                </thead>
                <tbody>
                    ${yogaClasses.joinToString("\n") { yogaClass ->
                        val classDate = Date(yogaClass.startTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)
                        val amount = yogaClass.durationHours * invoice.hourlyRate
                        """
                    <tr>
                        <td>${dateFormat.format(classDate)}</td>
                        <td>${timeFormat.format(classDate)}</td>
                        <td class="text-center">${formatDuration(yogaClass.durationHours)}</td>
                        <td class="text-right">${currencyFormat.format(invoice.hourlyRate)} €</td>
                        <td class="text-right">${currencyFormat.format(amount)} €</td>
                    </tr>
                        """.trim()
                    }}
                </tbody>
            </table>
            
            <!-- Totals -->
            <div class="totals-section">
                <table class="totals-table">
                    <tr>
                        <td>Summe Stunden:</td>
                        <td>${formatDuration(invoice.totalHours)}</td>
                    </tr>
                    <tr>
                        <td>Zwischensumme:</td>
                        <td>${currencyFormat.format(invoice.totalAmount)} €</td>
                    </tr>
                    <tr class="total-row">
                        <td>Gesamtbetrag:</td>
                        <td>${currencyFormat.format(invoice.totalAmount)} €</td>
                    </tr>
                </table>
            </div>
        </div>
        
        <!-- Payment information -->
        <div class="payment-section">
            <h3 class="payment-title">Zahlungsinformationen</h3>
            <div class="payment-details">
                <p>Bitte überweisen Sie den Gesamtbetrag auf folgendes Konto:</p>
                ${if (userProfile.bankName.isNotEmpty()) "<p><strong>Bank:</strong> ${userProfile.bankName}</p>" else ""}
                <p><strong>IBAN:</strong> ${formatIban(userProfile.iban)}</p>
                ${if (userProfile.bic.isNotEmpty()) "<p><strong>BIC:</strong> ${userProfile.bic}</p>" else ""}
                <p><strong>Verwendungszweck:</strong> ${invoice.invoiceNumber}</p>
            </div>
        </div>
    </div>
</body>
</html>
        """.trimIndent()
    }
    
    private fun formatDuration(hours: Double): String {
        val totalMinutes = (hours * 60).toInt()
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return if (m > 0) {
            "${h}h ${m}min"
        } else {
            "${h}h"
        }
    }
    
    private fun formatIban(iban: String): String {
        return iban.chunked(4).joinToString(" ")
    }
    
    private fun getMonthName(month: Int): String {
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
}
