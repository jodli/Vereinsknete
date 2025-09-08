package de.yogaknete.app.presentation.screens.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.presentation.components.TimePickerField
import de.yogaknete.app.presentation.components.DurationPickerField
import de.yogaknete.app.ui.theme.YogaPurple
import kotlinx.datetime.*

@Composable
fun QuickAddDialog(
    date: LocalDate,
    templates: List<ClassTemplate>,
    onTemplateSelected: (ClassTemplate, LocalTime?, Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTemplate by remember { mutableStateOf<ClassTemplate?>(null) }
    var overrideTime by remember { mutableStateOf(false) }
    var customStartTime by remember { mutableStateOf<LocalTime?>(null) }
    var overrideDuration by remember { mutableStateOf(false) }
    var customDuration by remember { mutableStateOf("1.25") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Schnell hinzufügen",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = formatDate(date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedTemplate == null) {
                    // Template-Auswahl
                    Text(
                        text = "Vorlage wählen:",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val dayTemplates = templates.filter { 
                            it.dayOfWeek == date.dayOfWeek && it.isActive 
                        }
                        val otherTemplates = templates.filter { 
                            it.dayOfWeek != date.dayOfWeek && it.isActive 
                        }
                        
                        if (dayTemplates.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Vorlagen für ${getDayName(date.dayOfWeek)}:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = YogaPurple,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(dayTemplates) { template ->
                                TemplateListItem(
                                    template = template,
                                    isRecommended = true,
                                    onClick = { selectedTemplate = template }
                                )
                            }
                        }
                        
                        if (otherTemplates.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Andere Vorlagen:",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(otherTemplates) { template ->
                                TemplateListItem(
                                    template = template,
                                    isRecommended = false,
                                    onClick = { selectedTemplate = template }
                                )
                            }
                        }
                        
                        if (templates.isEmpty()) {
                            item {
                                Text(
                                    text = "Keine Vorlagen verfügbar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Anpassungsoptionen für ausgewählte Vorlage
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = selectedTemplate!!.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedTemplate!!.className,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${formatTime(selectedTemplate!!.startTime)} - ${formatTime(selectedTemplate!!.endTime)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Zeit-Override
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = overrideTime,
                            onCheckedChange = { overrideTime = it }
                        )
                        Text(
                            text = "Andere Startzeit",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (overrideTime) {
                        TimePickerField(
                            time = customStartTime ?: selectedTemplate!!.startTime,
                            onTimeSelected = { customStartTime = it },
                            label = "Startzeit",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 40.dp, bottom = 8.dp)
                        )
                    }
                    
                    // Dauer-Override
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = overrideDuration,
                            onCheckedChange = { overrideDuration = it }
                        )
                        Text(
                            text = "Andere Dauer",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (overrideDuration) {
                        DurationPickerField(
                            durationHours = customDuration.toDoubleOrNull() ?: selectedTemplate!!.duration,
                            onDurationSelected = { customDuration = it.toString() },
                            label = "Dauer",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 40.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTemplate != null) {
                TextButton(
                    onClick = {
                        val startTime = if (overrideTime) customStartTime else null
                        val duration = if (overrideDuration) {
                            customDuration.toDoubleOrNull() ?: selectedTemplate!!.duration
                        } else null
                        
                        onTemplateSelected(selectedTemplate!!, startTime, duration)
                    }
                ) {
                    Text("Kurs erstellen")
                }
            }
        },
        dismissButton = {
            if (selectedTemplate != null) {
                TextButton(onClick = { selectedTemplate = null }) {
                    Text("Zurück")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Abbrechen")
                }
            }
        }
    )
}

@Composable
private fun TemplateListItem(
    template: ClassTemplate,
    isRecommended: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isRecommended) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = template.className,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${formatTime(template.startTime)} - ${formatTime(template.endTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isRecommended) {
                Badge {
                    Text("Empfohlen")
                }
            }
        }
    }
}


private fun formatDate(date: LocalDate): String {
    val dayNames = listOf("So", "Mo", "Di", "Mi", "Do", "Fr", "Sa")
    val monthNames = listOf(
        "Jan", "Feb", "Mär", "Apr", "Mai", "Jun",
        "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"
    )
    
    return "${dayNames[date.dayOfWeek.value % 7]}, ${date.dayOfMonth}. ${monthNames[date.monthNumber - 1]} ${date.year}"
}

private fun formatTime(time: LocalTime): String {
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

private fun getDayName(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Montag"
        DayOfWeek.TUESDAY -> "Dienstag"
        DayOfWeek.WEDNESDAY -> "Mittwoch"
        DayOfWeek.THURSDAY -> "Donnerstag"
        DayOfWeek.FRIDAY -> "Freitag"
        DayOfWeek.SATURDAY -> "Samstag"
        DayOfWeek.SUNDAY -> "Sonntag"
    }
}
