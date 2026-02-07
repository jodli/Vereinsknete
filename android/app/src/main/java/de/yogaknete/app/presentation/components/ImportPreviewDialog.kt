package de.yogaknete.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.yogaknete.app.domain.model.BackupMetadata
import de.yogaknete.app.domain.model.ImportStrategy
import de.yogaknete.app.ui.theme.YogaPurple
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ImportPreviewDialog(
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

                Text("\u2022 ${metadata.entryCount.studios} Studios")
                Text("\u2022 ${metadata.entryCount.classes} Kurse")
                Text("\u2022 ${metadata.entryCount.templates} Vorlagen")
                Text("\u2022 ${metadata.entryCount.invoices} Rechnungen")

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
