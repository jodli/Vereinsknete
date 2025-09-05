package de.yogaknete.app.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.yogaknete.app.presentation.theme.YogaKneteTheme

@Composable
fun UserProfileSetupScreen(
    onContinue: (name: String, hourlyRate: Double) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hourlyRateText by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var rateError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Header
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Dein Profil einrichten",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Erzähle uns etwas über dich, damit wir dir helfen können, deine Yoga-Kurse zu verwalten",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Name input
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                nameError = false
            },
            label = { Text("Dein Name") },
            placeholder = { Text("z.B. Maria Schmidt") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError,
            supportingText = if (nameError) {
                { Text("Bitte gib deinen Namen ein") }
            } else null,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Hourly rate input
        OutlinedTextField(
            value = hourlyRateText,
            onValueChange = { 
                hourlyRateText = it
                rateError = false
            },
            label = { Text("Stundensatz (€)") },
            placeholder = { Text("z.B. 31,50") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = rateError,
            supportingText = if (rateError) {
                { Text("Bitte gib einen gültigen Stundensatz ein") }
            } else {
                { Text("Dies ist dein Standard-Stundensatz. Du kannst ihn später für jeden Verein anpassen.") }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Zurück")
            }
            
            Button(
                onClick = {
                    // Validate inputs
                    val trimmedName = name.trim()
                    val hourlyRate = hourlyRateText.replace(",", ".").toDoubleOrNull()
                    
                    nameError = trimmedName.isEmpty()
                    rateError = hourlyRate == null || hourlyRate <= 0
                    
                    if (!nameError && !rateError) {
                        onContinue(trimmedName, hourlyRate!!)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Weiter")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileSetupScreenPreview() {
    YogaKneteTheme {
        UserProfileSetupScreen(
            onContinue = { _, _ -> },
            onBack = { }
        )
    }
}
