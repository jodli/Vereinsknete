package de.yogaknete.app.presentation.screens.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.ToggleOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.presentation.theme.YogaPurple
import de.yogaknete.app.presentation.components.TimePickerField
import de.yogaknete.app.presentation.components.DurationPickerField
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: TemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kursvorlagen verwalten") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showTemplateDialog() },
                containerColor = YogaPurple
            ) {
                Icon(Icons.Outlined.PostAdd, contentDescription = "Neue Vorlage")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.templates.isEmpty()) {
                EmptyTemplatesView(
                    modifier = Modifier.fillMaxSize(),
                    onCreateTemplate = { viewModel.showTemplateDialog() }
                )
            } else {
                TemplateList(
                    templates = uiState.templates,
                    onTemplateClick = { template ->
                        viewModel.showTemplateDialog(template)
                    },
                    onToggleActive = { template ->
                        viewModel.toggleTemplateActive(template)
                    },
                    onToggleAutoSchedule = { templateId, enabled ->
                        viewModel.toggleAutoSchedule(templateId, enabled)
                    },
                    onDeleteTemplate = { template ->
                        viewModel.deleteTemplate(template)
                    }
                )
            }
        }
        
        if (uiState.showTemplateDialog) {
            TemplateEditDialog(
                template = uiState.selectedTemplate,
                studios = uiState.studios,
                onSave = { name, studioId, className, dayOfWeek, startTime, duration ->
                    if (uiState.selectedTemplate != null) {
                        val updatedTemplate = uiState.selectedTemplate!!.copy(
                            name = name,
                            studioId = studioId,
                            className = className,
                            dayOfWeek = dayOfWeek,
                            startTime = startTime,
                            endTime = startTime.toSecondOfDay()
                                .plus((duration * 60 * 60).toInt())
                                .let { LocalTime.fromSecondOfDay(it) },
                            duration = duration
                        )
                        viewModel.updateTemplate(updatedTemplate)
                    } else {
                        viewModel.createTemplate(name, studioId, className, dayOfWeek, startTime, duration)
                    }
                },
                onDismiss = { viewModel.hideTemplateDialog() }
            )
        }
    }
}

@Composable
private fun EmptyTemplatesView(
    modifier: Modifier = Modifier,
    onCreateTemplate: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Keine Vorlagen vorhanden",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Erstelle Vorlagen für wiederkehrende Kurse",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateTemplate,
            colors = ButtonDefaults.buttonColors(
                containerColor = YogaPurple
            )
        ) {
            Icon(Icons.Outlined.PostAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Erste Vorlage erstellen")
        }
    }
}

@Composable
private fun TemplateList(
    templates: List<ClassTemplate>,
    onTemplateClick: (ClassTemplate) -> Unit,
    onToggleActive: (ClassTemplate) -> Unit,
    onToggleAutoSchedule: (Long, Boolean) -> Unit,
    onDeleteTemplate: (ClassTemplate) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Group templates by day of week
        val groupedTemplates = templates.groupBy { it.dayOfWeek }
        val sortedDays = DayOfWeek.values().filter { it in groupedTemplates.keys }
        
        sortedDays.forEach { dayOfWeek ->
            item {
                Text(
                    text = getDayName(dayOfWeek),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = YogaPurple
                )
            }
            
            items(groupedTemplates[dayOfWeek] ?: emptyList()) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onTemplateClick(template) },
                    onToggleActive = { onToggleActive(template) },
                    onToggleAutoSchedule = { enabled -> onToggleAutoSchedule(template.id, enabled) },
                    onDelete = { onDeleteTemplate(template) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateCard(
    template: ClassTemplate,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onToggleAutoSchedule: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (template.isActive) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with title and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (template.isActive) FontWeight.Bold else FontWeight.Normal
                        )
                        if (!template.isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Text(
                                    text = "INAKTIV",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    Text(
                        text = template.className,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatTime(template.startTime)} - ${formatTime(template.endTime)} (${template.duration} Std.)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Three-dot menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mehr Optionen"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(if (template.isActive) "Deaktivieren" else "Aktivieren")
                            },
                            onClick = {
                                onToggleActive()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (template.isActive) 
                                        Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Löschen") },
                            onClick = {
                                showDeleteDialog = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
            
            // Auto-schedule section (only if template is active)
            if (template.isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (template.autoSchedule) 
                                    Icons.Default.Schedule else Icons.Default.EditCalendar,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (template.autoSchedule) 
                                    YogaPurple else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Automatisch erstellen",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = if (template.autoSchedule) {
                                if (template.lastScheduledDate != null) {
                                    "Wöchentlich geplant • Zuletzt: ${formatDate(template.lastScheduledDate)}"
                                } else {
                                    "Wöchentlich geplant"
                                }
                            } else {
                                "Kurse müssen manuell erstellt werden"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    Switch(
                        checked = template.autoSchedule,
                        onCheckedChange = onToggleAutoSchedule,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = YogaPurple,
                            checkedTrackColor = YogaPurple.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Vorlage löschen?") },
            text = { 
                Text("Möchtest du die Vorlage '${template.name}' wirklich löschen?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateEditDialog(
    template: ClassTemplate?,
    studios: List<de.yogaknete.app.domain.model.Studio>,
    onSave: (String, Long, String, DayOfWeek, LocalTime, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(template) { mutableStateOf(template?.name ?: "") }
    var selectedStudioId by remember(template) { mutableLongStateOf(template?.studioId ?: studios.firstOrNull()?.id ?: 0L) }
    var className by remember(template) { mutableStateOf(template?.className ?: "") }
    var selectedDayOfWeek by remember(template) { mutableStateOf(template?.dayOfWeek ?: DayOfWeek.MONDAY) }
    var startTime by remember(template) { mutableStateOf(template?.startTime ?: LocalTime(9, 0)) }
    var duration by remember(template) { mutableStateOf((template?.duration ?: 1.25).toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (template == null) "Neue Vorlage" else "Vorlage bearbeiten",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Vorlagenname") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("z.B. Montag Morgen Yoga") }
                )
                
                // Studio selection dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = studios.find { it.id == selectedStudioId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Studio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Kursname") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("z.B. Vinyasa Flow") }
                )
                
                // Day of week selection
                var dayExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = dayExpanded,
                    onExpandedChange = { dayExpanded = it }
                ) {
                    OutlinedTextField(
                        value = getDayName(selectedDayOfWeek),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Wochentag") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
														.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false }
                    ) {
                        DayOfWeek.values().forEach { day ->
                            DropdownMenuItem(
                                text = { Text(getDayName(day)) },
                                onClick = {
                                    selectedDayOfWeek = day
                                    dayExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Time and duration inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimePickerField(
                        time = startTime,
                        onTimeSelected = { startTime = it },
                        label = "Startzeit",
                        modifier = Modifier.weight(1f)
                    )
                    
                    DurationPickerField(
                        durationHours = duration.toDoubleOrNull() ?: 1.25,
                        onDurationSelected = { duration = it.toString() },
                        label = "Dauer",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val durationValue = duration.toDoubleOrNull() ?: 1.25
                    onSave(name, selectedStudioId, className, selectedDayOfWeek, startTime, durationValue)
                },
                enabled = name.isNotBlank() && className.isNotBlank() && selectedStudioId != 0L
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

private fun formatDate(date: LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    val year = date.year
    return "$day.$month.$year"
}
