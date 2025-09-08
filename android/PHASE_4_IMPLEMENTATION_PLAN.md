# Phase 4: Polish & Refinements - Detailed Implementation Plan

## Overview

This document provides a detailed implementation plan for the remaining sprints in Phase 4 of the YogaKnete Android app development. Based on the current codebase analysis, Phase 1-3 are complete, and several bugs from Sprint 4.4 have been fixed.

## Current State Analysis

### ✅ Completed Features

- Basic onboarding flow (Welcome, UserProfile, Studio setup)
- Week view with class management
- Template system for quick class creation
- Invoice generation with PDF export
- Studio management
- User profile editing
- Database with Room (version 4)
- MVVM architecture with Hilt DI

### ⚠️ Remaining Tasks from Sprint 4.4

- [ ] Change away from destructive migrations for the database

### 📋 Pending Phase 4 Sprints

- Sprint 4.1: Data Backup & Restore System
- Sprint 4.2: Production Polish & Error Handling

---

## Sprint 4.1: Data Backup & User Profile Enhancement (2 days)

### Goals

Implement comprehensive backup/restore functionality and enhance user profile with all required fields for professional invoicing. Focus on data safety and completeness for a production-ready app.

### Implementation Tasks

#### 1. Backup & Export System (5 hours)

**New File**: `domain/service/BackupExportService.kt`

- Export complete database to JSON format
- Include all entities: UserProfile, Studios, YogaClasses, Templates, Invoices
- Compress to ZIP file for smaller size
- Save to device storage (Downloads folder)
- Generate timestamped filenames
- Export statistics and metadata

```kotlin
class BackupExportService @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val studioRepository: StudioRepository,
    private val classRepository: YogaClassRepository,
    private val templateRepository: ClassTemplateRepository,
    private val invoiceRepository: InvoiceRepository,
    private val context: Context
) {
    suspend fun exportFullBackup(): BackupResult {
        val backup = BackupData(
            version = 1,
            exportDate = Clock.System.now(),
            userProfile = userProfileRepository.getUserProfile(),
            studios = studioRepository.getAllStudios(),
            classes = classRepository.getAllClasses(),
            templates = templateRepository.getAllTemplates(),
            invoices = invoiceRepository.getAllInvoices()
        )

        val json = Json.encodeToString(backup)
        val fileName = "yogaknete_backup_${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())}.json"
        return saveToDownloads(json, fileName)
    }

    suspend fun exportToZip(): BackupResult {
        // Create ZIP with JSON + attachments
    }
}
```

#### 2. Import & Restore System (5 hours)

**New File**: `domain/service/BackupImportService.kt`

- Import from JSON backup files
- Validate backup file integrity
- Preview data before import
- Handle conflicts (merge/replace options)
- Restore complete state
- Progress tracking during import

```kotlin
class BackupImportService @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val studioRepository: StudioRepository,
    private val classRepository: YogaClassRepository,
    private val templateRepository: ClassTemplateRepository,
    private val invoiceRepository: InvoiceRepository
) {
    suspend fun validateBackup(uri: Uri): ValidationResult {
        // Check file format
        // Verify data integrity
        // Return preview data
    }

    suspend fun importBackup(
        uri: Uri,
        strategy: ImportStrategy = ImportStrategy.MERGE
    ): ImportResult {
        val backupData = parseBackupFile(uri)

        return when(strategy) {
            ImportStrategy.REPLACE -> replaceAllData(backupData)
            ImportStrategy.MERGE -> mergeData(backupData)
            ImportStrategy.SELECTIVE -> selectiveImport(backupData)
        }
    }

    enum class ImportStrategy {
        REPLACE,  // Clear existing and import
        MERGE,    // Keep existing and add new
        SELECTIVE // User chooses what to import
    }
}
```

#### 3. Backup Management Screen (4 hours)

**New File**: `presentation/screens/backup/BackupManagementScreen.kt`

- Export button with options (JSON/ZIP)
- Import with file picker
- Backup history list
- Auto-backup settings
- Cloud storage hints (manual upload)
- Preview before restore

```kotlin
@Composable
fun BackupManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datensicherung") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Export Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Daten exportieren",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Sichere alle deine Daten in einer Datei",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Button(
                            onClick = { viewModel.exportBackup(BackupFormat.JSON) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JSON")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.exportBackup(BackupFormat.ZIP) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Archive, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ZIP")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Daten importieren",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Stelle deine Daten aus einer Sicherung wieder her",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.selectFileForImport() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Datei auswählen")
                    }
                }
            }

            // Recent Backups
            if (uiState.recentBackups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Letzte Sicherungen",
                    style = MaterialTheme.typography.headlineMedium
                )
                LazyColumn {
                    items(uiState.recentBackups) { backup ->
                        BackupHistoryItem(
                            backup = backup,
                            onRestore = { viewModel.restoreBackup(it) },
                            onDelete = { viewModel.deleteBackup(it) },
                            onShare = { viewModel.shareBackup(it) }
                        )
                    }
                }
            }
        }
    }

    // Import preview dialog
    if (uiState.showImportPreview) {
        ImportPreviewDialog(
            backupData = uiState.previewData,
            onConfirm = { strategy ->
                viewModel.confirmImport(strategy)
            },
            onDismiss = { viewModel.cancelImport() }
        )
    }

    // Progress indicator
    if (uiState.isProcessing) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
```

