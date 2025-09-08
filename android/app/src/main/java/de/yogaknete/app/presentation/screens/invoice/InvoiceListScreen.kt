package de.yogaknete.app.presentation.screens.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.yogaknete.app.domain.model.InvoiceSummary
import de.yogaknete.app.domain.model.PaymentStatus
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.GERMANY) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rechnungen") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Month Navigation
            MonthNavigationHeader(
                month = uiState.selectedMonth,
                year = uiState.selectedYear,
                monthName = viewModel.getMonthName(uiState.selectedMonth),
                onPreviousMonth = { viewModel.navigateToPreviousMonth() },
                onNextMonth = { viewModel.navigateToNextMonth() },
                onMonthClick = { viewModel.toggleMonthPicker() }
            )
            
            // Monthly Summary
            if (uiState.invoiceSummaries.isNotEmpty()) {
                MonthlySummaryCard(
                    totalHours = viewModel.getTotalMonthlyHours(),
                    totalAmount = viewModel.getTotalMonthlyAmount(),
                    numberFormat = numberFormat
                )
            }
            
            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Invoice list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.invoiceSummaries.isEmpty() && !uiState.isLoading) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Keine abgeschlossenen Kurse für diesen Monat",
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(uiState.invoiceSummaries) { summary ->
                        InvoiceSummaryCard(
                            summary = summary,
                            numberFormat = numberFormat,
                            onCreateInvoice = { viewModel.createInvoice(summary) },
                            onUpdatePaymentStatus = { status ->
                                summary.invoiceId?.let { id ->
                                    viewModel.updatePaymentStatus(id, status)
                                }
                            },
                                onGeneratePdf = {
                                    // Navigate to invoice detail screen
                                    summary.invoiceId?.let { id ->
                                        navController.navigate("invoice_detail/$id")
                                    }
                                },
                                onDeleteInvoice = {
                                    summary.invoiceId?.let { id ->
                                        viewModel.deleteInvoice(id)
                                    }
                                }
                        )
                    }
                }
            }
            
            // Error handling
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun MonthNavigationHeader(
    month: Int,
    year: Int,
    monthName: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Vorheriger Monat")
            }
            
            TextButton(onClick = onMonthClick) {
                Text(
                    text = "$monthName $year",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Nächster Monat")
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    totalHours: Double,
    totalAmount: Double,
    numberFormat: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Gesamt Stunden",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = String.format("%.2f h", totalHours),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider(
                modifier = Modifier
                    .height(48.dp)
                    .width(1.dp)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Gesamt Betrag",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = numberFormat.format(totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceSummaryCard(
    summary: InvoiceSummary,
    numberFormat: NumberFormat,
    onCreateInvoice: () -> Unit,
    onUpdatePaymentStatus: (PaymentStatus) -> Unit,
    onGeneratePdf: () -> Unit,
    onDeleteInvoice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Studio name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.studioName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (summary.hasExistingInvoice) {
                    PaymentStatusChip(
                        status = summary.paymentStatus ?: PaymentStatus.PENDING
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Class and financial info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${summary.completedClasses} Kurse",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = String.format("%.2f Stunden", summary.totalHours),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = numberFormat.format(summary.hourlyRate) + "/h",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = numberFormat.format(summary.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!summary.hasExistingInvoice) {
                    Button(
                        onClick = onCreateInvoice,
                        modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rechnung erstellen")
                    }
                } else {
                    // Payment status toggle
                    if (summary.paymentStatus != PaymentStatus.PAID) {
                        OutlinedButton(
                            onClick = { onUpdatePaymentStatus(PaymentStatus.PAID) },
                            modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Outlined.AttachMoney,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Als bezahlt markieren")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onUpdatePaymentStatus(PaymentStatus.PENDING) },
                            modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Outlined.PendingActions,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Als ausstehend markieren")
                        }
                    }
                    
                    // View invoice / Generate PDF button
                    Button(
                        onClick = onGeneratePdf,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechnung anzeigen")
                    }
                }
            }
            
            // Delete button for existing invoices
            if (summary.hasExistingInvoice) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDeleteInvoice,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rechnung löschen")
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusChip(status: PaymentStatus) {
    val (color, icon, text) = when (status) {
        PaymentStatus.PAID -> Triple(
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle,
            "Bezahlt"
        )
        PaymentStatus.PENDING -> Triple(
            Color(0xFFFFA726),
            Icons.Outlined.PendingActions,
            "Ausstehend"
        )
        PaymentStatus.OVERDUE -> Triple(
            Color(0xFFEF5350),
            Icons.Default.Warning,
            "Überfällig"
        )
        PaymentStatus.CANCELLED -> Triple(
            Color(0xFF9E9E9E),
            Icons.Default.Cancel,
            "Storniert"
        )
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}
