package de.yogaknete.app.domain.service

import de.yogaknete.app.domain.model.Invoice
import de.yogaknete.app.domain.model.PaymentStatus
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.model.YogaClass
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InvoiceHtmlGeneratorTest {

    private lateinit var generator: InvoiceHtmlGenerator

    private val testProfile = UserProfile(
        name = "Anna Yoga",
        street = "Yogastraße 1",
        postalCode = "80331",
        city = "München",
        taxId = "123/456/78901",
        phone = "0170 1234567",
        email = "anna@yoga.de",
        bankName = "Sparkasse München",
        iban = "DE89370400440532013000",
        bic = "COBADEFFXXX",
        defaultHourlyRate = 45.0
    )

    private val testStudio = Studio(
        id = 1,
        name = "Yoga Studio Zen",
        contactPerson = "Max Müller",
        street = "Hauptstraße 10",
        postalCode = "80333",
        city = "München",
        hourlyRate = 45.0
    )

    private val testInvoice = Invoice(
        id = 1,
        studioId = 1,
        invoiceNumber = "2026-01-001",
        month = 1,
        year = 2026,
        totalHours = 3.0,
        hourlyRate = 45.0,
        totalAmount = 135.0,
        createdAt = LocalDateTime(2026, 1, 31, 10, 0)
    )

    private val testClasses = listOf(
        YogaClass(
            id = 1,
            studioId = 1,
            title = "Hatha Yoga",
            startTime = LocalDateTime(2026, 1, 7, 9, 0),
            endTime = LocalDateTime(2026, 1, 7, 10, 30),
            durationHours = 1.5
        ),
        YogaClass(
            id = 2,
            studioId = 1,
            title = "Vinyasa Flow",
            startTime = LocalDateTime(2026, 1, 14, 9, 0),
            endTime = LocalDateTime(2026, 1, 14, 10, 30),
            durationHours = 1.5
        )
    )

    @Before
    fun setUp() {
        generator = InvoiceHtmlGenerator()
    }

    @Test
    fun `Uhrzeit header has text-center class`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(
            "Uhrzeit th should have text-center class",
            html.contains("""<th class="text-center">Uhrzeit</th>""")
        )
    }

    @Test
    fun `time cells have text-center class`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        // Each yoga class row should have a centered time cell
        val timeCellPattern = Regex("""<td class="text-center">\d{2}:\d{2}</td>""")
        val matches = timeCellPattern.findAll(html).toList()
        assertEquals("Each class should have a centered time cell", testClasses.size, matches.size)
    }

    @Test
    fun `due date is shown in meta table`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(
            "Meta table should contain 'Zahlbar bis'",
            html.contains("Zahlbar bis:")
        )
    }

    @Test
    fun `due date is shown in screen payment section`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        // The screen payment section includes the due date in the text
        assertTrue(
            "Screen payment section should mention due date",
            html.contains("Bitte überweisen Sie den Gesamtbetrag bis zum")
        )
    }

    @Test
    fun `payment footer contains IBAN`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(
            "Payment footer should contain formatted IBAN",
            html.contains("""IBAN: DE89 3704 0044 0532 0130 00""")
        )
    }

    @Test
    fun `payment footer contains BIC when provided`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(
            "Payment footer should contain BIC",
            html.contains("BIC: COBADEFFXXX")
        )
    }

    @Test
    fun `payment footer omits BIC when empty`() {
        val profileNoBic = testProfile.copy(bic = "")
        val html = generator.generateInvoiceHtml(testInvoice, profileNoBic, testStudio, testClasses)

        val footerSection = html.substringAfter("payment-footer-center")
        assertFalse(
            "Payment footer should not contain BIC when empty",
            footerSection.contains("BIC:")
        )
    }

    @Test
    fun `payment footer contains invoice reference`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(
            "Payment footer should contain invoice reference",
            html.contains("Ref: 2026-01-001")
        )
    }

    @Test
    fun `payment footer has position fixed in print CSS`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(
            "Print CSS should fix payment footer to bottom",
            html.contains("position: fixed") && html.contains("payment-footer")
        )
    }

    @Test
    fun `payment section is hidden in print CSS`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        // The @media print block should hide .payment-section
        val printBlock = html.substringAfter("@media print")
        assertTrue(
            "Payment section should be hidden in print",
            printBlock.contains(".payment-section") && printBlock.contains("display: none")
        )
    }

    @Test
    fun `page counter CSS is present`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(
            "CSS should contain page counter for page numbers",
            html.contains("counter(page)") && html.contains("counter(pages)")
        )
    }

    @Test
    fun `QR code is embedded when provided`() {
        val fakeBase64 = "iVBORw0KGgoAAAANSUhEUg=="
        val html = generator.generateInvoiceHtml(
            testInvoice, testProfile, testStudio, testClasses,
            qrCodeBase64 = fakeBase64
        )

        assertTrue(
            "QR code image should be embedded as data URI",
            html.contains("""src="data:image/png;base64,$fakeBase64"""")
        )
        assertTrue(
            "QR code should have alt text GiroCode",
            html.contains("""alt="GiroCode"""")
        )
    }

    @Test
    fun `QR code is not embedded when null`() {
        val html = generator.generateInvoiceHtml(
            testInvoice, testProfile, testStudio, testClasses,
            qrCodeBase64 = null
        )

        assertFalse(
            "No img tag should be present in footer when QR is null",
            html.contains("data:image/png;base64")
        )
    }

    @Test
    fun `print body has padding-bottom for footer space`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val printBlock = html.substringAfter("@media print")
        assertTrue(
            "Print body should have padding-bottom to reserve footer space",
            printBlock.contains("padding-bottom")
        )
    }

    // --- HTML structure ---

    @Test
    fun `output is valid HTML5 document`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("<html lang=\"de\">"))
        assertTrue(html.contains("<head>"))
        assertTrue(html.contains("</head>"))
        assertTrue(html.contains("<body>"))
        assertTrue(html.contains("</body>"))
        assertTrue(html.contains("</html>"))
    }

    @Test
    fun `title contains invoice number`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("<title>Rechnung 2026-01-001</title>"))
    }

    // --- Header / sender info ---

    @Test
    fun `header shows sender name`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val senderBlock = html.substringAfter("""<div class="sender-info">""").substringBefore("</div>")
        assertTrue(senderBlock.contains("Anna Yoga"))
    }

    @Test
    fun `header shows RECHNUNG title`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("<h1>RECHNUNG</h1>"))
    }

    @Test
    fun `header shows invoice number`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("""<div class="invoice-number">2026-01-001</div>"""))
    }

    @Test
    fun `header shows optional fields when present`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val senderBlock = html.substringAfter("""<div class="sender-info">""").substringBefore("</div>")
        assertTrue("Street should be shown", senderBlock.contains("Yogastraße 1"))
        assertTrue("City should be shown", senderBlock.contains("80331 München"))
        assertTrue("Phone should be shown", senderBlock.contains("Tel: 0170 1234567"))
        assertTrue("Email should be shown", senderBlock.contains("E-Mail: anna@yoga.de"))
        assertTrue("Tax ID should be shown", senderBlock.contains("Steuernummer: 123/456/78901"))
    }

    @Test
    fun `header omits optional fields when empty`() {
        val minimalProfile = testProfile.copy(
            street = "",
            postalCode = "",
            city = "",
            phone = "",
            email = "",
            taxId = ""
        )
        val html = generator.generateInvoiceHtml(testInvoice, minimalProfile, testStudio, testClasses)

        val senderBlock = html.substringAfter("""<div class="sender-info">""").substringBefore("</div>")
        assertFalse("No Tel: when phone empty", senderBlock.contains("Tel:"))
        assertFalse("No E-Mail: when email empty", senderBlock.contains("E-Mail:"))
        assertFalse("No Steuernummer when taxId empty", senderBlock.contains("Steuernummer:"))
    }

    // --- Addresses ---

    @Test
    fun `Rechnungssteller shows user profile name and address`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val rechnungsstellerBlock = html.substringAfter("Rechnungssteller").substringBefore("Rechnungsempfänger")
        assertTrue(rechnungsstellerBlock.contains("Anna Yoga"))
        assertTrue(rechnungsstellerBlock.contains("Yogastraße 1"))
        assertTrue(rechnungsstellerBlock.contains("80331 München"))
    }

    @Test
    fun `Rechnungsempfaenger shows studio name and address`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val empfaengerBlock = html.substringAfter("Rechnungsempfänger").substringBefore("invoice-meta")
        assertTrue(empfaengerBlock.contains("Yoga Studio Zen"))
        assertTrue(empfaengerBlock.contains("Max Müller"))
        assertTrue(empfaengerBlock.contains("Hauptstraße 10"))
        assertTrue(empfaengerBlock.contains("80333 München"))
    }

    @Test
    fun `studio contact person is omitted when empty`() {
        val studioNoContact = testStudio.copy(contactPerson = "")
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, studioNoContact, testClasses)

        val empfaengerBlock = html.substringAfter("Rechnungsempfänger").substringBefore("invoice-meta")
        assertFalse(empfaengerBlock.contains("Max Müller"))
    }

    // --- Meta table ---

    @Test
    fun `meta table contains invoice number`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("Rechnungsnummer:"))
        // The invoice number appears in a td after the label
        val metaBlock = html.substringAfter("meta-table").substringBefore("</table>")
        assertTrue(metaBlock.contains("2026-01-001"))
    }

    @Test
    fun `meta table contains Rechnungsdatum`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("Rechnungsdatum:"))
    }

    // --- Services description ---

    @Test
    fun `services description shows correct month and year`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("Yoga-Unterricht im Januar 2026"))
    }

    @Test
    fun `services description uses German month names`() {
        val months = mapOf(
            1 to "Januar", 2 to "Februar", 3 to "März", 4 to "April",
            5 to "Mai", 6 to "Juni", 7 to "Juli", 8 to "August",
            9 to "September", 10 to "Oktober", 11 to "November", 12 to "Dezember"
        )
        for ((num, name) in months) {
            val invoice = testInvoice.copy(month = num)
            val html = generator.generateInvoiceHtml(invoice, testProfile, testStudio, testClasses)
            assertTrue("Month $num should be '$name'", html.contains("Yoga-Unterricht im $name"))
        }
    }

    // --- Services table rows ---

    @Test
    fun `services table has one row per yoga class`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val tbody = html.substringAfter("<tbody>").substringBefore("</tbody>")
        val rowCount = Regex("<tr>").findAll(tbody).count()
        assertEquals(2, rowCount)
    }

    @Test
    fun `services table row contains formatted date`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        // 2026-01-07 -> 07.01.2026
        assertTrue(html.contains("07.01.2026"))
        assertTrue(html.contains("14.01.2026"))
    }

    @Test
    fun `services table row contains hourly rate`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val tbody = html.substringAfter("<tbody>").substringBefore("</tbody>")
        // DecimalFormat uses system locale, so check for either 45,00 or 45.00
        assertTrue(tbody.contains("45,00 €") || tbody.contains("45.00 €"))
    }

    @Test
    fun `services table row contains calculated amount`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        // 1.5h * 45€ = 67.50€
        val tbody = html.substringAfter("<tbody>").substringBefore("</tbody>")
        assertTrue(tbody.contains("67,50 €") || tbody.contains("67.50 €"))
    }

    @Test
    fun `services table row shows duration with hours and minutes`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        // 1.5 hours -> "1h 30min"
        val tbody = html.substringAfter("<tbody>").substringBefore("</tbody>")
        assertTrue(tbody.contains("1h 30min"))
    }

    @Test
    fun `services table row shows whole hour duration without minutes`() {
        val wholeHourClasses = listOf(
            YogaClass(
                id = 1, studioId = 1, title = "Yoga",
                startTime = LocalDateTime(2026, 1, 7, 9, 0),
                endTime = LocalDateTime(2026, 1, 7, 11, 0),
                durationHours = 2.0
            )
        )
        val invoice = testInvoice.copy(totalHours = 2.0, totalAmount = 90.0)
        val html = generator.generateInvoiceHtml(invoice, testProfile, testStudio, wholeHourClasses)

        val tbody = html.substringAfter("<tbody>").substringBefore("</tbody>")
        assertTrue(tbody.contains("2h"))
        assertFalse(tbody.contains("2h 0min"))
    }

    // --- Totals ---

    @Test
    fun `totals section shows total hours`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("Summe Stunden:"))
        val totalsBlock = html.substringAfter("Summe Stunden:").substringBefore("</tr>")
        assertTrue(totalsBlock.contains("3h"))
    }

    @Test
    fun `totals section shows Zwischensumme`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("Zwischensumme:"))
        val totalsBlock = html.substringAfter("Zwischensumme:").substringBefore("</tr>")
        assertTrue(totalsBlock.contains("135"))
    }

    @Test
    fun `totals section shows Gesamtbetrag`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("Gesamtbetrag:"))
        val gesamtBlock = html.substringAfter("Gesamtbetrag:").substringBefore("</tr>")
        assertTrue(gesamtBlock.contains("135"))
    }

    @Test
    fun `Gesamtbetrag row has total-row class for bold styling`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("""<tr class="total-row">"""))
    }

    // --- Screen payment section ---

    @Test
    fun `screen payment section shows bank name when present`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val paymentBlock = html.substringAfter("Zahlungsinformationen").substringBefore("<!-- Payment footer")
        assertTrue(paymentBlock.contains("Sparkasse München"))
    }

    @Test
    fun `screen payment section omits bank name when empty`() {
        val noBankProfile = testProfile.copy(bankName = "")
        val html = generator.generateInvoiceHtml(testInvoice, noBankProfile, testStudio, testClasses)

        val paymentBlock = html.substringAfter("Zahlungsinformationen").substringBefore("<!-- Payment footer")
        assertFalse(paymentBlock.contains("<strong>Bank:</strong>"))
    }

    @Test
    fun `screen payment section shows IBAN`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val paymentBlock = html.substringAfter("Zahlungsinformationen").substringBefore("<!-- Payment footer")
        assertTrue(paymentBlock.contains("DE89 3704 0044 0532 0130 00"))
    }

    @Test
    fun `screen payment section shows BIC when present`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val paymentBlock = html.substringAfter("Zahlungsinformationen").substringBefore("<!-- Payment footer")
        assertTrue(paymentBlock.contains("COBADEFFXXX"))
    }

    @Test
    fun `screen payment section omits BIC when empty`() {
        val noBicProfile = testProfile.copy(bic = "")
        val html = generator.generateInvoiceHtml(testInvoice, noBicProfile, testStudio, testClasses)

        val paymentBlock = html.substringAfter("Zahlungsinformationen").substringBefore("<!-- Payment footer")
        assertFalse(paymentBlock.contains("<strong>BIC:</strong>"))
    }

    @Test
    fun `screen payment section shows Verwendungszweck`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val paymentBlock = html.substringAfter("Zahlungsinformationen").substringBefore("<!-- Payment footer")
        assertTrue(paymentBlock.contains("Verwendungszweck:</strong> 2026-01-001"))
    }

    // --- Edge cases ---

    @Test
    fun `empty yoga classes list produces empty tbody`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, emptyList())

        val tbody = html.substringAfter("<tbody>").substringBefore("</tbody>")
        assertFalse(tbody.contains("<tr>"))
    }

    @Test
    fun `all table column headers are present`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        val thead = html.substringAfter("<thead>").substringBefore("</thead>")
        assertTrue(thead.contains("Datum"))
        assertTrue(thead.contains("Uhrzeit"))
        assertTrue(thead.contains("Dauer"))
        assertTrue(thead.contains("Stundensatz"))
        assertTrue(thead.contains("Betrag"))
    }

    @Test
    fun `Datum header is left-aligned via first-child CSS rule`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        // Datum th has no explicit class - it relies on th:first-child { text-align: left }
        assertTrue(html.contains("<th>Datum</th>"))
        assertTrue(html.contains("th:first-child"))
    }

    @Test
    fun `Stundensatz and Betrag headers are right-aligned`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("""<th class="text-right">Stundensatz</th>"""))
        assertTrue(html.contains("""<th class="text-right">Betrag</th>"""))
    }

    @Test
    fun `Dauer header is center-aligned`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        assertTrue(html.contains("""<th class="text-center">Dauer</th>"""))
    }

    @Test
    fun `payment footer due date matches meta table due date`() {
        val html = generator.generateInvoiceHtml(testInvoice, testProfile, testStudio, testClasses)

        // Extract the due date from the meta table
        val metaDueDate = Regex("""Zahlbar bis:</td>\s*<td>(\d{2}\.\d{2}\.\d{4})</td>""")
            .find(html)?.groupValues?.get(1)
        assertNotNull("Due date should be in meta table", metaDueDate)

        // The same date should appear in the payment footer
        val footerBlock = html.substringAfter("payment-footer-left")
        assertTrue(
            "Footer due date should match meta table due date",
            footerBlock.contains("Zahlbar bis $metaDueDate")
        )
    }
}
