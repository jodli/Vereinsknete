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
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.presentation.theme.YogaKneteTheme
import de.yogaknete.app.presentation.screens.templates.QuickAddDialog
import kotlinx.datetime.*

@Composable
fun WeekViewScreen(
    onNavigateToInvoice: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
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
                isPastWeek = state.currentWeekEnd < Clock.System.todayIn(TimeZone.currentSystemDefault()),
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
                onCreateInvoice = onNavigateToInvoice,
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
    isPastWeek: Boolean,
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
                    imageVector = Icons.Default.Home,
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
                        text = { Text("Monatsübersicht") },
                        onClick = {
                            showMenu = false
                            onShowMonthlyStats()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
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
                    if (isPastWeek) {
                        DropdownMenuItem(
                            text = { Text("Massenstorno (Woche)") },
                            onClick = {
                                showMenu = false
                                onBulkCancel()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Clear,
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
    onCreateInvoice: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (totalClasses > 0) "$totalClasses erledigte Kurse" else "Keine erledigten Kurse",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (totalClasses > 0) {
                        Text(
                            text = "${String.format("%.2f", totalHours).replace(".", ",")} Stunden",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "€ ${String.format("%.2f", totalEarnings).replace(".", ",")} Verdienst",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Button(
                    onClick = onCreateInvoice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rechnung")
                }
            }
            
            // Hint for more details
            Text(
                text = "Tippe für Details",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
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
            Text(
                text = "Keine Kurse",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
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
        ClassStatus.SCHEDULED -> null
        ClassStatus.COMPLETED -> Icons.Default.CheckCircle
        ClassStatus.CANCELLED -> Icons.Default.Clear
    }
    
    val statusColor = when (yogaClass.status) {
        ClassStatus.SCHEDULED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        ClassStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        ClassStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

    val borderColor = when (yogaClass.status) {
        ClassStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        ClassStatus.CANCELLED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (statusIcon != null) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                // For scheduled classes, show a simple circle/dot
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = statusColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
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
                
                Text(
                    text = yogaClass.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (yogaClass.status == ClassStatus.CANCELLED) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (yogaClass.status != ClassStatus.SCHEDULED) {
                    Text(
                        text = when (yogaClass.status) {
                            ClassStatus.COMPLETED -> "Erledigt ✓"
                            ClassStatus.CANCELLED -> "Ausgefallen"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
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
