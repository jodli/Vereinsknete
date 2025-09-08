package de.yogaknete.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputField(
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        OutlinedTextField(
            value = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
            onValueChange = { },
            readOnly = true,
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Zeit wählen"
                    )
                }
            },
            singleLine = true
        )
    }
    
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onConfirm = { h, m ->
                onHourChange(h)
                onMinuteChange(m)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    time: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    label: String = "Zeit",
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = formatTime(time),
        onValueChange = { },
        readOnly = true,
        enabled = true,
        label = { Text(label) },
        modifier = modifier.clickable { showTimePicker = true },
        trailingIcon = {
            IconButton(onClick = { showTimePicker = true }) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = "Zeit wählen"
                )
            }
        },
        singleLine = true
    )
    
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = time.hour,
            initialMinute = time.minute,
            onConfirm = { h, m ->
                onTimeSelected(LocalTime(h, m))
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationInputField(
    label: String,
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDurationPicker by remember { mutableStateOf(false) }
    
    // Common durations in minutes
    val commonDurations = listOf(
        30 to "30 Min",
        45 to "45 Min",
        60 to "1 Std",
        75 to "1¼ Std",
        90 to "1½ Std",
        120 to "2 Std"
    )
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        OutlinedTextField(
            value = formatDuration(durationMinutes),
            onValueChange = { },
            readOnly = true,
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDurationPicker = true },
            trailingIcon = {
                IconButton(onClick = { showDurationPicker = true }) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Dauer wählen"
                    )
                }
            },
            singleLine = true
        )
    }
    
    if (showDurationPicker) {
        AlertDialog(
            onDismissRequest = { showDurationPicker = false },
            title = {
                Text("Kursdauer auswählen")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonDurations.forEach { (minutes, label) ->
                        Card(
                            onClick = {
                                onDurationChange(minutes)
                                showDurationPicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (minutes == durationMinutes)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                            border = if (minutes == durationMinutes)
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (minutes == durationMinutes) FontWeight.Bold else FontWeight.Normal
                                )
                                if (minutes == durationMinutes) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDurationPicker = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationPickerField(
    durationHours: Double,
    onDurationSelected: (Double) -> Unit,
    label: String = "Dauer",
    modifier: Modifier = Modifier
) {
    var showDurationPicker by remember { mutableStateOf(false) }
    
    // Common durations in hours
    val commonDurations = listOf(
        0.5 to "30 Min",
        0.75 to "45 Min",
        1.0 to "1 Std",
        1.25 to "1¼ Std",
        1.5 to "1½ Std",
        2.0 to "2 Std"
    )
    
    OutlinedTextField(
        value = formatDurationHours(durationHours),
        onValueChange = { },
        readOnly = true,
        enabled = true,
        label = { Text(label) },
        modifier = modifier.clickable { showDurationPicker = true },
        trailingIcon = {
            IconButton(onClick = { showDurationPicker = true }) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Dauer wählen"
                )
            }
        },
        singleLine = true
    )
    
    if (showDurationPicker) {
        AlertDialog(
            onDismissRequest = { showDurationPicker = false },
            title = {
                Text("Kursdauer auswählen")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonDurations.forEach { (hours, label) ->
                        Card(
                            onClick = {
                                onDurationSelected(hours)
                                showDurationPicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (hours == durationHours)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                            border = if (hours == durationHours)
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (hours == durationHours) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hours == durationHours) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDurationPicker = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

private fun formatTime(time: LocalTime): String {
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

private fun formatDuration(minutes: Int): String {
    return when {
        minutes < 60 -> "${minutes} Min"
        minutes == 60 -> "1 Std"
        minutes % 60 == 0 -> "${minutes / 60} Std"
        minutes == 75 -> "1¼ Std"
        minutes == 90 -> "1½ Std"
        else -> "${minutes / 60}:${(minutes % 60).toString().padStart(2, '0')} Std"
    }
}

private fun formatDurationHours(hours: Double): String {
    return when {
        hours < 1.0 -> "${(hours * 60).toInt()} Min"
        hours == 1.0 -> "1 Std"
        hours == 1.25 -> "1¼ Std"
        hours == 1.5 -> "1½ Std"
        hours % 1.0 == 0.0 -> "${hours.toInt()} Std"
        else -> "${String.format("%.2f", hours).replace(".", ",")} Std"
    }
}
