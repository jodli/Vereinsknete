package de.yogaknete.app.core.utils

import androidx.compose.ui.text.AnnotatedString
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for StringUtils - string formatting helper functions
 */
class StringUtilsTest {

    @Test
    fun `formatAsIban formats German IBAN correctly`() {
        val iban = "DE89370400440532013000"

        val formatted = iban.formatAsIban()

        assertEquals("DE89 3704 0044 0532 0130 00", formatted)
    }

    @Test
    fun `formatAsIban handles short string`() {
        val short = "DE89"

        val formatted = short.formatAsIban()

        assertEquals("DE89", formatted)
    }

    @Test
    fun `formatAsIban handles empty string`() {
        val empty = ""

        val formatted = empty.formatAsIban()

        assertEquals("", formatted)
    }

    @Test
    fun `formatAsIban strips existing spaces before formatting`() {
        val iban = "DE89 2385 8444 8353 4128 57"

        val formatted = iban.formatAsIban()

        assertEquals("DE89 2385 8444 8353 4128 57", formatted)
    }

    @Test
    fun `formatAsIban handles partial group at end`() {
        val iban = "DE8937040044053201300" // 21 chars, last group has 1 char

        val formatted = iban.formatAsIban()

        assertEquals("DE89 3704 0044 0532 0130 0", formatted)
    }

    @Test
    fun `IbanVisualTransformation formats text correctly`() {
        val transformation = IbanVisualTransformation()
        val input = AnnotatedString("DE89370400440532013000")

        val result = transformation.filter(input)

        assertEquals("DE89 3704 0044 0532 0130 00", result.text.text)
    }

    @Test
    fun `IbanVisualTransformation handles empty input`() {
        val transformation = IbanVisualTransformation()
        val input = AnnotatedString("")

        val result = transformation.filter(input)

        assertEquals("", result.text.text)
    }

    @Test
    fun `IbanVisualTransformation originalToTransformed maps correctly`() {
        val transformation = IbanVisualTransformation()
        val input = AnnotatedString("DE89370400440532013000")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Position 0 (D) -> 0
        assertEquals(0, offsetMapping.originalToTransformed(0))
        // Position 4 (3) -> 5 (after first space)
        assertEquals(5, offsetMapping.originalToTransformed(4))
        // Position 8 (0) -> 10 (after second space)
        assertEquals(10, offsetMapping.originalToTransformed(8))
    }

    @Test
    fun `IbanVisualTransformation transformedToOriginal maps correctly`() {
        val transformation = IbanVisualTransformation()
        val input = AnnotatedString("DE89370400440532013000")
        val result = transformation.filter(input)
        val offsetMapping = result.offsetMapping

        // Position 0 (D) -> 0
        assertEquals(0, offsetMapping.transformedToOriginal(0))
        // Position 5 (3, after space) -> 4
        assertEquals(4, offsetMapping.transformedToOriginal(5))
        // Position 10 (0, after second space) -> 8
        assertEquals(8, offsetMapping.transformedToOriginal(10))
    }
}
