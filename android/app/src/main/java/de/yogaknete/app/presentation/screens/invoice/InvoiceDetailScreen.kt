package de.yogaknete.app.presentation.screens.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.yogaknete.app.domain.model.PaymentStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    navController: NavController,
    invoiceId: Long,
    viewModel: InvoiceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Format helpers
    val currencyFormat = remember { DecimalFormat("#,##0.00 €") }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.GERMANY) }
    
    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Rechnung ${uiState.invoice?.invoiceNumber ?: ""}")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            // Get the activity from the context
                            val activity = context as? android.app.Activity
                            if (activity != null) {
                                viewModel.generatePdf(activity)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("PDF-Generierung fehlgeschlagen: Keine Activity gefunden")
                                }
                            }
                        },
                        enabled = !uiState.isPdfGenerating && uiState.invoice != null
                    ) {
                        if (uiState.isPdfGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "PDF generieren"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.invoice != null && uiState.studio != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // Invoice Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Rechnung",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = uiState.invoice!!.invoiceNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            // Payment Status Badge
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = when (uiState.invoice!!.paymentStatus) {
                                    PaymentStatus.PAID -> MaterialTheme.colorScheme.tertiaryContainer
                                    PaymentStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                                    PaymentStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer
                                    PaymentStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = when (uiState.invoice?.paymentStatus) {
                                            PaymentStatus.PAID -> Icons.Default.CheckCircle
                                            else -> Icons.Default.DateRange
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = when (uiState.invoice!!.paymentStatus) {
                                            PaymentStatus.PAID -> MaterialTheme.colorScheme.onTertiaryContainer
                                            PaymentStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                                            PaymentStatus.OVERDUE -> MaterialTheme.colorScheme.onErrorContainer
                                            PaymentStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                    Text(
                                        text = when (uiState.invoice!!.paymentStatus) {
                                            PaymentStatus.PAID -> "Bezahlt"
                                            PaymentStatus.PENDING -> "Offen"
                                            PaymentStatus.OVERDUE -> "Überfällig"
                                            PaymentStatus.CANCELLED -> "Storniert"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when (uiState.invoice!!.paymentStatus) {
                                            PaymentStatus.PAID -> MaterialTheme.colorScheme.onTertiaryContainer
                                            PaymentStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                                            PaymentStatus.OVERDUE -> MaterialTheme.colorScheme.onErrorContainer
                                            PaymentStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Total Amount (show actual calculated amount)
                        Text(
                            text = currencyFormat.format(uiState.actualTotalAmount),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = "${uiState.actualTotalHours} Stunden × ${currencyFormat.format(uiState.invoice!!.hourlyRate)}/Stunde",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Sender and Receiver Information
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sender (User)
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Von",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            uiState.userProfile?.let { profile ->
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (profile.street.isNotEmpty()) {
                                    Text(
                                        text = profile.street,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (profile.postalCode.isNotEmpty() || profile.city.isNotEmpty()) {
                                    Text(
                                        text = "${profile.postalCode} ${profile.city}".trim(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (profile.taxId.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Steuernr: ${profile.taxId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Receiver (Studio)
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "An",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = uiState.studio!!.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (uiState.studio!!.contactPerson.isNotEmpty()) {
                                Text(
                                    text = uiState.studio!!.contactPerson,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (uiState.studio!!.street.isNotEmpty()) {
                                Text(
                                    text = uiState.studio!!.street,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (uiState.studio!!.postalCode.isNotEmpty() || uiState.studio!!.city.isNotEmpty()) {
                                Text(
                                    text = "${uiState.studio!!.postalCode} ${uiState.studio!!.city}".trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Classes List
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Leistungsübersicht",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Yoga-Kurse im ${getMonthName(uiState.invoice!!.month)} ${uiState.invoice!!.year}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Classes table header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Datum",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Zeit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Dauer",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(0.6f),
                                textAlign = TextAlign.End
                            )
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        // Classes list
                        uiState.yogaClasses.forEach { yogaClass ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dateFormat.format(Date(yogaClass.startTime.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds())),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = timeFormat.format(Date(yogaClass.startTime.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds())),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${yogaClass.durationHours} h",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(0.6f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        // Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Gesamt",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${uiState.actualTotalHours} Stunden",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bank Details
                uiState.userProfile?.let { profile ->
                    if (profile.iban.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Bankverbindung",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (profile.bankName.isNotEmpty()) {
                                    Text(
                                        text = profile.bankName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Text(
                                    text = "IBAN: ${profile.iban}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                if (profile.bic.isNotEmpty()) {
                                    Text(
                                        text = "BIC: ${profile.bic}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
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
