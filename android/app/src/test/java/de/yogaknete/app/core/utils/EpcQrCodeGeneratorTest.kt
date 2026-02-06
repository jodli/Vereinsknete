package de.yogaknete.app.core.utils

import org.junit.Test
import org.junit.Assert.*

class EpcQrCodeGeneratorTest {

    private val generator = EpcQrCodeGenerator()

    @Test
    fun `buildEpcPayload produces correct EPC069-12 format`() {
        val payload = generator.buildEpcPayload(
            bic = "COBADEFFXXX",
            recipientName = "Max Mustermann",
            iban = "DE89370400440532013000",
            amount = 450.00,
            reference = "RG-2026-01-001"
        )

        val lines = payload.split("\n")
        assertEquals("BCD", lines[0])
        assertEquals("002", lines[1])
        assertEquals("1", lines[2])
        assertEquals("SCT", lines[3])
        assertEquals("COBADEFFXXX", lines[4])
        assertEquals("Max Mustermann", lines[5])
        assertEquals("DE89370400440532013000", lines[6])
        assertEquals("EUR450.00", lines[7])
        assertEquals("", lines[8])
        assertEquals("RG-2026-01-001", lines[9])
        assertEquals("", lines[10])
        assertEquals(11, lines.size)
    }

    @Test
    fun `buildEpcPayload strips spaces from IBAN`() {
        val payload = generator.buildEpcPayload(
            bic = "COBADEFFXXX",
            recipientName = "Test",
            iban = "DE89 3704 0044 0532 0130 00",
            amount = 100.0,
            reference = "REF-001"
        )

        val lines = payload.split("\n")
        assertEquals("DE89370400440532013000", lines[6])
    }

    @Test
    fun `buildEpcPayload trims BIC whitespace`() {
        val payload = generator.buildEpcPayload(
            bic = "  COBADEFFXXX  ",
            recipientName = "Test",
            iban = "DE89370400440532013000",
            amount = 100.0,
            reference = "REF-001"
        )

        val lines = payload.split("\n")
        assertEquals("COBADEFFXXX", lines[4])
    }

    @Test
    fun `buildEpcPayload handles empty BIC`() {
        val payload = generator.buildEpcPayload(
            bic = "",
            recipientName = "Test",
            iban = "DE89370400440532013000",
            amount = 100.0,
            reference = "REF-001"
        )

        val lines = payload.split("\n")
        assertEquals("", lines[4])
    }

    @Test
    fun `buildEpcPayload formats amount with two decimals`() {
        val payload = generator.buildEpcPayload(
            bic = "COBADEFFXXX",
            recipientName = "Test",
            iban = "DE89370400440532013000",
            amount = 1234.50,
            reference = "REF-001"
        )

        val lines = payload.split("\n")
        assertEquals("EUR1234.50", lines[7])
    }

    @Test
    fun `buildEpcPayload formats whole number amount with decimals`() {
        val payload = generator.buildEpcPayload(
            bic = "COBADEFFXXX",
            recipientName = "Test",
            iban = "DE89370400440532013000",
            amount = 300.0,
            reference = "REF-001"
        )

        val lines = payload.split("\n")
        assertEquals("EUR300.00", lines[7])
    }

    @Test
    fun `buildEpcPayload truncates name at 70 characters`() {
        val longName = "A".repeat(100)

        val payload = generator.buildEpcPayload(
            bic = "COBADEFFXXX",
            recipientName = longName,
            iban = "DE89370400440532013000",
            amount = 100.0,
            reference = "REF-001"
        )

        val lines = payload.split("\n")
        assertEquals(70, lines[5].length)
    }

    @Test
    fun `buildEpcPayload truncates reference at 140 characters`() {
        val longRef = "R".repeat(200)

        val payload = generator.buildEpcPayload(
            bic = "COBADEFFXXX",
            recipientName = "Test",
            iban = "DE89370400440532013000",
            amount = 100.0,
            reference = longRef
        )

        val lines = payload.split("\n")
        assertEquals(140, lines[9].length)
    }
}
