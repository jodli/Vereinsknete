package de.yogaknete.app.domain.service

import android.app.Activity
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.qualifiers.ApplicationContext
import de.yogaknete.app.domain.model.Invoice
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.model.YogaClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class InvoicePdfService @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val htmlGenerator: InvoiceHtmlGenerator
) {
    
    /**
     * Generates a PDF invoice and triggers the Android print dialog
     * Requires an Activity context to work properly
     */
    suspend fun generateAndPrintPdf(
        activity: Activity,
        invoice: Invoice,
        userProfile: UserProfile,
        studio: Studio,
        yogaClasses: List<YogaClass>
    ) = withContext(Dispatchers.Main) {
        val html = htmlGenerator.generateInvoiceHtml(
            invoice = invoice,
            userProfile = userProfile,
            studio = studio,
            yogaClasses = yogaClasses
        )
        
        // Create WebView to render HTML
        val webView = WebView(activity).apply {
            settings.apply {
                javaScriptEnabled = false
                loadWithOverviewMode = true
                useWideViewPort = true
            }
        }
        
        // Load HTML content
        webView.loadDataWithBaseURL(
            null,
            html,
            "text/html",
            "UTF-8",
            null
        )
        
        // Wait for the page to load
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                
                // Create print job
                createPrintJob(activity, webView, invoice.invoiceNumber)
            }
        }
    }
    
    /**
     * Generates a PDF invoice and saves it to a file
     */
    suspend fun generatePdfToFile(
        invoice: Invoice,
        userProfile: UserProfile,
        studio: Studio,
        yogaClasses: List<YogaClass>,
        outputFile: File
    ): File = withContext(Dispatchers.IO) {
        val html = htmlGenerator.generateInvoiceHtml(
            invoice = invoice,
            userProfile = userProfile,
            studio = studio,
            yogaClasses = yogaClasses
        )
        
        // Save the HTML file (can be opened in a browser or converted to PDF later)
        outputFile.writeText(html)
        outputFile
    }
    
    /**
     * Generates HTML preview for the invoice
     */
    fun generateHtmlPreview(
        invoice: Invoice,
        userProfile: UserProfile,
        studio: Studio,
        yogaClasses: List<YogaClass>
    ): String {
        return htmlGenerator.generateInvoiceHtml(
            invoice = invoice,
            userProfile = userProfile,
            studio = studio,
            yogaClasses = yogaClasses
        )
    }
    
    private fun createPrintJob(activity: Activity, webView: WebView, documentName: String) {
        val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
        
        val printAdapter = webView.createPrintDocumentAdapter(documentName)
        
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
        
        printManager.print(
            documentName,
            printAdapter,
            printAttributes
        )
    }
}
