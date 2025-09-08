package de.yogaknete.app.presentation.screens.week

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.yogaknete.app.core.utils.DateUtils
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.CreationSource
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.presentation.theme.YogaKneteTheme
import de.yogaknete.app.presentation.screens.templates.QuickAddDialog
import kotlinx.datetime.*
import java.util.Locale

@Composable
fun WeekViewScreen(
    onNavigateToInvoice: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToStudios: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    viewModel: WeekViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isPastWeek = remember(state.currentWeekEnd) { DateUtils.isPast(state.currentWeekEnd) }
    
    Scaffold(
        topBar = {
            WeekViewTopBar(
                weekStart = state.currentWeekStart,
                weekEnd = state.currentWeekEnd,
                onPreviousWeek = { viewModel.navigateToPreviousWeek() },
                onNextWeek = { viewModel.navigateToNextWeek() },
                onToday = { viewModel.navigateToToday() },
                onNavigateToTemplates = onNavigateToTemplates,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToInvoice = onNavigateToInvoice,
                onNavigateToStudios = onNavigateToStudios,
                onNavigateToBackup = onNavigateToBackup,
                isNotFutureWeek = state.currentWeekStart <= Clock.System.todayIn(TimeZone.currentSystemDefault()),
                onBulkCancel = { viewModel.showBulkCancelDialog() },
                onShowMonthlyStats = { viewModel.showMonthlyStats() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddClassDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Kurs hinzufügen"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Week summary
            WeekSummaryCard(
                totalClasses = state.totalClassesThisWeek,
                totalHours = state.totalHoursThisWeek,
                totalEarnings = state.totalEarningsThisWeek,
                onShowStats = { viewModel.showWeekStats() }
            )
            
            // Classes list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.weekDays.forEach { date ->
                    val dayClasses = state.classes[date] ?: emptyList()
                    
                    item {
                        DayHeader(
                            date = date,
                            isToday = DateUtils.isToday(date),
                            onQuickAdd = { viewModel.showQuickAddDialogForDate(date) }
                        )
                    }
                    
                    if (dayClasses.isEmpty()) {
                        item {
                            EmptyDayCard(
                                onClick = { viewModel.showQuickAddDialogForDate(date) }
                            )
                        }
                    } else {
                        items(dayClasses) { yogaClass ->
                            YogaClassCard(
                                yogaClass = yogaClass,
                                studio = state.studios.find { it.id == yogaClass.studioId },
                                onClick = { viewModel.selectClass(yogaClass) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add class dialog
    if (state.showAddClassDialog) {
        AddClassDialog(
            studios = state.studios,
            weekDays = state.weekDays,
            templates = state.templates,
            onDismiss = { viewModel.hideAddClassDialog() },
            onConfirm = { studioId, title, date, startHour, startMinute, endHour, endMinute ->
                viewModel.addClass(studioId, title, date, startHour, startMinute, endHour, endMinute)
            }
        )
    }
    
    // Quick add dialog (templates)
    val quickAddDate = state.quickAddDate
    if (state.showQuickAddDialog && quickAddDate != null) {
        QuickAddDialog(
            date = quickAddDate,
            templates = state.templates,
            onTemplateSelected = { template, startOverride, durationOverride ->
                viewModel.createClassFromTemplate(
                    template = template,
                    date = quickAddDate,
                    startTimeOverride = startOverride,
                    durationOverride = durationOverride
                )
            },
            onDismiss = { viewModel.hideQuickAddDialog() }
        )
    }
    
    // Class action dialog
    state.selectedClass?.let { selectedClass ->
        ClassActionDialog(
            yogaClass = selectedClass,
            studio = state.studios.find { it.id == selectedClass.studioId },
            onDismiss = { viewModel.clearSelectedClass() },
            onMarkCompleted = { viewModel.markClassAsCompleted(selectedClass) },
            onMarkCancelled = { viewModel.markClassAsCancelled(selectedClass) },
            onEdit = { viewModel.showEditClassDialog(selectedClass) },
            onDelete = { viewModel.deleteClass(selectedClass) }
        )
    }

    // Bulk cancel dialog
    if (state.showBulkCancelDialog) {
        val weekClasses = remember(state.classes) { state.classes.values.flatten() }
        BulkCancelDialog(
            classes = weekClasses,
            studios = state.studios,
            onDismiss = { viewModel.hideBulkCancelDialog() },
            onConfirm = { ids -> viewModel.bulkCancelClasses(ids) }
        )
    }

    // Edit/reschedule dialog
    if (state.showEditClassDialog && state.selectedClass != null) {
        val cls = state.selectedClass!!
        EditClassDialog(
            yogaClass = cls,
            studios = state.studios,
            onDismiss = { viewModel.hideEditClassDialog() },
            onConfirm = { date, startHour, startMinute, endHour, endMinute ->
                viewModel.updateClassSchedule(
                    yogaClassId = cls.id,
                    newDate = date,
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute
                )
            }
        )
    }
    
    // Week statistics dialog
    if (state.showWeekStats) {
        WeekStatsDialog(
            weekStart = state.currentWeekStart,
            weekEnd = state.currentWeekEnd,
            totalClasses = state.totalClassesThisWeek,
            totalHours = state.totalHoursThisWeek,
            totalEarnings = state.totalEarningsThisWeek,
            earningsPerStudio = state.earningsPerStudio,
            studios = state.studios,
            onDismiss = { viewModel.hideWeekStats() }
        )
    }
    
    // Monthly statistics dialog
    if (state.showMonthlyStats) {
        MonthlyStatsDialog(
            month = state.monthlyStatsMonth,
            year = state.monthlyStatsYear,
            totalClasses = state.monthlyTotalClasses,
            totalHours = state.monthlyTotalHours,
            totalEarnings = state.monthlyTotalEarnings,
            earningsPerStudio = state.monthlyEarningsPerStudio,
            studios = state.studios,
            onDismiss = { viewModel.hideMonthlyStats() },
            onPreviousMonth = {
                val newMonth = if (state.monthlyStatsMonth == 1) 12 else state.monthlyStatsMonth - 1
                val newYear = if (state.monthlyStatsMonth == 1) state.monthlyStatsYear - 1 else state.monthlyStatsYear
                viewModel.loadMonthlyStats(newMonth, newYear)
            },
            onNextMonth = {
                val newMonth = if (state.monthlyStatsMonth == 12) 1 else state.monthlyStatsMonth + 1
                val newYear = if (state.monthlyStatsMonth == 12) state.monthlyStatsYear + 1 else state.monthlyStatsYear
                viewModel.loadMonthlyStats(newMonth, newYear)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekViewTopBar(
    weekStart: LocalDate,
    weekEnd: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToday: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInvoice: () -> Unit,
    onNavigateToStudios: () -> Unit,
    onNavigateToBackup: () -> Unit,
    isNotFutureWeek: Boolean,
    onBulkCancel: () -> Unit,
    onShowMonthlyStats: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Diese Woche",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = DateUtils.formatWeekRange(weekStart, weekEnd),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onPreviousWeek) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Vorherige Woche"
                )
            }
        },
        actions = {
            IconButton(onClick = onNextWeek) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Nächste Woche"
                )
            }
            IconButton(onClick = onToday) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = "Heute"
                )
            }
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
                        text = { Text("Rechnungen") },
                        onClick = {
                            showMenu = false
                            onNavigateToInvoice()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Receipt,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Studios verwalten") },
                        onClick = {
                            showMenu = false
                            onNavigateToStudios()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Mein Profil") },
                        onClick = {
                            showMenu = false
                            onNavigateToProfile()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Datensicherung") },
                        onClick = {
                            showMenu = false
                            onNavigateToBackup()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null
                            )
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Monatsübersicht") },
                        onClick = {
                            showMenu = false
                            onShowMonthlyStats()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Vorlagen verwalten") },
                        onClick = {
                            showMenu = false
                            onNavigateToTemplates()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = null
                            )
                        }
                    )
                    if (isNotFutureWeek) {
                        DropdownMenuItem(
                            text = { Text("Massenstorno (Woche)") },
                            onClick = {
                                showMenu = false
                                onBulkCancel()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun WeekSummaryCard(
    totalClasses: Int,
    totalHours: Double,
    totalEarnings: Double,
    onShowStats: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onShowStats() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Wochenübersicht",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (totalClasses > 0) {
                // Stats grid layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Classes stat
                    StatItem(
                        value = totalClasses.toString(),
                        label = if (totalClasses == 1) "Kurs" else "Kurse",
                        icon = Icons.Default.School,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Hours stat
                    StatItem(
                        value = String.format(Locale.GERMAN, "%.1f", totalHours).replace(".", ","),
                        label = "Stunden",
                        icon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Earnings stat
                    StatItem(
                        value = "€${String.format(Locale.GERMAN, "%.0f", totalEarnings).replace(".", ",")}",
                        label = "Verdienst",
                        icon = Icons.Default.Euro,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Empty state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Noch keine erledigten Kurse diese Woche",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tippe für detaillierte Statistiken",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = color.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DayHeader(
    date: LocalDate,
    isToday: Boolean,
    onQuickAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = DateUtils.formatDayDate(date),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
        )
        
        if (isToday) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "HEUTE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onQuickAdd) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Schnell hinzufügen"
            )
        }
    }
}

@Composable
private fun EmptyDayCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.EventBusy,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keine Kurse geplant",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun YogaClassCard(
    yogaClass: YogaClass,
    studio: Studio?,
    onClick: () -> Unit
) {
    val statusIcon = when (yogaClass.status) {
        ClassStatus.SCHEDULED -> Icons.Default.Schedule
        ClassStatus.COMPLETED -> Icons.Default.CheckCircle
        ClassStatus.CANCELLED -> Icons.Default.Cancel
    }
    
    val statusColor = when (yogaClass.status) {
        ClassStatus.SCHEDULED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        ClassStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        ClassStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

    val borderColor = when (yogaClass.status) {
        ClassStatus.COMPLETED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        ClassStatus.CANCELLED -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    
    val backgroundColor = when (yogaClass.status) {
        ClassStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
        ClassStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = DateUtils.formatTimeRange(yogaClass.startTime, yogaClass.endTime),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (yogaClass.status == ClassStatus.CANCELLED) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                )
                
                Text(
                    text = studio?.name ?: "Unbekanntes Studio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (yogaClass.status == ClassStatus.CANCELLED) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = yogaClass.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (yogaClass.status == ClassStatus.CANCELLED) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    // Show AUTO badge for auto-scheduled classes
                    if (yogaClass.creationSource == CreationSource.AUTO) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(
                                text = "AUTO",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                
                if (yogaClass.status != ClassStatus.SCHEDULED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (yogaClass.status) {
                                ClassStatus.COMPLETED -> Icons.Default.CheckCircle
                                ClassStatus.CANCELLED -> Icons.Default.Cancel
                                else -> Icons.Default.Schedule
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = statusColor
                        )
                        Text(
                            text = when (yogaClass.status) {
                                ClassStatus.COMPLETED -> "Erledigt"
                                ClassStatus.CANCELLED -> "Ausgefallen"
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }
            }
            
            if (yogaClass.status == ClassStatus.SCHEDULED) {
                Text(
                    text = "Antippen für Aktionen",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeekViewScreenPreview() {
    YogaKneteTheme {
        // Preview would need mock data
    }
}