---

#### 6. Database Migration Strategy (2 hours)

**Update**: `AppDatabase.kt`

- Implement non-destructive migrations
- Add migration from version 4 to 5
- Backup before migration
- Migration testing

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns for extended user profile
        database.execSQL("""
            ALTER TABLE UserProfile ADD COLUMN taxId TEXT DEFAULT ''
        """)
        database.execSQL("""
            ALTER TABLE UserProfile ADD COLUMN street TEXT DEFAULT ''
        """)
        // ... other schema changes ...
    }
}

Room.databaseBuilder(context, AppDatabase::class.java, "yoga_knete_database")
    .addMigrations(MIGRATION_4_5)
    .build()
```

---

## Sprint 4.2: Production Polish & Error Handling (2 days)

### Goals
Prepare the app for production use with proper error handling, logging, app branding, and final polish before handover.

### Implementation Tasks

#### 1. App Icon & Branding (2 hours)
**New Assets**: `res/mipmap-*` and `res/drawable`
- Create yoga-themed app icon with "YK" monogram
- Adaptive icon for Android 8+
- Consistent purple color theme

```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/yoga_purple_light"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
```

#### 2. Error Handling System (3 hours)
**New File**: `core/error/ErrorHandler.kt`
- Global exception handling
- User-friendly German error messages
- Prevent app crashes

```kotlin
@Singleton
class GlobalErrorHandler @Inject constructor() {
    fun handleError(error: Throwable): String {
        return when (error) {
            is IOException -> "Datei konnte nicht gelesen werden"
            is SQLiteException -> "Datenbankfehler aufgetreten"
            else -> "Ein Fehler ist aufgetreten"
        }
    }
}
```

#### 3. Logging Infrastructure (2 hours)  
**Setup Timber**: `YogaKneteApplication.kt`
- Debug logs only in debug builds
- No sensitive data in logs
- Clean logs for release

```kotlin
class YogaKneteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
```

#### 4. Input Validation (3 hours)
**New File**: `core/validation/Validators.kt`
- IBAN validation (German format)
- Date/time validation
- Prevent invalid data entry

```kotlin
object Validators {
    fun validateIBAN(iban: String): Boolean {
        val cleaned = iban.replace(" ", "")
        return cleaned.matches(Regex("^DE\\d{20}$"))
    }
    
    fun validateHourlyRate(rate: String): Boolean {
        return rate.toDoubleOrNull()?.let { it > 0 } ?: false
    }
}
```

#### 5. Release Build Configuration (2 hours)
**Update**: `app/build.gradle.kts`
- ProGuard configuration
- APK size optimization  
- Version management

```kotlin
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

#### 6. Final Testing Checklist (4 hours)
- [ ] All user flows work without crashes
- [ ] Backup and restore functional
- [ ] German text reviewed and correct
- [ ] App works on Android 7.0+
- [ ] No memory leaks
- [ ] Invoice generation works
- [ ] Database migration tested
- [ ] Release APK builds successfully

---

## Testing Requirements

### Sprint 4.1 - Backup System Tests
```kotlin
class BackupExportServiceTest {
    @Test fun `export creates valid JSON file`()
    @Test fun `export includes all entities`()
    @Test fun `export handles empty database`()
}

class BackupImportServiceTest {
    @Test fun `import validates file format`()
    @Test fun `merge strategy preserves existing data`()
    @Test fun `replace strategy clears old data`()
}
```

### Sprint 4.2 - Production Tests  
```kotlin
class ValidatorsTest {
    @Test fun `IBAN validation accepts valid German IBANs`()
    @Test fun `hourly rate validation works`()
}

class ErrorHandlerTest {
    @Test fun `error handler returns German messages`()
    @Test fun `app doesn't crash on exceptions`()
}
```

---

## Timeline

### Sprint 4.1: Backup System (2 days)
- Day 1: Export/Import services (10 hours)
- Day 2: UI and database migration (6 hours)

### Sprint 4.2: Production Polish (2 days)
- Day 1: Icon, error handling, logging (8 hours)
- Day 2: Validation, testing, release prep (8 hours)

---

## Definition of Done

### Sprint 4.1 Completion
- [ ] Backup export working
- [ ] Backup import working
- [ ] UI for backup management
- [ ] Database migration tested

### Sprint 4.2 Completion
- [ ] App icon created
- [ ] Error handling implemented
- [ ] Logging configured
- [ ] Validation working
- [ ] Release APK builds
- [ ] All tests passing

### Production Ready
- [ ] No crashes in main flows
- [ ] German text correct
- [ ] Backup/restore functional
- [ ] Ready for handover to wife

---

## Dependencies to Add

```kotlin
// app/build.gradle.kts
dependencies {
    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // Testing
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}
```
