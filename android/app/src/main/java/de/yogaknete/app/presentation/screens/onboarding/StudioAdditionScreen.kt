package de.yogaknete.app.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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

data class StudioInput(
    val name: String,
    val contactPerson: String = "",
    val email: String = "",
    val phone: String = "",
    val street: String = "",
    val postalCode: String = "",
    val city: String = "",
    val hourlyRate: Double
)

@Composable
fun StudioAdditionScreen(
    defaultHourlyRate: Double,
    onContinue: (studios: List<StudioInput>) -> Unit,
    onBack: () -> Unit
) {
    var studios by remember { mutableStateOf(listOf<StudioInput>()) }
    var studioName by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var studioRateText by remember { mutableStateOf(defaultHourlyRate.toString().replace(".", ",")) }
    var nameError by remember { mutableStateOf(false) }
    var rateError by remember { mutableStateOf(false) }
    
    var showExpandedForm by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Header
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Deine Yoga-Studios",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Füge die Vereine oder Studios hinzu, in denen du Yoga-Kurse gibst",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Add studio form
            Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Neues Studio hinzufügen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = studioName,
                    onValueChange = { 
                        studioName = it
                        nameError = false
                    },
                    label = { Text("Studio-Name") },
                    placeholder = { Text("z.B. TSV München") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Bitte gib einen Studio-Namen ein") }
                    } else null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = studioRateText,
                    onValueChange = { 
                        studioRateText = it
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
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Toggle for additional fields
                TextButton(
                    onClick = { showExpandedForm = !showExpandedForm },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showExpandedForm) "Weniger Felder" else "Kontaktdaten hinzufügen (optional)"
                    )
                }
                
                if (showExpandedForm) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Contact person
                    OutlinedTextField(
                        value = contactPerson,
                        onValueChange = { contactPerson = it },
                        label = { Text("Ansprechpartner") },
                        placeholder = { Text("z.B. Max Mustermann") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-Mail") },
                        placeholder = { Text("studio@example.de") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefon") },
                        placeholder = { Text("+49 123 456789") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Address
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Straße und Hausnummer") },
                        placeholder = { Text("Yogastraße 1") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = postalCode,
                            onValueChange = { postalCode = it },
                            label = { Text("PLZ") },
                            modifier = Modifier.weight(0.3f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("Stadt") },
                            modifier = Modifier.weight(0.7f),
                            singleLine = true
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        val trimmedName = studioName.trim()
                        val hourlyRate = studioRateText.replace(",", ".").toDoubleOrNull()
                        
                        nameError = trimmedName.isEmpty()
                        rateError = hourlyRate == null || hourlyRate <= 0
                        
                        if (!nameError && !rateError) {
                            studios = studios + StudioInput(
                                name = trimmedName,
                                contactPerson = contactPerson.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                street = street.trim(),
                                postalCode = postalCode.trim(),
                                city = city.trim(),
                                hourlyRate = hourlyRate!!
                            )
                            // Reset fields
                            studioName = ""
                            contactPerson = ""
                            email = ""
                            phone = ""
                            street = ""
                            postalCode = ""
                            city = ""
                            studioRateText = defaultHourlyRate.toString().replace(".", ",")
                            showExpandedForm = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Studio hinzufügen")
                }
            }
        }
        
            if (studios.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Deine Studios (${studios.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Display studios without LazyColumn since we're already in a scrollable container
                studios.forEach { studio ->
                    StudioItem(
                        studio = studio,
                        onDelete = {
                            studios = studios - studio
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Buttons at the bottom (outside scrollable area)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Zurück")
            }
            
            Button(
                onClick = { onContinue(studios) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (studios.isEmpty()) "Überspringen" else "Fertig (${studios.size})")
            }
        }
    }
}

@Composable
private fun StudioItem(
    studio: StudioInput,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = studio.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${studio.hourlyRate.toString().replace(".", ",")}€ pro Stunde",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Löschen",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudioAdditionScreenPreview() {
    YogaKneteTheme {
        StudioAdditionScreen(
            defaultHourlyRate = 31.50,
            onContinue = { },
            onBack = { }
        )
    }
}
