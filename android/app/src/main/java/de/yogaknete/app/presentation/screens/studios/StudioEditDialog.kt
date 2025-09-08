package de.yogaknete.app.presentation.screens.studios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.yogaknete.app.domain.model.Studio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioEditDialog(
    studio: Studio? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        contactPerson: String,
        email: String,
        phone: String,
        street: String,
        postalCode: String,
        city: String,
        hourlyRate: Double
    ) -> Unit
) {
    val isEditMode = studio != null
    
    // Form state
    var name by remember { mutableStateOf(studio?.name ?: "") }
    var contactPerson by remember { mutableStateOf(studio?.contactPerson ?: "") }
    var email by remember { mutableStateOf(studio?.email ?: "") }
    var phone by remember { mutableStateOf(studio?.phone ?: "") }
    var street by remember { mutableStateOf(studio?.street ?: "") }
    var postalCode by remember { mutableStateOf(studio?.postalCode ?: "") }
    var city by remember { mutableStateOf(studio?.city ?: "") }
    var hourlyRateText by remember { 
        mutableStateOf(studio?.hourlyRate?.toString()?.replace(".", ",") ?: "")
    }
    
    // Error states
    var nameError by remember { mutableStateOf(false) }
    var rateError by remember { mutableStateOf(false) }
    
    // Expanded form state
    var showExpandedForm by remember { mutableStateOf(isEditMode) }
    
    val scrollState = rememberScrollState()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                TopAppBar(
                    title = { 
                        Text(
                            text = if (isEditMode) "Studio bearbeiten" else "Neues Studio",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Schließen")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                val trimmedName = name.trim()
                                val hourlyRate = hourlyRateText.replace(",", ".").toDoubleOrNull()
                                
                                nameError = trimmedName.isEmpty()
                                rateError = hourlyRate == null || hourlyRate <= 0
                                
                                if (!nameError && !rateError) {
                                    onSave(
                                        trimmedName,
                                        contactPerson.trim(),
                                        email.trim(),
                                        phone.trim(),
                                        street.trim(),
                                        postalCode.trim(),
                                        city.trim(),
                                        hourlyRate!!
                                    )
                                }
                            }
                        ) {
                            Text("Speichern")
                        }
                    }
                )
                
                // Form content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Basic Information
                    Text(
                        text = "Grunddaten",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = false
                        },
                        label = { Text("Studio-Name *") },
                        placeholder = { Text("z.B. TSV München") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Store, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("Bitte gib einen Studio-Namen ein") }
                        } else null,
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = hourlyRateText,
                        onValueChange = { 
                            hourlyRateText = it
                            rateError = false
                        },
                        label = { Text("Stundensatz (€) *") },
                        placeholder = { Text("z.B. 31,50") },
                        leadingIcon = {
                            Icon(Icons.Outlined.AttachMoney, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = rateError,
                        supportingText = if (rateError) {
                            { Text("Bitte gib einen gültigen Stundensatz ein") }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    // Toggle for additional fields
                    if (!isEditMode) {
                        TextButton(
                            onClick = { showExpandedForm = !showExpandedForm },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (showExpandedForm) 
                                    Icons.Outlined.ExpandLess 
                                else 
                                    Icons.Outlined.ExpandMore,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showExpandedForm) 
                                    "Weniger Felder" 
                                else 
                                    "Kontaktdaten hinzufügen (optional)"
                            )
                        }
                    }
                    
                    if (showExpandedForm || isEditMode) {
                        HorizontalDivider()
                        
                        // Contact Information
                        Text(
                            text = "Kontaktdaten",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = contactPerson,
                            onValueChange = { contactPerson = it },
                            label = { Text("Ansprechpartner") },
                            placeholder = { Text("z.B. Max Mustermann") },
                            leadingIcon = {
                                Icon(Icons.Outlined.ContactMail, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-Mail") },
                            placeholder = { Text("studio@example.de") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Telefon") },
                            placeholder = { Text("+49 123 456789") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        
                        HorizontalDivider()
                        
                        // Address
                        Text(
                            text = "Adresse",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = street,
                            onValueChange = { street = it },
                            label = { Text("Straße und Hausnummer") },
                            placeholder = { Text("Yogastraße 1") },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = postalCode,
                                onValueChange = { postalCode = it },
                                label = { Text("PLZ") },
                                placeholder = { Text("12345") },
                                modifier = Modifier.weight(0.3f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("Stadt") },
                                placeholder = { Text("Berlin") },
                                modifier = Modifier.weight(0.7f),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }
    }
}
