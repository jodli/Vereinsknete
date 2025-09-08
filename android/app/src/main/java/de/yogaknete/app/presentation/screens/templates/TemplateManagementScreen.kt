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
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (template.isActive) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = template.className,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${formatTime(template.startTime)} - ${formatTime(template.endTime)} (${template.duration} Std.)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row {
                IconButton(onClick = onToggleActive) {
                    Icon(
                        imageVector = if (template.isActive) {
                            Icons.Outlined.ToggleOn
                        } else {
                            Icons.Outlined.ToggleOff
                        },
                        contentDescription = if (template.isActive) "Deaktivieren" else "Aktivieren",
                        tint = if (template.isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Löschen",
                        tint = MaterialTheme.colorScheme.error
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
    var selectedStudioId by remember(template) { mutableStateOf(template?.studioId ?: studios.firstOrNull()?.id ?: 0L) }
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
                            .menuAnchor()
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
                
                // Time picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime.hour.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { hour ->
                                if (hour in 0..23) {
                                    startTime = LocalTime(hour, startTime.minute)
                                }
                            }
                        },
                        label = { Text("Stunde") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startTime.minute.toString().padStart(2, '0'),
                        onValueChange = {
                            it.toIntOrNull()?.let { minute ->
                                if (minute in 0..59) {
                                    startTime = LocalTime(startTime.hour, minute)
                                }
                            }
                        },
                        label = { Text("Minute") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Dauer (Stunden)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("z.B. 1.25") }
                )
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
