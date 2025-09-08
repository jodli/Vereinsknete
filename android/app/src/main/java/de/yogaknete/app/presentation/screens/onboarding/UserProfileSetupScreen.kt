package de.yogaknete.app.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.presentation.theme.YogaKneteTheme

@Composable
fun UserProfileSetupScreen(
    onContinue: (UserProfile) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var taxId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var iban by remember { mutableStateOf("") }
    var bic by remember { mutableStateOf("") }
    var hourlyRateText by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf(false) }
    var rateError by remember { mutableStateOf(false) }
    var taxIdError by remember { mutableStateOf(false) }
    var ibanError by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Header
        Icon(
            imageVector = Icons.Outlined.AccountCircle,
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Address fields
        Text(
            text = "Adresse",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = street,
            onValueChange = { street = it },
            label = { Text("Straße und Hausnummer") },
            placeholder = { Text("z.B. Yogastraße 42") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tax ID
        OutlinedTextField(
            value = taxId,
            onValueChange = { 
                taxId = it
                taxIdError = false
            },
            label = { Text("Steuernummer / USt-IdNr") },
            placeholder = { Text("z.B. DE123456789") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Badge,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = taxIdError,
            supportingText = if (taxIdError) {
                { Text("Bitte gib deine Steuernummer ein") }
            } else {
                { Text("Erforderlich für Rechnungen") }
            },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contact fields
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-Mail (optional)") },
            placeholder = { Text("maria@yoga.de") },
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
        
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefon (optional)") },
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Bank details
        Text(
            text = "Bankverbindung",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = bankName,
            onValueChange = { bankName = it },
            label = { Text("Bank") },
            placeholder = { Text("z.B. Sparkasse Berlin") },
            leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalance,
                            contentDescription = null
                        )
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
            placeholder = { Text("DE89 3704 0044 0532 0130 00") },
            modifier = Modifier.fillMaxWidth(),
            isError = ibanError,
            supportingText = if (ibanError) {
                { Text("Bitte gib eine gültige IBAN ein") }
            } else {
                { Text("Erforderlich für Überweisungen") }
            },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = bic,
            onValueChange = { bic = it.uppercase() },
            label = { Text("BIC (optional)") },
            placeholder = { Text("COBADEFFXXX") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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
                    imageVector = Icons.Outlined.AttachMoney,
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
                    val trimmedTaxId = taxId.trim()
                    val trimmedIban = iban.replace(" ", "").trim()
                    
                    nameError = trimmedName.isEmpty()
                    rateError = hourlyRate == null || hourlyRate <= 0
                    taxIdError = trimmedTaxId.isEmpty()
                    ibanError = trimmedIban.isEmpty() || trimmedIban.length < 15
                    
                    if (!nameError && !rateError && !taxIdError && !ibanError) {
                        val profile = UserProfile(
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
                            isOnboardingComplete = false
                        )
                        onContinue(profile)
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
            onContinue = { _ -> },
            onBack = { }
        )
    }
}
