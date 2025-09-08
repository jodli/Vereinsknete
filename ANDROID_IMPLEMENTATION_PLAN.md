# 📱 YogaKnete Android Implementation Plan

## 🎯 Project Overview

### Vision

Build a **simplified, mobile-first Android app** specifically designed for yoga instructors to track classes and generate invoices with minimal friction. Focus on your wife's specific workflow rather than generic flexibility.

### Key Success Metrics

- ⏱️ **Time to log a class**: < 10 seconds
- 📄 **Time to generate invoice**: < 30 seconds
- 👆 **Taps for common tasks**: ≤ 3
- 😊 **User satisfaction**: Your wife actually uses it daily!

---

## 🏗️ Technical Architecture

### Tech Stack

```kotlin
📱 Frontend:
  - Kotlin 1.9+
  - Jetpack Compose (modern declarative UI)
  - Material Design 3 (Material You)

🎨 UI/UX:
  - Single Activity Architecture
  - Bottom Navigation
  - Compose Navigation

📦 Data Layer:
  - Room Database (local SQLite)
  - Proto DataStore (preferences)
  - Kotlin Coroutines + Flow

🏛️ Architecture:
  - MVVM with Clean Architecture
  - Repository Pattern
  - Use Cases for business logic
  - Dependency Injection (Hilt)

📄 PDF Generation:
  - iText7 or Android Print Framework
  - Custom templates for invoices

🧪 Testing:
  - JUnit 4 + MockK
  - Compose UI Testing
  - Room Database Testing
  - Espresso for E2E tests
```

### Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/de/yogaknete/
│   │   │   ├── YogaKneteApp.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── core/
│   │   │   │   ├── di/           # Dependency injection modules
│   │   │   │   ├── utils/        # Date, time, currency helpers
│   │   │   │   └── navigation/   # Navigation setup
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── YogaDatabase.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   └── entities/
│   │   │   │   │   └── preferences/
│   │   │   │   └── repository/
│   │   │   ├── domain/
│   │   │   │   ├── model/        # Domain models
│   │   │   │   ├── repository/   # Repository interfaces
│   │   │   │   └── usecase/      # Business logic
│   │   │   └── presentation/
│   │   │       ├── theme/        # Colors, typography, shapes
│   │   │       ├── components/   # Reusable UI components
│   │   │       └── screens/
│   │   │           ├── week/     # Week view (main screen)
│   │   │           ├── invoice/  # Invoice generation
│   │   │           ├── settings/ # User settings
│   │   │           └── onboarding/ # First-time setup
│   │   └── res/
│   └── test/                      # Unit tests
│   └── androidTest/               # Instrumented tests
└── build.gradle.kts
```

---

## 🚀 Implementation Phases

### Phase 1: MVP Foundation (Week 1)

**Goal**: Minimal working app with class tracking

#### Sprint 1.1: Project Setup (1 day)

- [x] Initialize Android project with Kotlin & Compose
- [x] Setup Gradle with minimal dependencies
- [x] Create basic Material 3 yoga theme
- [x] Setup Room database with basic schema

#### Sprint 1.2: Onboarding & Studio Management (2 days)

- [x] User profile setup screen (name, tax ID, bank details)
- [x] Add studio screen (name, contact person, email, hourly rate)
- [x] Store in database
- [x] Skip onboarding if already completed

#### Sprint 1.3: Basic Week View (2 days)

- [x] Display current week with day cards
- [x] Add class manually (studio, time, date)
- [x] Mark class as completed/cancelled
- [x] Navigate between weeks (previous/next)

**Deliverable**: App that can add studios and track classes

### Phase 2: Class Management Improvements (Week 2)

**Goal**: Easier class entry and management

#### Sprint 2.1: Templates & Quick Entry (2 days)

- [x] Create recurring class templates
- [x] Quick add from template
- [x] Auto-fill last used values
- [x] Default 1.25 hour duration

#### Sprint 2.2: Better Cancellation Handling (2 days)

- [x] Cancel/reschedule classes
- [x] Bulk cancel for past weeks
- [x] Edit classes in past weeks
- [x] Visual indicators for status

#### Sprint 2.3: Week Statistics (1 day)

- [x] Calculate weekly hours
- [x] Show earnings per studio
- [x] Monthly overview stats

**Deliverable**: Fully functional class tracking

### Phase 3: Invoice Generation (Week 3)

**Goal**: Professional PDF invoices with sharing

#### Sprint 3.1 Invoice List Screen (2 days)

- [x] Monthly invoice overview
- [x] Studio selection cards
- [x] Calculate hours and amounts
- [x] Show payment status

#### Sprint 3.2 PDF Generation (3 days)

- [x] Professional black & white invoice template
- [x] Include sender info (from user profile)
- [x] Include receiver info (studio contact person)
- [x] Auto-generate invoice number (YYYY-XXX, current year and ongoing number for this year, e.g. 2025-002)
- [x] List all completed classes with dates
- [x] Show hours, hourly rate, total amount
- [x] Add bank transfer details (from user profile) at bottom
- [x] PDF generation with iText7 or Android Print

### Phase 4: Polish & Refinements (Week 4)

**Goal**: Production-ready app with good UX

#### Sprint 4.1 Onboarding Flow (2 days)

- [ ] Welcome screen with setup wizard
- [ ] User profile input (name, tax ID, bank)
- [ ] Studio configuration
- [ ] Weekly template setup
- [ ] Import existing data option

#### Sprint 4.2 Templates & Patterns (2 days)

- [ ] Create/edit class templates
- [ ] Auto-fill from history
- [ ] Recurring class patterns
- [ ] Batch operations support

#### Sprint 4.3 Advanced Features (3 days)

- [ ] Holiday/sick day management
- [ ] Monthly statistics dashboard
- [ ] Backup/restore functionality
- [ ] Settings & preferences screen
- [ ] German localization throughout

#### Sprint 4.4 Fix Bugs

- [x] User profile is missing tax ID, bank details, etc.
- [x] Add a way to change the user profile
- [x] When creating manual classes, you can't change the date
- [ ] Change away from destructive migrations for the database
- [x] The date selection does not work in the EditClassDialog
- [x] The BulkCancelDialog can't be triggered
- [x] invoice id is <year>-<increasing number per year>, e.g. 2025-005
- [x] add a way to remove generated invoices
- [x] also display invoices for inactive studios

---

## 🧪 Testing Strategy

### Unit Tests (40% coverage target)

```kotlin
// Example test structure
class YogaClassUseCaseTest {
    @Test
    fun `marking class as completed updates hours correctly`()
    @Test
    fun `cannot mark past class as scheduled`()
    @Test
    fun `invoice calculation matches hourly rate`()
}
```

### Key Test Scenarios

1. **Class Management**
   - Add new class with template
   - Mark class completed
   - Cancel class
   - Edit class details
2. **Invoice Generation**
   - Calculate monthly totals
   - Generate PDF correctly
   - Handle partial months
   - Multiple studios in same month

3. **Data Integrity**
   - Backup and restore
   - Migration from old data
   - Offline functionality

### Integration Tests

```kotlin
@Test
fun `complete week workflow - add, complete, invoice`() {
    // 1. Add classes for week
    // 2. Mark some as completed
    // 3. Generate invoice
    // 4. Verify calculations
}
```

---

## 🎨 UI/UX Design Principles

### Core Principles

1. **One-Thumb Operation**: All primary actions reachable
2. **Smart Defaults**: Learn from usage patterns
3. **Visual Feedback**: Clear success/error states
4. **Progressive Disclosure**: Hide complexity
5. **Offline-First**: Everything works without internet

### Key UI Components

```kotlin
// Custom composables
@Composable
fun YogaClassCard(
    yogaClass: YogaClass,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

@Composable
fun QuickEntryBottomSheet(
    templates: List<ClassTemplate>,
    onClassAdded: (YogaClass) -> Unit
)

@Composable
fun WeeklyCalendarView(
    weekStart: LocalDate,
    classes: List<YogaClass>,
    onClassClick: (YogaClass) -> Unit
)
```

### Color Scheme (Material You)

```kotlin
// Dynamic color with fallback
val primaryColor = Color(0xFF7B68EE)  // Yoga purple
val secondaryColor = Color(0xFF4CAF50) // Success green
val errorColor = Color(0xFFF44336)     // Cancel red
```

---

## 🚦 Go/No-Go Criteria

Before each phase, verify:

### Phase 1 Checkpoint

- ✅ Can add and complete classes
- ✅ Data persists between sessions
- ✅ Week view is intuitive
- ✅ Less than 3 taps for common tasks

### Phase 2 Checkpoint

- ✅ Invoices calculate correctly
- ✅ PDFs look professional
- ✅ WhatsApp sharing works
- ✅ No data loss issues

### Phase 3 Checkpoint

- ✅ Wife finds it easier than web app
- ✅ Onboarding takes < 5 minutes
- ✅ All text in German
- ✅ Feels polished and complete

---

## 🔧 Development Setup

### Prerequisites

```bash
# Required tools
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Kotlin 1.9+
```
