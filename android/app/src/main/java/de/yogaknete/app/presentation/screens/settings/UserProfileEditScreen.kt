package de.yogaknete.app.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.yogaknete.app.domain.model.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileEditScreen(
    navController: NavController,
    viewModel: UserProfileEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Form state
    var name by remember(uiState.profile) { mutableStateOf(uiState.profile?.name ?: "") }
    var street by remember(uiState.profile) { mutableStateOf(uiState.profile?.street ?: "") }
    var postalCode by remember(uiState.profile) { mutableStateOf(uiState.profile?.postalCode ?: "") }
    var city by remember(uiState.profile) { mutableStateOf(uiState.profile?.city ?: "") }
    var taxId by remember(uiState.profile) { mutableStateOf(uiState.profile?.taxId ?: "") }
    var phone by remember(uiState.profile) { mutableStateOf(uiState.profile?.phone ?: "") }
    var email by remember(uiState.profile) { mutableStateOf(uiState.profile?.email ?: "") }
    var bankName by remember(uiState.profile) { mutableStateOf(uiState.profile?.bankName ?: "") }
    var iban by remember(uiState.profile) { mutableStateOf(uiState.profile?.iban ?: "") }
    var bic by remember(uiState.profile) { mutableStateOf(uiState.profile?.bic ?: "") }
    var hourlyRateText by remember(uiState.profile) { 
        mutableStateOf(uiState.profile?.defaultHourlyRate?.toString()?.replace(".", ",") ?: "")
    }
    
    // Error states
    var nameError by remember { mutableStateOf(false) }
    var rateError by remember { mutableStateOf(false) }
    var taxIdError by remember { mutableStateOf(false) }
    var ibanError by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    // Show success message
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Profil erfolgreich gespeichert")
            viewModel.resetSaveState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil bearbeiten") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Validate inputs
                            val trimmedName = name.trim()
                            val hourlyRate = hourlyRateText.replace(",", ".").toDoubleOrNull()
                            val trimmedTaxId = taxId.trim()
                            val trimmedIban = iban.replace(" ", "").trim()
                            
                            nameError = trimmedName.isEmpty()
                            rateError = hourlyRate == null || hourlyRate <= 0
                            taxIdError = trimmedTaxId.isEmpty()
                            ibanError = trimmedIban.isEmpty() || trimmedIban.length < 15
                            
                            if (!nameError && !rateError && !taxIdError && !ibanError) {
                                val updatedProfile = UserProfile(
                                    id = uiState.profile?.id ?: 1L,
                                    name = trimmedName,
                                    street = street.trim(),
                                    postalCode = postalCode.trim(),
                                    city = city.trim(),
                                    taxId = trimmedTaxId,
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    bankName = bankName.trim(),
                                    iban = trimmedIban,
                                    bic = bic.trim().uppercase(),
                                    defaultHourlyRate = hourlyRate!!,
                                    isOnboardingComplete = true
                                )
                                viewModel.saveProfile(updatedProfile)
                            }
                        },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Speichern")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Personal Information Section
                Text(
                    text = "Persönliche Daten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = false
                    },
                    label = { Text("Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Bitte gib deinen Namen ein") }
                    } else null,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Address Section
                Text(
                    text = "Adresse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Straße und Hausnummer") },
                    leadingIcon = {
                        Icon(Icons.Default.Home, contentDescription = null)
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tax and Business Section
                Text(
                    text = "Geschäftsdaten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = taxId,
                    onValueChange = { 
                        taxId = it
                        taxIdError = false
                    },
                    label = { Text("Steuernummer / USt-IdNr") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = taxIdError,
                    supportingText = if (taxIdError) {
                        { Text("Bitte gib deine Steuernummer ein") }
                    } else null,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = hourlyRateText,
                    onValueChange = { 
                        hourlyRateText = it
                        rateError = false
                    },
                    label = { Text("Standard-Stundensatz (€)") },
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = rateError,
                    supportingText = if (rateError) {
                        { Text("Bitte gib einen gültigen Stundensatz ein") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contact Section
                Text(
                    text = "Kontaktdaten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bank Details Section
                Text(
                    text = "Bankverbindung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank") },
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = iban,
                    onValueChange = { 
                        iban = it.uppercase()
                        ibanError = false
                    },
                    label = { Text("IBAN") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = ibanError,
                    supportingText = if (ibanError) {
                        { Text("Bitte gib eine gültige IBAN ein") }
                    } else null,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = bic,
                    onValueChange = { bic = it.uppercase() },
                    label = { Text("BIC (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
