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

## 📊 Data Models

### Simplified Domain Models

```kotlin
// Core business entities
data class YogaClass(
    val id: Long = 0,
    val studioId: Long,
    val className: String,      // "Hatha Yoga", "Rücken-Yoga"
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: ClassStatus = ClassStatus.SCHEDULED,
    val notes: String? = null
)

enum class ClassStatus {
    SCHEDULED,    // Geplant
    COMPLETED,    // Durchgeführt
    CANCELLED,    // Ausgefallen
    HOLIDAY,      // Urlaub
    SICK         // Krank
}

data class Studio(
    val id: Long = 0,
    val name: String,           // "Yoga Studio München"
    val contactPerson: String,  // "Herr Schmidt" - Ansprechpartner
    val contactEmail: String,   // "rechnung@studio.de"
    val hourlyRate: BigDecimal, // 45.00 EUR (can vary per studio)
    val isActive: Boolean = true
)

data class UserProfile(
    val id: Long = 0,
    val name: String,           // "Maria Mustermann"
    val address: String,        // "Yogaweg 15, 80333 München"
    val phone: String?,         // "+49 123 456789"
    val email: String,          // "maria@yoga.de"
    val taxId: String,          // "123/456/78901" Steuernummer
    val bankName: String,       // "Sparkasse München"
    val iban: String,           // "DE89 3704 0044 0532 0130 00"
    val bic: String?            // "COBADEFF"
)

data class ClassTemplate(
    val id: Long = 0,
    val name: String,           // "Montag Abend Yoga"
    val studioId: Long,
    val className: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)

data class Invoice(
    val id: Long = 0,
    val invoiceNumber: String,  // "2025-0003" auto-generated
    val studioId: Long,
    val month: YearMonth,
    val classes: List<YogaClass>,
    val totalHours: BigDecimal,
    val hourlyRate: BigDecimal,
    val totalAmount: BigDecimal,
    val pdfPath: String?,
    val createdAt: Instant,
    val sentAt: Instant? = null
)
```

---

## 🚀 Implementation Phases

### Phase 1: MVP Foundation (Week 1)
**Goal**: Minimal working app with class tracking

#### Sprint 1.1: Project Setup (1 day)
- [ ] Initialize Android project with Kotlin & Compose
- [ ] Setup Gradle with minimal dependencies
- [ ] Create basic Material 3 yoga theme
- [ ] Setup Room database with basic schema

#### Sprint 1.2: Onboarding & Studio Management (2 days)
- [ ] User profile setup screen (name, tax ID, bank details)
- [ ] Add studio screen (name, contact person, email, hourly rate)
- [ ] Store in database
- [ ] Skip onboarding if already completed

#### Sprint 1.3: Basic Week View (2 days)
- [ ] Display current week with day cards
- [ ] Add class manually (studio, time, date)
- [ ] Mark class as completed/cancelled
- [ ] Navigate between weeks (previous/next)

**Deliverable**: App that can add studios and track classes

### Phase 2: Class Management Improvements (Week 2)
**Goal**: Easier class entry and management

#### Sprint 2.1: Templates & Quick Entry (2 days)
- [ ] Create recurring class templates
- [ ] Quick add from template
- [ ] Auto-fill last used values
- [ ] Default 1.25 hour duration

#### Sprint 2.2: Better Cancellation Handling (2 days)
- [ ] Cancel/reschedule classes
- [ ] Bulk cancel for past weeks
- [ ] Edit classes in past weeks
- [ ] Visual indicators for status

#### Sprint 2.3: Week Statistics (1 day)
- [ ] Calculate weekly hours
- [ ] Show earnings per studio
- [ ] Monthly overview stats

**Deliverable**: Fully functional class tracking

### Phase 2: Invoice Generation (Week 3)
**Goal**: Professional PDF invoices with sharing

#### 2.1 Invoice List Screen (2 days)
- [ ] Monthly invoice overview
- [ ] Studio selection cards
- [ ] Calculate hours and amounts
- [ ] Show payment status

