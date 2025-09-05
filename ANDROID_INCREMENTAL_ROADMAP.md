# 🚀 YogaKnete Android - Incremental Development Roadmap

## 🎯 Development Philosophy
**Small, Working Increments** - Each week delivers a usable feature that your wife can test

---

## 📅 Week 1: Foundation & First Working App
*Goal: Basic app that can track classes*

### Day 1-2: Project Setup & Data Models
```kotlin
// Minimal initial setup
- Android project with Compose
- Room database with 3 tables: UserProfile, Studio, YogaClass
- Basic navigation structure
- Yoga-themed colors (purple/calming)
```

### Day 3-4: Onboarding Flow
```
1. Welcome Screen
   └─> User Profile (Name, Address, Tax ID, Bank Details)
   └─> Add First Studio (Name, Contact Person, Email, Hourly Rate)
   └─> Main Week View
```

### Day 5: Basic Week View
```
┌─────────────────────────┐
│  Diese Woche            │
│  [<] November 4-10 [>]  │
│                         │
│  Mo 4.11 - Keine Kurse  │
│  Di 5.11 - Keine Kurse  │
│  Mi 6.11 - Keine Kurse  │
│                         │
│  [+ Kurs hinzufügen]    │
└─────────────────────────┘
```

### 🎯 Week 1 Deliverable
- ✅ App launches and saves user profile
- ✅ Can add studios with different hourly rates
- ✅ Can manually add a class
- ✅ Can mark class as completed/cancelled
- ✅ Data persists between app launches

**Test with your wife**: Can she add her studios and log Monday's class?

---

## 📅 Week 2: Making It Practical
*Goal: Faster class entry and better management*

### Day 1-2: Templates
```kotlin
// Add template system
data class ClassTemplate(
    val name: String,        // "Montag Yoga"
    val studioId: Long,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime, // Default: 17:30
    val duration: Float = 1.25f // Default: 75 minutes
)
```

**Features**:
- Create templates from existing classes
- One-tap to add class from template
- Auto-suggest based on current day/time

### Day 3: Cancellation Management
```
Past Week Editing:
- Long-press class to edit/cancel
- "Forgot to cancel last week's classes?"
- Bulk operations: Select multiple → Cancel all
```

### Day 4-5: Better UI/UX
```
Visual Improvements:
- ✅ Green border = Completed
- 🚫 Red strike = Cancelled
- ⭕ Gray circle = Scheduled
- Swipe right to complete
- Swipe left to cancel
```

### 🎯 Week 2 Deliverable
- ✅ Templates speed up class entry to <10 seconds
- ✅ Can edit/cancel classes from past weeks
- ✅ Visual feedback for class status
- ✅ Weekly statistics (hours, earnings)

**Test with your wife**: Is adding classes fast enough now?

---

## 📅 Week 3: Invoice Generation
*Goal: Professional invoices with one tap*

### Day 1: Invoice Data Preparation
```kotlin
// Invoice calculation logic
fun calculateInvoice(studio: Studio, month: YearMonth): Invoice {
    val classes = getCompletedClasses(studio, month)
    val totalHours = classes.sumOf { it.duration }
    val totalAmount = totalHours * studio.hourlyRate
    val invoiceNumber = generateInvoiceNumber() // "2024-11-001"
}
```

### Day 2-3: PDF Generation
```
Invoice Layout (Black & White):
┌─────────────────────────────────┐
│ RECHNUNG                        │
│ Nr: 2024-11-001                │
│                                 │
│ Von:                            │
│ Maria Mustermann                │
│ Yogaweg 15                      │
│ 80333 München                   │
│ Steuernr: 123/456/78901        │
│                                 │
│ An:                             │
│ Yoga Studio München             │
│ z.H. Herr Schmidt              │
│                                 │
│ Leistungszeitraum: Nov 2024    │
│                                 │
│ Datum      Leistung      Std.   │
│ 04.11.24   Hatha Yoga    1,25   │
│ 07.11.24   Hatha Yoga    1,25   │
│ 11.11.24   Hatha Yoga    1,25   │
│ 14.11.24   Hatha Yoga    1,25   │
│                                 │
│ Gesamt: 5,00 Std × 45,00€      │
│ Rechnungsbetrag: 225,00€       │
│                                 │
│ Bankverbindung:                │
│ Sparkasse München              │
│ IBAN: DE89 3704 0044...        │
└─────────────────────────────────┘
```

### Day 4: Sharing Integration
```kotlin
// WhatsApp/Email sharing
fun shareInvoice(invoice: Invoice) {
    val pdfFile = generatePDF(invoice)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(invoice.studio.contactEmail))
        putExtra(Intent.EXTRA_SUBJECT, "Rechnung ${invoice.invoiceNumber}")
        putExtra(Intent.EXTRA_STREAM, Uri.fromFile(pdfFile))
    }
    startActivity(Intent.createChooser(intent, "Rechnung senden"))
}
```

