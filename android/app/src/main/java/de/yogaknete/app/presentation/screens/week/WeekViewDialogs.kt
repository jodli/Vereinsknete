package de.yogaknete.app.presentation.screens.week

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassDialog(
    studios: List<Studio>,
    weekDays: List<LocalDate>,
    templates: List<ClassTemplate>,
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
    var templateExpanded by remember { mutableStateOf(false) }
    var selectedTemplate: ClassTemplate? by remember { mutableStateOf(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
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
                        endHour = tmpl.endTime.hour
                        endMinute = tmpl.endTime.minute
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
                
                // Time inputs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Startzeit",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = startHour.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { h ->
                                        if (h in 0..23) startHour = h
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Std") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = startMinute.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { m ->
                                        if (m in 0..59) startMinute = m
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Min") },
                                singleLine = true
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Endzeit",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = endHour.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { h ->
                                        if (h in 0..23) endHour = h
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Std") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = endMinute.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { m ->
                                        if (m in 0..59) endMinute = m
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Min") },
                                singleLine = true
                            )
                        }
                    }
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
                                    imageVector = Icons.Outlined.CheckCircleOutline,
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
                                    imageVector = Icons.Default.Cancel,
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
                                    text = "📝 Bearbeiten/Verschieben",
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
                    .menuAnchor()
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
                    Text(
                        text = "Keine geplanten Kurse in dieser Woche.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
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
                    
                    Divider()
                    
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
    var startHour by remember { mutableStateOf(yogaClass.startTime.hour) }
    var startMinute by remember { mutableStateOf(yogaClass.startTime.minute) }
    var endHour by remember { mutableStateOf(yogaClass.endTime.hour) }
    var endMinute by remember { mutableStateOf(yogaClass.endTime.minute) }
    var showDatePicker by remember { mutableStateOf(false) }
    
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
                
                // Time inputs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Startzeit",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = startHour.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { h ->
                                        if (h in 0..23) startHour = h
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Std") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = startMinute.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { m ->
                                        if (m in 0..59) startMinute = m
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Min") },
                                singleLine = true
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Endzeit",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = endHour.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { h ->
                                        if (h in 0..23) endHour = h
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Std") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = endMinute.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { m ->
                                        if (m in 0..59) endMinute = m
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Min") },
                                singleLine = true
                            )
                        }
                    }
                }
                
                // Duration display
                val duration = calculateDuration(startHour, startMinute, endHour, endMinute)
                if (duration > 0) {
                    Text(
                        text = "Dauer: ${String.format("%.2f", duration).replace(".", ",")} Stunden",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
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
                                text = String.format("%.2f", totalHours).replace(".", ","),
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
                                text = "€ ${String.format("%.2f", totalEarnings).replace(".", ",")}",
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
                                            text = "€ ${String.format("%.2f", earnings).replace(".", ",")}",
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
                    Text(
                        text = "Noch keine erledigten Kurse diese Woche.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                                text = String.format("%.2f", totalHours).replace(".", ","),
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
                                text = "€ ${String.format("%.2f", totalEarnings).replace(".", ",")}",
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
                                                    text = "${String.format("%.2f", hours).replace(".", ",")} Stunden • €${studio.hourlyRate}/Std",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                            Text(
                                                text = "€ ${String.format("%.2f", earnings).replace(".", ",")}",
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
                    Text(
                        text = "Noch keine erledigten Kurse in diesem Monat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
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