#### 2.2 PDF Generation (3 days)
- [ ] Professional black & white invoice template
- [ ] Include sender info (from user profile)
- [ ] Include receiver info (studio contact person)
- [ ] Auto-generate invoice number (YYYY-MM-XXX)
- [ ] List all completed classes with dates
- [ ] Show hours, hourly rate, total amount
- [ ] Add bank transfer details at bottom
- [ ] PDF generation with iText7 or Android Print

#### 2.3 Sharing Integration (2 days)
- [ ] WhatsApp sharing intent
- [ ] Email attachment support
- [ ] Save to device storage
- [ ] Share history tracking

### Phase 4: Polish & Refinements (Week 4)
**Goal**: Production-ready app with good UX

#### 3.1 Onboarding Flow (2 days)
- [ ] Welcome screen with setup wizard
- [ ] User profile input (name, tax ID, bank)
- [ ] Studio configuration
- [ ] Weekly template setup
- [ ] Import existing data option

#### 3.2 Templates & Patterns (2 days)
- [ ] Create/edit class templates
- [ ] Auto-fill from history
- [ ] Recurring class patterns
- [ ] Batch operations support

#### 3.3 Advanced Features (3 days)
- [ ] Holiday/sick day management
- [ ] Monthly statistics dashboard
- [ ] Backup/restore functionality
- [ ] Settings & preferences screen
- [ ] German localization throughout

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

## 📋 Development Checklist

### Week 1: Foundation ✅
- [ ] Project setup complete
- [ ] Database schema implemented
- [ ] Basic navigation working
- [ ] Week view displaying mock data
- [ ] Can mark class as completed

### Week 2: Core Features 📝
- [ ] Quick entry working
- [ ] Data persisting to database
- [ ] Templates functional
- [ ] Week navigation smooth
- [ ] Basic stats calculating

### Week 3: Invoicing 💰
- [ ] Invoice list screen complete
- [ ] PDF generation working
- [ ] Sharing via WhatsApp
- [ ] Professional invoice layout
- [ ] Payment tracking

### Week 4: Polish 🎨
- [ ] Onboarding flow complete
- [ ] All German translations
- [ ] Smooth animations
- [ ] Error handling robust
- [ ] Ready for daily use

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

### Initial Setup Commands
```bash
# Create new Android project
cd /mnt/quickstuff/git/Vereinsknete
mkdir -p android && cd android

# Initialize with gradle
gradle init --type basic --dsl kotlin

# Add Android plugin and dependencies
# (Configure in build.gradle.kts)
```

### Key Dependencies
```kotlin
dependencies {
    // Compose
    implementation("androidx.compose:compose-bom:2024.02.00")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // PDF
    implementation("com.itextpdf:itext7-core:7.2.5")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## 🎯 Next Steps

1. **Review this plan with your wife**
   - Does the workflow match her needs?
   - Any missing features?
   - Color/design preferences?

2. **Set up development environment**
   - Install Android Studio
   - Create project structure
   - Configure Gradle

3. **Start with Phase 1**
   - Focus on week view first
   - Get early feedback
   - Iterate quickly

---

## 📝 Notes for Discussion

### Key Features Based on Feedback
1. **Dynamic Studio Management**: Add unlimited studios during app use
2. **Flexible Cancellations**: Can cancel/edit classes in past weeks
3. **Professional Invoices**: Black & white with all required fields
4. **Incremental Development**: Small, working releases every week
5. **Yoga Theme**: Purple/calming colors for the app interface

### Potential Enhancements (Post-MVP)
- [ ] Participant tracking
- [ ] 10-class punch cards
- [ ] Cloud backup (Google Drive)
- [ ] Multi-instructor support
- [ ] Revenue analytics
- [ ] Tax report generation

### Risk Mitigation
- **Data Loss**: Auto-backup to local storage daily
- **Complexity Creep**: Strictly follow MVP scope
- **User Adoption**: Include wife in all UX decisions
- **Technical Debt**: Refactor after each phase

---

## 🤝 Let's Iterate!

This plan is a starting point. Let's discuss:
1. What excites you most?
2. What concerns you?
3. What's missing?
4. Should we adjust the timeline?
5. Any technical preferences?

Ready to build something your wife will love using! 🧘‍♀️✨
