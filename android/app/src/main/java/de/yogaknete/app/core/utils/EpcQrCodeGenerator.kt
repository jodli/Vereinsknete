package de.yogaknete.app.core.utils

import android.graphics.Bitmap
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject

class EpcQrCodeGenerator @Inject constructor() {

    private val amountFormat = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))

    /**
     * Generates an EPC QR code (GiroCode) as a Base64-encoded PNG data string.
     *
     * The EPC QR standard is defined by the European Payments Council and is
     * widely supported by German banking apps for SEPA credit transfers.
     *
     * @param bic BIC/SWIFT code of the recipient's bank
     * @param recipientName Name of the payment recipient (max 70 chars)
     * @param iban IBAN of the recipient (spaces are stripped)
     * @param amount Payment amount in EUR
     * @param reference Payment reference / Verwendungszweck (max 140 chars)
     * @param size QR code image size in pixels
     * @return Base64-encoded PNG string, or null if generation fails
     */
    fun generateBase64Png(
        bic: String,
        recipientName: String,
        iban: String,
        amount: Double,
        reference: String,
        size: Int = 300
    ): String? {
        val payload = buildEpcPayload(bic, recipientName, iban, amount, reference)
        val bitmap = generateQrBitmap(payload, size) ?: return null
        return bitmapToBase64Png(bitmap)
    }

    /**
     * Builds the EPC QR payload string according to the EPC069-12 standard.
     *
     * Format:
     * BCD                     - Service Tag
     * 002                     - Version
     * 1                       - Character set (1 = UTF-8)
     * SCT                     - Identification (SEPA Credit Transfer)
     * {BIC}                   - BIC of the beneficiary bank
     * {Name}                  - Name of the beneficiary
     * {IBAN}                  - IBAN of the beneficiary
     * EUR{Amount}             - Amount in EUR
     *                         - Purpose (empty)
     * {Reference}             - Remittance information (unstructured)
     *                         - Information to beneficiary (empty)
     */
    internal fun buildEpcPayload(
        bic: String,
        recipientName: String,
        iban: String,
        amount: Double,
        reference: String
    ): String {
        val cleanIban = iban.replace(" ", "")
        val cleanBic = bic.trim()
        val truncatedName = recipientName.take(70)
        val truncatedRef = reference.take(140)

        return listOf(
            "BCD",
            "002",
            "1",
            "SCT",
            cleanBic,
            truncatedName,
            cleanIban,
            "EUR${amountFormat.format(amount)}",
            "",
            truncatedRef,
            ""
        ).joinToString("\n")
    }

    private fun generateQrBitmap(payload: String, size: Int): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun bitmapToBase64Png(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
