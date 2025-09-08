package de.yogaknete.app

import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.domain.model.*
import kotlinx.datetime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

/**
 * Test to verify that BackupData serialization works correctly 
 * with all current entity structures after database changes
 */
class BackupCompatibilityTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `BackupData serialization includes all current entity fields`() {
        // Create sample data with all current entity structures
        val userProfile = UserProfile(
            id = 1L,
            name = "Test User",
            street = "Test Street 1",
            postalCode = "12345",
            city = "Test City",
            taxId = "123456789",
            phone = "+49 123 456789",
            email = "test@example.com",
            bankName = "Test Bank",
            iban = "DE89 3704 0044 0532 0130 00",
            bic = "TESTDE01",
            defaultHourlyRate = 50.0,
            isOnboardingComplete = true
        )

        val studio = Studio(
            id = 1L,
            name = "Test Studio",
            contactPerson = "Studio Manager",
            email = "studio@example.com",
            phone = "+49 123 456790",
            street = "Studio Street 1",
            postalCode = "12345",
            city = "Studio City",
            hourlyRate = 60.0,
            isActive = true
        )

        val yogaClass = YogaClass(
            id = 1L,
            studioId = 1L,
            title = "Test Yoga Class",
            startTime = LocalDateTime(2024, 3, 15, 10, 0),
            endTime = LocalDateTime(2024, 3, 15, 11, 15),
            durationHours = 1.25,
            status = ClassStatus.COMPLETED,
            notes = "Test notes",
            creationSource = CreationSource.TEMPLATE,
            sourceTemplateId = 1L
        )

        val classTemplate = ClassTemplate(
            id = 1L,
            name = "Monday Morning Flow",
            studioId = 1L,
            className = "Vinyasa Flow",
            dayOfWeek = DayOfWeek.MONDAY,
            startTime = LocalTime(10, 0),
            endTime = LocalTime(11, 15),
            duration = 1.25,
            isActive = true,
            autoSchedule = false,
            lastScheduledDate = LocalDate(2024, 3, 15)
        )

        val invoice = Invoice(
            id = 1L,
            studioId = 1L,
            invoiceNumber = "2024-03-001",
            month = 3,
            year = 2024,
            totalHours = 10.0,
            hourlyRate = 60.0,
            totalAmount = 600.0,
            paymentStatus = PaymentStatus.PAID,
            createdAt = LocalDateTime(2024, 4, 1, 12, 0),
            paidAt = LocalDateTime(2024, 4, 5, 14, 30),
            notes = "Test invoice",
            pdfPath = "/test/path/invoice.pdf"
        )

        // Create backup data
        val backupData = BackupData(
            version = BackupData.CURRENT_VERSION,
            exportDate = Clock.System.now(),
            appVersion = "1.0.0",
            userProfile = userProfile,
            studios = listOf(studio),
            classes = listOf(yogaClass),
            templates = listOf(classTemplate),
            invoices = listOf(invoice)
        )

        // Test serialization
        val jsonString = json.encodeToString(backupData)
        assertNotNull("Serialization should produce valid JSON", jsonString)
        assertTrue("JSON should contain user profile data", jsonString.contains("Test User"))
        assertTrue("JSON should contain studio data", jsonString.contains("Test Studio"))
        assertTrue("JSON should contain class data", jsonString.contains("Test Yoga Class"))
        assertTrue("JSON should contain template data", jsonString.contains("Monday Morning Flow"))
        assertTrue("JSON should contain invoice data", jsonString.contains("2024-03-001"))

        // Test deserialization
        val deserializedBackup = json.decodeFromString<BackupData>(jsonString)
        
        // Verify all data is correctly deserialized
        assertEquals("Version should match", BackupData.CURRENT_VERSION, deserializedBackup.version)
        assertNotNull("User profile should be present", deserializedBackup.userProfile)
        assertEquals("Studio count should match", 1, deserializedBackup.studios.size)
        assertEquals("Class count should match", 1, deserializedBackup.classes.size)
        assertEquals("Template count should match", 1, deserializedBackup.templates.size)
        assertEquals("Invoice count should match", 1, deserializedBackup.invoices.size)

        // Verify specific field values are preserved
        assertEquals("User profile name should match", "Test User", deserializedBackup.userProfile?.name)
        assertEquals("Studio name should match", "Test Studio", deserializedBackup.studios[0].name)
        assertEquals("Class title should match", "Test Yoga Class", deserializedBackup.classes[0].title)
        assertEquals("Template name should match", "Monday Morning Flow", deserializedBackup.templates[0].name)
        assertEquals("Invoice number should match", "2024-03-001", deserializedBackup.invoices[0].invoiceNumber)

        // Verify foreign key relationships are preserved
        assertEquals("Class should reference correct studio", 1L, deserializedBackup.classes[0].studioId)
        assertEquals("Template should reference correct studio", 1L, deserializedBackup.templates[0].studioId)
        assertEquals("Invoice should reference correct studio", 1L, deserializedBackup.invoices[0].studioId)
        assertEquals("Class should reference correct template", 1L, deserializedBackup.classes[0].sourceTemplateId)
    }

    @Test
    fun `BackupData handles empty collections correctly`() {
        val backupData = BackupData(
            version = BackupData.CURRENT_VERSION,
            exportDate = Clock.System.now(),
            appVersion = "1.0.0",
            userProfile = null,
            studios = emptyList(),
            classes = emptyList(),
            templates = emptyList(),
            invoices = emptyList()
        )

        // Test serialization of empty backup
        val jsonString = json.encodeToString(backupData)
        assertNotNull("Serialization should handle empty data", jsonString)

        // Test deserialization of empty backup
        val deserializedBackup = json.decodeFromString<BackupData>(jsonString)
        assertNull("User profile should be null", deserializedBackup.userProfile)
        assertTrue("Studios should be empty", deserializedBackup.studios.isEmpty())
        assertTrue("Classes should be empty", deserializedBackup.classes.isEmpty())
        assertTrue("Templates should be empty", deserializedBackup.templates.isEmpty())
        assertTrue("Invoices should be empty", deserializedBackup.invoices.isEmpty())
    }

    @Test
    fun `BackupMetadata serialization works correctly`() {
        val metadata = BackupMetadata(
            version = 1,
            exportDate = Clock.System.now(),
            appVersion = "1.0.0",
            entryCount = BackupEntryCount(
                studios = 2,
                classes = 15,
                templates = 5,
                invoices = 3
            )
        )

        val jsonString = json.encodeToString(metadata)
        val deserializedMetadata = json.decodeFromString<BackupMetadata>(jsonString)

        assertEquals("Version should match", 1, deserializedMetadata.version)
        assertEquals("App version should match", "1.0.0", deserializedMetadata.appVersion)
        assertEquals("Studio count should match", 2, deserializedMetadata.entryCount.studios)
        assertEquals("Class count should match", 15, deserializedMetadata.entryCount.classes)
        assertEquals("Template count should match", 5, deserializedMetadata.entryCount.templates)
        assertEquals("Invoice count should match", 3, deserializedMetadata.entryCount.invoices)
    }
}
