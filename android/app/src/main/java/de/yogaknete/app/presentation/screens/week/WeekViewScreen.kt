package de.yogaknete.app.presentation.screens.week

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import kotlinx.datetime.*

@Composable
fun WeekViewScreen(
    onNavigateToInvoice: () -> Unit = {},
    viewModel: WeekViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            WeekViewTopBar(
                weekStart = state.currentWeekStart,
                weekEnd = state.currentWeekEnd,
                onPreviousWeek = { viewModel.navigateToPreviousWeek() },
                onNextWeek = { viewModel.navigateToNextWeek() },
                onToday = { viewModel.navigateToToday() }
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
                onCreateInvoice = onNavigateToInvoice
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
                            isToday = DateUtils.isToday(date)
                        )
                    }
                    
                    if (dayClasses.isEmpty()) {
                        item {
                            EmptyDayCard()
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
            onDismiss = { viewModel.hideAddClassDialog() },
            onConfirm = { studioId, title, date, startHour, startMinute, endHour, endMinute ->
                viewModel.addClass(studioId, title, date, startHour, startMinute, endHour, endMinute)
            }
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
            onDelete = { viewModel.deleteClass(selectedClass) }
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
    onToday: () -> Unit
) {
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Vorherige Woche"
                )
            }
        },
        actions = {
            IconButton(onClick = onNextWeek) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Nächste Woche"
                )
            }
            IconButton(onClick = onToday) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Heute"
                )
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
    onCreateInvoice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$totalClasses Kurse diese Woche",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${String.format("%.2f", totalHours).replace(".", ",")} Stunden gesamt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Button(
                onClick = onCreateInvoice,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rechnung")
            }
        }
    }
}

@Composable
private fun DayHeader(
    date: LocalDate,
    isToday: Boolean
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
    }
}

@Composable
private fun EmptyDayCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
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
        ClassStatus.SCHEDULED -> Icons.Default.CheckCircle
        ClassStatus.COMPLETED -> Icons.Default.CheckCircle
        ClassStatus.CANCELLED -> Icons.Default.Clear
    }
    
    val statusColor = when (yogaClass.status) {
        ClassStatus.SCHEDULED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        ClassStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        ClassStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = studio?.name ?: "Unbekanntes Studio",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = yogaClass.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                    text = "Antippen zum Abhaken",
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
