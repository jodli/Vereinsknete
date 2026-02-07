# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

YogaKnete (package: `de.yogaknete.app`) is a single-user Android app for yoga instructors to track classes and generate invoices. German-only UI. Offline-first with local Room database. The entire Android project lives under `android/`.

## Build & Development Commands

All commands run from the `android/` directory:

```bash
# Build
gradle assembleDebug
gradle assembleRelease        # requires keystore config

# Tests
gradle testDebugUnitTest      # unit tests (no instrumented tests)
gradle test                   # all unit tests

# Lint
gradle lintDebug

# Install on device
gradle installDebug

# Seed test data on device
./scripts/seed-db.sh          # pushes seed JSON to device, then import via Datensicherung screen
```

**JDK requirement:** Use JDK 11–17. JDK 24+ breaks the Android test runner.

## Architecture

MVVM + Clean Architecture with Hilt DI. Single-Activity app (`MainActivity`) with Jetpack Compose Navigation.

```
android/app/src/main/java/de/yogaknete/app/
├── core/           # DI modules (DatabaseModule, RepositoryModule), navigation, utilities
├── data/           # Room DAOs, entities, repository implementations
├── domain/         # Models, repository interfaces, services, use cases
├── presentation/   # Compose screens + ViewModels, organized by feature
└── ui/theme/       # Material 3 theme (Color, Type, Theme, Icons)
```

### Key patterns

- **State**: ViewModels expose `StateFlow`, Compose collects via `collectAsState()`
- **Data flow**: Screen → ViewModel → Repository (interface) → RepositoryImpl → Room DAO
- **DI**: Hilt `@HiltViewModel` for ViewModels, `@Module`/`@Provides` with `SingletonComponent` for singletons
- **Navigation**: String-based routes in `MainActivity` (`"week"`, `"invoices"`, `"invoice_detail/{invoiceId}"`, `"templates"`, `"profile_edit"`, `"studios"`, `"backup"`). Note: `core/navigation/Navigation.kt` is an older scaffold — actual navigation lives in `MainActivity.kt`
- **Date/time**: Uses `kotlinx-datetime` (`LocalDate`, `LocalDateTime`), not `java.util.*`
- **Serialization**: `kotlinx-serialization-json` for backup/restore (`BackupData` model)

### Domain models

- **YogaClass** — individual session with status (SCHEDULED/COMPLETED/CANCELLED) linked to a Studio
- **Studio** — yoga studio with contact info and hourly rate
- **Invoice** — monthly invoice per studio with payment status, PDF generation
- **ClassTemplate** — recurring class definition for auto-scheduling
- **UserProfile** — instructor's business info (name, address, tax ID, IBAN) used in invoice generation

### Invoice generation pipeline

`InvoiceHtmlGenerator` → HTML template (German) → iText7 PDF conversion (`InvoicePdfService`) → EPC QR code via ZXing (`EpcQrCodeGenerator`) embedded in PDF for SEPA bank transfers.


## Testing

Unit tests only (no UI instrumented tests). Located in `android/app/src/test/`. Uses JUnit 4 + MockK + kotlinx-coroutines-test. Tests cover models, repositories, ViewModels, date utilities, EPC QR generation, and invoice HTML generation.

## CI/CD

- **CI** (`.github/workflows/ci.yml`): lint → unit tests → debug APK on push/PR
- **CD** (`.github/workflows/cd.yml`): signed release APK + AAB on tag push (`v*`)
