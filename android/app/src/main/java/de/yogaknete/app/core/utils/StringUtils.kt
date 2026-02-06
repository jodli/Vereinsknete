package de.yogaknete.app.core.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Formats a string as IBAN in DIN format (groups of 4 separated by spaces).
 * Example: "DE89370400440532013000" → "DE89 3704 0044 0532 0130 00"
 */
fun String.formatAsIban(): String = this.replace("\\s".toRegex(), "").chunked(4).joinToString(" ")

/**
 * VisualTransformation that displays IBAN input in DIN format (groups of 4 separated by spaces).
 * The underlying value remains unformatted for storage.
 */
class IbanVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = text.text.formatAsIban()

        return TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val spacesBeforeOffset = offset / 4
                    return (offset + spacesBeforeOffset).coerceAtMost(formatted.length)
                }

                override fun transformedToOriginal(offset: Int): Int {
                    val spacesBeforeOffset = offset / 5
                    return (offset - spacesBeforeOffset).coerceAtMost(text.text.length)
                }
            }
        )
    }
}
