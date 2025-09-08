package de.yogaknete.app.presentation.screens.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.yogaknete.app.domain.model.ImportStrategy
import de.yogaknete.app.domain.model.BackupMetadata
import de.yogaknete.app.domain.model.BackupResult
import de.yogaknete.app.presentation.theme.YogaPurple
import androidx.compose.foundation.clickable
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupManagementScreen(
    navController: NavController,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // File picker for import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(it) }
    }
    
    // Launch file picker when requested
    LaunchedEffect(uiState.showFilePicker) {
        if (uiState.showFilePicker) {
            filePickerLauncher.launch("application/json")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datensicherung") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Export Section
                ExportSection(
                    onExportJson = { viewModel.exportBackup(BackupFormat.JSON) },
                    onExportZip = { viewModel.exportBackup(BackupFormat.ZIP) },
                    lastBackupResult = uiState.lastBackupResult
                )
                
                // Import Section
                ImportSection(
                    onSelectFile = { viewModel.selectFileForImport() }
                )
                
                // Info Section
                InfoSection()
            }
            
            // Loading overlay
            if (uiState.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Bitte warten...")
                        }
                    }
                }
            }
        }
    }
    
    // Import Preview Dialog
    if (uiState.showImportPreview) {
        uiState.previewMetadata?.let { metadata ->
            ImportPreviewDialog(
                metadata = metadata,
            onConfirm = { strategy ->
                viewModel.confirmImport(strategy)
            },
                onDismiss = { viewModel.cancelImport() }
            )
        }
    }
    
    // Success Snackbar
    if (uiState.showSuccessMessage) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMessages() },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Erfolgreich") },
            text = { Text(uiState.successMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissMessages() }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Error Dialog
    if (uiState.showErrorMessage) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMessages() },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Fehler") },
            text = { Text(uiState.errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissMessages() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ExportSection(
    onExportJson: () -> Unit,
    onExportZip: () -> Unit,
    lastBackupResult: BackupResult.Success?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = YogaPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Daten exportieren",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Sichere alle deine Daten in einer Datei, die du auf deinem Gerät oder in der Cloud speichern kannst.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExportJson,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YogaPurple
                    )
                ) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("JSON")
                }
                
                OutlinedButton(
                    onClick = onExportZip,
                    modifier = Modifier.weight(1f),
                    enabled = false // ZIP not implemented yet
                ) {
                    Icon(Icons.Default.FolderZip, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ZIP")
                }
            }
            
            if (lastBackupResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Letzte Sicherung: ${lastBackupResult.fileName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ImportSection(
    onSelectFile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Upload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Daten importieren",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Stelle deine Daten aus einer zuvor erstellten Sicherungsdatei wieder her.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onSelectFile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Datei auswählen")
            }
        }
    }
}

@Composable
private fun InfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Wichtige Hinweise",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoItem(
                icon = Icons.Default.CloudUpload,
                text = "Backups werden im Downloads-Ordner gespeichert. Du kannst sie manuell in Google Drive oder andere Cloud-Dienste hochladen."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoItem(
                icon = Icons.Default.Timer,
                text = "Erstelle regelmäßig Backups, besonders vor größeren Änderungen oder am Monatsende nach der Rechnungserstellung."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoItem(
                icon = Icons.Default.Lock,
                text = "Deine Daten bleiben immer auf deinem Gerät. Es erfolgt keine automatische Cloud-Synchronisation."
            )
        }
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ImportPreviewDialog(
    metadata: BackupMetadata,
    onConfirm: (ImportStrategy) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStrategy by remember { mutableStateOf(ImportStrategy.MERGE) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Backup-Vorschau",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    "Backup-Details:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val exportDate = metadata.exportDate.toLocalDateTime(TimeZone.currentSystemDefault())
                Text("Erstellt am: ${exportDate.date} um ${exportDate.time}")
                Text("Version: ${metadata.version}")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Enthaltene Daten:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text("• ${metadata.entryCount.studios} Studios")
                Text("• ${metadata.entryCount.classes} Kurse")
                Text("• ${metadata.entryCount.templates} Vorlagen")
                Text("• ${metadata.entryCount.invoices} Rechnungen")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Import-Strategie:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStrategy == ImportStrategy.MERGE,
                            onClick = { selectedStrategy = ImportStrategy.MERGE }
                        )
                        Text(
                            "Zusammenführen",
                            modifier = Modifier.clickable { selectedStrategy = ImportStrategy.MERGE }
                        )
                    }
                    Text(
                        "Bestehende Daten behalten und neue hinzufügen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 40.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStrategy == ImportStrategy.REPLACE,
                            onClick = { selectedStrategy = ImportStrategy.REPLACE }
                        )
                        Text(
                            "Ersetzen",
                            modifier = Modifier.clickable { selectedStrategy = ImportStrategy.REPLACE }
                        )
                    }
                    Text(
                        "Alle Daten löschen und durch Backup ersetzen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 40.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStrategy) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = YogaPurple
                )
            ) {
                Text("Importieren")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