### Day 5: Invoice Management
- List of generated invoices
- Mark as "sent" after sharing
- Regenerate if needed
- Invoice history by studio

### 🎯 Week 3 Deliverable
- ✅ Generate professional PDF invoices
- ✅ All required fields included
- ✅ Share via WhatsApp/Email
- ✅ Invoice history tracking

**Test with your wife**: Generate and send October invoices

---

## 📅 Week 4: Polish & Production Ready
*Goal: Smooth, delightful experience*

### Day 1-2: Enhanced Studio Management
```
Studio Management Screen:
- Add new studio anytime
- Edit studio details
- Set active/inactive
- Different rates per studio
- Quick access from main screen
```

### Day 3: Backup & Restore
```kotlin
// Auto-backup to local storage
- Daily automatic backups
- Manual backup option
- Export to CSV
- Import from old app (if needed)
```

### Day 4: Final Polish
- Smooth animations
- Loading states
- Error handling
- German translations complete
- App icon and splash screen

### Day 5: Testing & Bug Fixes
- Complete user flow testing
- Fix any reported issues
- Performance optimization
- Prepare for daily use

### 🎯 Week 4 Deliverable
- ✅ Production-ready app
- ✅ All workflows smooth
- ✅ Data backup working
- ✅ Wife approved! 😊

---

## 🔄 Iterative Refinements (Post-Launch)

### Based on Daily Use Feedback:
- **Week 5**: Quick fixes and adjustments
- **Week 6**: Feature requests from actual use
- **Week 7**: Performance optimizations
- **Week 8**: Nice-to-have features

### Potential Future Features:
- Dashboard with monthly/yearly overview
- Export data for tax purposes
- Multiple instructor support (if needed)
- Cloud backup (Google Drive)
- Widget for today's classes

---

## 📊 Success Metrics Per Week

### Week 1 Success:
- [ ] Wife can add her studios
- [ ] Wife can log a class
- [ ] Data saves correctly

### Week 2 Success:
- [ ] Class entry takes < 10 seconds
- [ ] Can fix forgotten cancellations
- [ ] Templates match her schedule

### Week 3 Success:
- [ ] Invoice looks professional
- [ ] PDF generation works
- [ ] Can send via WhatsApp

### Week 4 Success:
- [ ] Wife uses it daily
- [ ] No crashes or data loss
- [ ] Faster than old web app

---

## 🛠️ Technical Approach

### Incremental Architecture:
```
Week 1: Monolithic, get it working
Week 2: Extract repositories
Week 3: Add use cases for complex logic
Week 4: Clean up and optimize
```

### Testing Strategy:
```
Week 1: Manual testing only
Week 2: Add critical unit tests
Week 3: Test invoice calculations
Week 4: Full test coverage
```

### Release Strategy:
```
- Internal testing APK every Friday
- Wife tests over weekend
- Fix issues Monday morning
- Iterate based on feedback
```

---

## 💡 Key Decisions Made

### Based on Your Feedback:
1. **Studios are dynamic** - Not hardcoded, add as needed
2. **Cancellations are flexible** - Can edit past weeks
3. **Invoice format is professional** - Black & white, all details
4. **Development is incremental** - Working app every week
5. **Theme is yoga-appropriate** - Calming purple colors

### Simplifications from Original Plan:
- No participant tracking
- No payment status tracking (just "sent")
- No complex statistics
- No cloud sync initially
- No multi-language (German only)

---

## 🎯 Next Immediate Steps

### To Start Development:

1. **Today**: Set up Android Studio project
   ```bash
   # Create project structure
   cd /mnt/quickstuff/git/Vereinsknete
   mkdir -p android-app
   # Initialize with Android Studio
   ```

2. **Tomorrow**: Create data models and database
   ```kotlin
   // Start with Room entities
   @Entity
   data class Studio(...)
   ```

3. **This Week**: Deliver first working version
   - Basic but functional
   - Your wife can start testing immediately

### Questions Before We Start:

1. **Default class duration**: Is 1.25 hours (75 min) standard?
2. **Invoice numbering**: Sequential (001, 002) or include date?
3. **Studio limit**: Roughly how many studios expected? (5? 10? 20?)
4. **Preferred testing day**: When can your wife test weekly builds?

---

## 🤝 Let's Build It!

This incremental approach means:
- **Week 1**: She can start using it (basic)
- **Week 2**: It becomes convenient
- **Week 3**: She can bill clients
- **Week 4**: It's polished and reliable

Ready to start with Week 1? We'll have something working in just a few days! 🚀
