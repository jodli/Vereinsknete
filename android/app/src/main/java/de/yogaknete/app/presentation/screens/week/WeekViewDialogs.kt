package de.yogaknete.app.presentation.screens.week

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.yogaknete.app.core.utils.DateUtils
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.YogaClass
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassDialog(
    studios: List<Studio>,
    weekDays: List<LocalDate>,
    onDismiss: () -> Unit,
    onConfirm: (studioId: Long, title: String, date: LocalDate, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit
) {
    var selectedStudioId by remember { mutableStateOf(studios.firstOrNull()?.id ?: 0L) }
    var title by remember { mutableStateOf("Hatha Yoga") }
    var selectedDate by remember { mutableStateOf(weekDays.firstOrNull() ?: LocalDate(2024, 1, 1)) }
    var startHour by remember { mutableStateOf(17) }
    var startMinute by remember { mutableStateOf(30) }
    var endHour by remember { mutableStateOf(18) }
    var endMinute by remember { mutableStateOf(45) }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Neuer Yoga-Kurs",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Studio selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = studios.find { it.id == selectedStudioId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Studio") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        studios.forEach { studio ->
                            DropdownMenuItem(
                                text = { Text(studio.name) },
                                onClick = {
                                    selectedStudioId = studio.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Kursname") },
                    placeholder = { Text("z.B. Hatha Yoga") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Date selection (simplified - could be improved)
                Text(
                    text = "Tag: ${DateUtils.formatDayDate(selectedDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Time inputs (simplified)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = String.format("%02d:%02d", startHour, startMinute),
                        onValueChange = { },
                        label = { Text("Von") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = String.format("%02d:%02d", endHour, endMinute),
                        onValueChange = { },
                        label = { Text("Bis") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedStudioId > 0 && title.isNotBlank()) {
                        onConfirm(
                            selectedStudioId,
                            title,
                            selectedDate,
                            startHour,
                            startMinute,
                            endHour,
                            endMinute
                        )
                    }
                }
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun ClassActionDialog(
    yogaClass: YogaClass,
    studio: Studio?,
    onDismiss: () -> Unit,
    onMarkCompleted: () -> Unit,
    onMarkCancelled: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Kurs abschließen?",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = studio?.name ?: "Unbekanntes Studio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = DateUtils.formatDayDate(yogaClass.startTime.date),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "${DateUtils.formatTimeRange(yogaClass.startTime, yogaClass.endTime)} (${String.format("%.2f", yogaClass.durationHours).replace(".", ",")} Std.)",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = yogaClass.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                if (yogaClass.status == ClassStatus.SCHEDULED) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Mark as completed
                        Card(
                            onClick = onMarkCompleted,
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "✅ Durchgeführt",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        
                        // Mark as cancelled
                        Card(
                            onClick = onMarkCancelled,
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "🚫 Ausgefallen",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                } else {
                    // Show current status
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (yogaClass.status) {
                                ClassStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ClassStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Text(
                            text = when (yogaClass.status) {
                                ClassStatus.COMPLETED -> "✅ Kurs wurde durchgeführt"
                                ClassStatus.CANCELLED -> "🚫 Kurs ist ausgefallen"
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Delete option for non-scheduled classes
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Löschen")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
