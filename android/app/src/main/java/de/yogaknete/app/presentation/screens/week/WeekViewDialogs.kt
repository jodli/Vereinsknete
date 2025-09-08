package de.yogaknete.app.presentation.screens.week

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.yogaknete.app.core.utils.DateUtils
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.presentation.components.TimeInputField
import de.yogaknete.app.presentation.components.DurationInputField
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassDialog(
    studios: List<Studio>,
    weekDays: List<LocalDate>,
    templates: List<ClassTemplate>,
    onDismiss: () -> Unit,
    onConfirm: (studioId: Long, title: String, date: LocalDate, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit
) {
    var selectedStudioId by remember { mutableLongStateOf(studios.firstOrNull()?.id ?: 0L) }
    var title by remember { mutableStateOf("Hatha Yoga") }
    var selectedDate by remember { mutableStateOf(weekDays.firstOrNull() ?: LocalDate(2024, 1, 1)) }
    var startHour by remember { mutableIntStateOf(17) }
    var startMinute by remember { mutableIntStateOf(30) }
    var durationMinutes by remember { mutableIntStateOf(75) } // Default: 1.25 hours = 75 minutes
    var expanded by remember { mutableStateOf(false) }
    var templateExpanded by remember { mutableStateOf(false) }
    var selectedTemplate: ClassTemplate? by remember { mutableStateOf(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Calculate end time based on start time + duration
    val endHour = (startHour * 60 + startMinute + durationMinutes) / 60
    val endMinute = (startHour * 60 + startMinute + durationMinutes) % 60
    
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
                
                // Optional: Template selection
                TemplateSelector(
                    selectedDate = selectedDate,
                    templates = templates,
                    selectedTemplate = selectedTemplate,
                    onTemplateSelected = { tmpl ->
                        selectedTemplate = tmpl
                        // apply template values
                        selectedStudioId = tmpl.studioId
                        title = tmpl.className
                        startHour = tmpl.startTime.hour
                        startMinute = tmpl.startTime.minute
                        durationMinutes = (tmpl.duration * 60).toInt()
                    }
                )
                
                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Kursname") },
                    placeholder = { Text("z.B. Hatha Yoga") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Date selection with date picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    OutlinedTextField(
                        value = DateUtils.formatDayDate(selectedDate),
                        onValueChange = { },
                        label = { Text("Datum") },
                        readOnly = true,
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePicker = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Event,
                                    contentDescription = "Datum auswählen"
                                )
                            }
                        }
                    )
                }
                
                // Time and duration inputs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeInputField(
                        label = "Startzeit",
                        hour = startHour,
                        minute = startMinute,
                        onHourChange = { startHour = it },
                        onMinuteChange = { startMinute = it },
                        modifier = Modifier.weight(1f)
                    )
                    
                    DurationInputField(
                        label = "Dauer",
                        durationMinutes = durationMinutes,
                        onDurationChange = { durationMinutes = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Show calculated end time
                Text(
                    text = "Endzeit: ${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 16.dp)
                )
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
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDays() * 24 * 60 * 60 * 1000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val localDateTime = instant.toLocalDateTime(TimeZone.UTC)
                            selectedDate = localDateTime.date
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ClassActionDialog(
    yogaClass: YogaClass,
    studio: Studio?,
    onDismiss: () -> Unit,
    onMarkCompleted: () -> Unit,
    onMarkCancelled: () -> Unit,
    onEdit: () -> Unit,
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
                    text = "${DateUtils.formatTimeRange(yogaClass.startTime, yogaClass.endTime)} (${String.format(Locale.GERMAN, "%.2f", yogaClass.durationHours).replace(".", ",")} Std.)",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = yogaClass.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Always show all action buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mark as completed
                    val isCompleted = yogaClass.status == ClassStatus.COMPLETED
                    Card(
                        onClick = onMarkCompleted,
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompleted) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        ),
                        border = if (isCompleted) 
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
                        else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Durchgeführt",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isCompleted) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    // Mark as cancelled
                    val isCancelled = yogaClass.status == ClassStatus.CANCELLED
                    Card(
                        onClick = onMarkCancelled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCancelled)
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                        ),
                        border = if (isCancelled) 
                            BorderStroke(2.dp, MaterialTheme.colorScheme.error) 
                        else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Ausgefallen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isCancelled) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isCancelled) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    // Edit/Reschedule button
                    Card(
                        onClick = onEdit,
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Bearbeiten/Verschieben",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    // Delete option for completed or cancelled classes
                    if (yogaClass.status != ClassStatus.SCHEDULED) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kurs löschen")
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateSelector(
    selectedDate: LocalDate,
    templates: List<ClassTemplate>,
    selectedTemplate: ClassTemplate?,
    onTemplateSelected: (ClassTemplate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dayTemplates = remember(selectedDate, templates) {
        templates.filter { it.isActive && it.dayOfWeek == selectedDate.dayOfWeek }
    }
    val otherTemplates = remember(selectedDate, templates) {
        templates.filter { it.isActive && it.dayOfWeek != selectedDate.dayOfWeek }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Vorlage (optional)",
            style = MaterialTheme.typography.labelLarge
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedTemplate?.name ?: "Keine Vorlage",
                onValueChange = {},
                readOnly = true,
                label = { Text("Vorlage auswählen") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (dayTemplates.isNotEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Empfohlen für ${getDayName(selectedDate.dayOfWeek)}", fontWeight = FontWeight.Bold) },
                        onClick = { },
                        enabled = false
                    )
                    dayTemplates.forEach { tmpl ->
                        DropdownMenuItem(
                            text = { Text("${tmpl.name} (${formatTime(tmpl.startTime)})") },
                            onClick = {
                                onTemplateSelected(tmpl)
                                expanded = false
                            }
                        )
                    }
                }
                if (otherTemplates.isNotEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Andere Vorlagen", fontWeight = FontWeight.Bold) },
                        onClick = { },
                        enabled = false
                    )
                    otherTemplates.forEach { tmpl ->
                        DropdownMenuItem(
                            text = { Text("${tmpl.name} (${getDayName(tmpl.dayOfWeek)})") },
                            onClick = {
                                onTemplateSelected(tmpl)
                                expanded = false
                            }
                        )
                    }
                }
                if (dayTemplates.isEmpty() && otherTemplates.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Keine Vorlagen verfügbar") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                }
            }
        }
    }
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

private fun formatTime(time: LocalTime): String {
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkCancelDialog(
    classes: List<YogaClass>,
    studios: List<Studio>,
    onDismiss: () -> Unit,
    onConfirm: (List<Long>) -> Unit
) {
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val scheduledClasses = remember(classes) {
        classes.filter { it.status == ClassStatus.SCHEDULED }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Massenstorno",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Wähle die Kurse aus, die storniert werden sollen:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (scheduledClasses.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Keine geplanten Kurse in dieser Woche",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Select/Deselect all
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedIds.size} von ${scheduledClasses.size} ausgewählt",
                            style = MaterialTheme.typography.labelMedium
                        )
                        TextButton(
                            onClick = {
                                selectedIds = if (selectedIds.size == scheduledClasses.size) {
                                    emptySet()
                                } else {
                                    scheduledClasses.map { it.id }.toSet()
                                }
                            }
                        ) {
                            Text(
                                text = if (selectedIds.size == scheduledClasses.size) "Keine auswählen" else "Alle auswählen"
                            )
                        }
                    }
                    
                    HorizontalDivider()
                    
                    // List of classes
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        scheduledClasses.forEach { yogaClass ->
                            val studio = studios.find { it.id == yogaClass.studioId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedIds = if (selectedIds.contains(yogaClass.id)) {
                                            selectedIds - yogaClass.id
                                        } else {
                                            selectedIds + yogaClass.id
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedIds.contains(yogaClass.id))
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedIds.contains(yogaClass.id),
                                        onCheckedChange = { checked ->
                                            selectedIds = if (checked) {
                                                selectedIds + yogaClass.id
                                            } else {
                                                selectedIds - yogaClass.id
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = DateUtils.formatDayDate(yogaClass.startTime.date),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${DateUtils.formatTimeRange(yogaClass.startTime, yogaClass.endTime)} • ${studio?.name ?: "Unbekannt"}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = yogaClass.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedIds.isNotEmpty()) {
                        onConfirm(selectedIds.toList())
                    }
                },
                enabled = selectedIds.isNotEmpty()
            ) {
                Text("Stornieren (${selectedIds.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClassDialog(
    yogaClass: YogaClass,
    studios: List<Studio>,
    onDismiss: () -> Unit,
    onConfirm: (date: LocalDate, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit
) {
    var selectedDate by remember { mutableStateOf(yogaClass.startTime.date) }
    var startHour by remember { mutableIntStateOf(yogaClass.startTime.hour) }
    var startMinute by remember { mutableIntStateOf(yogaClass.startTime.minute) }
    val currentDurationMinutes = remember(yogaClass) {
        val startMinutes = yogaClass.startTime.hour * 60 + yogaClass.startTime.minute
        val endMinutes = yogaClass.endTime.hour * 60 + yogaClass.endTime.minute
        endMinutes - startMinutes
    }
    var durationMinutes by remember { mutableIntStateOf(currentDurationMinutes) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Calculate end time based on start time + duration
    val endHour = (startHour * 60 + startMinute + durationMinutes) / 60
    val endMinute = (startHour * 60 + startMinute + durationMinutes) % 60
    
    val studio = studios.find { it.id == yogaClass.studioId }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Kurs bearbeiten",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Show current class info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = studio?.name ?: "Unbekanntes Studio",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = yogaClass.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Date selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    OutlinedTextField(
                        value = DateUtils.formatDayDate(selectedDate),
                        onValueChange = { },
                        label = { Text("Datum") },
                        readOnly = true,
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePicker = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Event,
                                    contentDescription = "Datum auswählen"
                                )
                            }
                        }
                    )
                }
                
                // Time and duration inputs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeInputField(
                        label = "Startzeit",
                        hour = startHour,
                        minute = startMinute,
                        onHourChange = { startHour = it },
                        onMinuteChange = { startMinute = it },
                        modifier = Modifier.weight(1f)
                    )
                    
                    DurationInputField(
                        label = "Dauer",
                        durationMinutes = durationMinutes,
                        onDurationChange = { durationMinutes = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Show calculated end time
                Text(
                    text = "Endzeit: ${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedDate, startHour, startMinute, endHour, endMinute)
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDays() * 24 * 60 * 60 * 1000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val localDateTime = instant.toLocalDateTime(TimeZone.UTC)
                            selectedDate = localDateTime.date
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun calculateDuration(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Double {
    val startMinutes = startHour * 60 + startMinute
    val endMinutes = endHour * 60 + endMinute
    val durationMinutes = if (endMinutes > startMinutes) {
        endMinutes - startMinutes
    } else {
        0
    }
    return durationMinutes / 60.0
}

@Composable
fun WeekStatsDialog(
    weekStart: LocalDate,
    weekEnd: LocalDate,
    totalClasses: Int,
    totalHours: Double,
    totalEarnings: Double,
    earningsPerStudio: Map<Long, Double>,
    studios: List<Studio>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Wochenstatistik",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Week range
                Text(
                    text = DateUtils.formatWeekRange(weekStart, weekEnd),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                // Summary card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Erledigte Kurse:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "$totalClasses",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gesamt Stunden:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = String.format(Locale.GERMAN, "%.2f", totalHours).replace(".", ","),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gesamt Verdienst:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "€ ${String.format(Locale.GERMAN, "%.2f", totalEarnings).replace(".", ",")}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Earnings per studio
                if (earningsPerStudio.isNotEmpty()) {
                    Text(
                        text = "Verdienst pro Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        earningsPerStudio.forEach { (studioId, earnings) ->
                            val studio = studios.find { it.id == studioId }
                            if (studio != null && earnings > 0) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.fillMaxWidth()
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
                                                text = studio.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "€${studio.hourlyRate}/Stunde",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        Text(
                                            text = "€ ${String.format(Locale.GERMAN, "%.2f", earnings).replace(".", ",")}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Noch keine erledigten Kurse diese Woche",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun MonthlyStatsDialog(
    month: Int,
    year: Int,
    totalClasses: Int,
    totalHours: Double,
    totalEarnings: Double,
    earningsPerStudio: Map<Long, Double>,
    studios: List<Studio>,
    onDismiss: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthName = when (month) {
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
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Vorheriger Monat"
                    )
                }
                Text(
                    text = "$monthName $year",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Nächster Monat"
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Erledigte Kurse:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "$totalClasses",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gesamt Stunden:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = String.format(Locale.GERMAN, "%.2f", totalHours).replace(".", ","),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monats-Verdienst:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "€ ${String.format(Locale.GERMAN, "%.2f", totalEarnings).replace(".", ",")}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Earnings per studio
                if (earningsPerStudio.isNotEmpty()) {
                    Text(
                        text = "Verdienst pro Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, false)
                    ) {
                        earningsPerStudio
                            .toList()
                            .sortedByDescending { it.second }
                            .forEach { (studioId, earnings) ->
                                val studio = studios.find { it.id == studioId }
                                if (studio != null && earnings > 0) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier.fillMaxWidth()
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
                                                    text = studio.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                // Calculate and show number of hours for this studio
                                                val hours = earnings / studio.hourlyRate
                                                Text(
                                                    text = "${String.format(Locale.GERMAN, "%.2f", hours).replace(".", ",")} Stunden • €${studio.hourlyRate}/Std",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                            Text(
                                                text = "€ ${String.format(Locale.GERMAN, "%.2f", earnings).replace(".", ",")}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Noch keine erledigten Kurse in diesem Monat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Comparison with previous month (optional future feature)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}
