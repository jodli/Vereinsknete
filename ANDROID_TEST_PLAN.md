# 🧪 YogaKnete Android Test Plan

## 📋 Test Strategy Overview

### Test Pyramid
```
         /\        E2E Tests (10%)
        /  \       - Critical user journeys
       /    \      - Full workflow validation
      /──────\     
     /        \    Integration Tests (30%)
    /          \   - Database operations
   /            \  - Repository layer
  /──────────────\ 
 /                \ Unit Tests (60%)
/                  \- Business logic
──────────────────── - ViewModels, Use Cases
```

### Coverage Goals
- **Overall**: 70% code coverage
- **Business Logic**: 90% coverage
- **UI Components**: 50% coverage
- **Database Layer**: 80% coverage

---

## 🔬 Unit Tests

### 1. Domain Layer Tests

#### YogaClassUseCaseTests
```kotlin
class YogaClassUseCaseTests {
    
    @Test
    fun `calculate class duration returns correct hours`() {
        // Given: A class from 17:30 to 18:45
        val yogaClass = YogaClass(
            startTime = LocalTime.of(17, 30),
            endTime = LocalTime.of(18, 45)
        )
        
        // When: Calculate duration
        val duration = calculateDuration(yogaClass)
        
        // Then: Should return 1.25 hours
        assertEquals(1.25, duration, 0.01)
    }
    
    @Test
    fun `cannot mark future class as completed`() {
        // Given: A class tomorrow
        val futureClass = YogaClass(
            date = LocalDate.now().plusDays(1),
            status = ClassStatus.SCHEDULED
        )
        
        // When/Then: Should throw exception
        assertThrows<IllegalStateException> {
            markClassAsCompleted(futureClass)
        }
    }
    
    @Test
    fun `marking class as completed updates status correctly`() {
        // Given: Today's scheduled class
        val todayClass = YogaClass(
            date = LocalDate.now(),
            status = ClassStatus.SCHEDULED
        )
        
        // When: Mark as completed
        val completed = markClassAsCompleted(todayClass)
        
        // Then: Status should be COMPLETED
        assertEquals(ClassStatus.COMPLETED, completed.status)
    }
}
```

#### InvoiceCalculationTests
```kotlin
class InvoiceCalculationTests {
    
    @Test
    fun `invoice totals calculated correctly for single studio`() {
        // Given: 4 completed classes, 1.25 hours each, 31.50€/hour
        val classes = listOf(
            createCompletedClass(hours = 1.25),
            createCompletedClass(hours = 1.25),
            createCompletedClass(hours = 1.25),
            createCompletedClass(hours = 1.25)
        )
        val hourlyRate = BigDecimal("31.50")
        
        // When: Calculate invoice
        val invoice = calculateInvoice(classes, hourlyRate)
        
        // Then: Total should be 157.50€
        assertEquals(BigDecimal("157.50"), invoice.totalAmount)
        assertEquals(BigDecimal("5.00"), invoice.totalHours)
    }
    
    @Test
    fun `invoice excludes cancelled classes`() {
        // Given: Mix of completed and cancelled classes
        val classes = listOf(
            createCompletedClass(hours = 1.25),
            createCancelledClass(hours = 1.25),
            createCompletedClass(hours = 1.25)
        )
        
        // When: Calculate invoice
        val invoice = calculateInvoice(classes, BigDecimal("31.50"))
        
        // Then: Only count completed classes
        assertEquals(BigDecimal("78.75"), invoice.totalAmount)
        assertEquals(BigDecimal("2.50"), invoice.totalHours)
    }
    
    @Test
    fun `invoice handles different hourly rates per studio`() {
        // Test multiple studios with different rates
    }
}
```

#### TemplateMatchingTests
```kotlin
class TemplateMatchingTests {
    
    @Test
    fun `find matching template for current day and time`() {
        // Given: Monday 17:30 template exists
        val template = ClassTemplate(
            dayOfWeek = DayOfWeek.MONDAY,
            startTime = LocalTime.of(17, 30),
            endTime = LocalTime.of(18, 45)
        )
        
        // When: Request template for Monday evening
        val match = findBestTemplate(
            DayOfWeek.MONDAY, 
            LocalTime.of(17, 0)
        )
        
        // Then: Should return the template
        assertEquals(template, match)
    }
    
    @Test
    fun `suggest next class based on history`() {
        // Test pattern recognition
    }
}
```

### 2. Presentation Layer Tests

#### WeekViewModelTests
```kotlin
class WeekViewModelTests {
    
    private lateinit var viewModel: WeekViewModel
    private val repository = mockk<YogaClassRepository>()
    
    @Before
    fun setup() {
        viewModel = WeekViewModel(repository)
    }
    
    @Test
    fun `loading week data updates UI state`() = runTest {
        // Given: Repository returns classes
        coEvery { repository.getClassesForWeek(any()) } returns testClasses
        
        // When: Load week
        viewModel.loadWeek(LocalDate.now())
        
        // Then: UI state should update
        assertEquals(4, viewModel.uiState.value.classes.size)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }
    
    @Test
    fun `marking class completed updates statistics`() = runTest {
        // Given: Week with scheduled classes
        viewModel.loadWeek(LocalDate.now())
        
        // When: Mark one as completed
        viewModel.markClassCompleted(testClass.id)
        
        // Then: Stats should update
        val stats = viewModel.uiState.value.weekStats
        assertEquals(1, stats.completedClasses)
        assertEquals(BigDecimal("1.25"), stats.totalHours)
    }
    
    @Test
    fun `navigation between weeks updates date range`() {
        // Test week navigation logic
    }
}
```

---

## 🔗 Integration Tests

### 1. Database Tests

#### RoomDatabaseTests
```kotlin
@RunWith(AndroidJUnit4::class)
class YogaDatabaseTests {
    
    private lateinit var database: YogaDatabase
    private lateinit var classDao: YogaClassDao
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, YogaDatabase::class.java
        ).build()
        classDao = database.yogaClassDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun `insert and retrieve yoga class`() = runTest {
        // Given: A yoga class
        val yogaClass = createTestClass()
        
        // When: Insert into database
        classDao.insert(yogaClass)
        
        // Then: Should be retrievable
        val retrieved = classDao.getClassById(yogaClass.id)
        assertEquals(yogaClass, retrieved)
    }
    
    @Test
    fun `query classes for date range`() = runTest {
        // Given: Classes across multiple weeks
        insertTestClasses()
        
        // When: Query for specific week
        val weekClasses = classDao.getClassesBetween(
            startDate = LocalDate.of(2024, 11, 4),
            endDate = LocalDate.of(2024, 11, 10)
        )
        
        // Then: Should return only that week's classes
        assertEquals(4, weekClasses.size)
    }
    
    @Test
    fun `update class status persists`() = runTest {
        // Test status updates
    }
    
    @Test
    fun `cascade delete removes related data`() = runTest {
        // Test referential integrity
    }
}
```

### 2. Repository Tests

#### YogaClassRepositoryTests
```kotlin
class YogaClassRepositoryTests {
    
    @Test
    fun `repository handles database errors gracefully`() = runTest {
        // Given: Database throws exception
        coEvery { dao.insert(any()) } throws SQLiteException()
        
        // When: Try to save class
        val result = repository.saveClass(testClass)
        
        // Then: Should return error result
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `repository caches frequently accessed data`() = runTest {
        // Test caching behavior
    }
}
```

---

## 📱 UI Tests

### 1. Compose UI Tests

#### WeekViewScreenTests
```kotlin
@RunWith(AndroidJUnit4::class)
class WeekViewScreenTests {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `week view displays class cards`() {
        // Given: Week with classes
        composeTestRule.setContent {
            WeekViewScreen(
                classes = testClasses,
                onClassClick = {}
            )
        }
        
        // Then: Cards should be visible
        composeTestRule
            .onNodeWithText("TSV München")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("17:30 - 18:45")
            .assertIsDisplayed()
    }
    
    @Test
    fun `clicking class card opens confirmation dialog`() {
        // Given: Week view
        composeTestRule.setContent {
            WeekViewScreen(/*...*/)
        }
        
        // When: Click on class
        composeTestRule
            .onNodeWithText("TSV München")
            .performClick()
        
        // Then: Dialog appears
        composeTestRule
            .onNodeWithText("Kurs abschließen?")
            .assertIsDisplayed()
    }
    
    @Test
    fun `swipe gesture navigates between weeks`() {
        // Test swipe navigation
    }
}
```

### 2. Bottom Sheet Tests

```kotlin
class QuickEntryBottomSheetTests {
    
    @Test
    fun `template chips are displayed`() {
        // Test template selection
    }
    
    @Test
    fun `time picker shows common slots`() {
        // Test time selection
    }
    
    @Test
    fun `save button enabled only with valid data`() {
        // Test validation
    }
}
```

---

## 🚀 End-to-End Tests

### 1. Critical User Journeys

#### CompleteWeekWorkflowTest
```kotlin
@LargeTest
@RunWith(AndroidJUnit4::class)
class CompleteWeekWorkflowTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun `complete weekly workflow from start to invoice`() {
        // 1. Open app - should show current week
        onView(withText("Diese Woche"))
            .check(matches(isDisplayed()))
        
        // 2. Add a new class using FAB
        onView(withId(R.id.fab_add_class))
            .perform(click())
        
        // 3. Select template
        onView(withText("Montag Abend Yoga"))
            .perform(click())
        
        // 4. Save class
        onView(withText("Speichern"))
            .perform(click())
        
        // 5. Mark class as completed
        onView(withText("TSV München"))
            .perform(click())
        
        onView(withText("Durchgeführt"))
            .perform(click())
        
        // 6. Navigate to invoice screen
        onView(withText("Rechnung erstellen"))
            .perform(click())
        
        // 7. Select studio
        onView(withText("TSV München"))
            .perform(click())
        
        // 8. Generate PDF
        onView(withText("PDF erstellen"))
            .perform(click())
        
        // 9. Verify success
        onView(withText("Rechnung wurde erstellt"))
            .check(matches(isDisplayed()))
    }
}
```

#### OnboardingFlowTest
```kotlin
@LargeTest
class OnboardingFlowTest {
    
    @Test
    fun `first time user completes onboarding`() {
        // 1. Welcome screen
        onView(withText("Willkommen bei YogaKnete"))
            .check(matches(isDisplayed()))
        
        // 2. Enter user details
        onView(withId(R.id.input_name))
            .perform(typeText("Maria Mustermann"))
        
        onView(withId(R.id.input_tax_id))
            .perform(typeText("123/456/78901"))
        
        // 3. Add studios
        // ... continue flow
        
        // 4. Setup weekly template
        // ... 
        
        // 5. Complete onboarding
        onView(withText("Fertig"))
            .perform(click())
        
        // 6. Verify main screen appears
        onView(withText("Diese Woche"))
            .check(matches(isDisplayed()))
    }
}
```

---

## 🐛 Edge Cases & Error Scenarios

### Data Validation Tests
```kotlin
@Test
fun `cannot create class with end time before start time`()

@Test
fun `handles timezone changes correctly`()

@Test
fun `handles daylight saving time transitions`()

@Test
fun `handles leap year dates`()

@Test
fun `handles maximum class duration (24 hours)`()
```

### Error Recovery Tests
```kotlin
@Test
fun `app recovers from database corruption`()

@Test
fun `handles out of storage space gracefully`()

@Test
fun `PDF generation fails with proper error message`()

@Test
fun `backup restoration handles version mismatches`()
```

### Performance Tests
```kotlin
@Test
fun `week view loads within 500ms with 100 classes`()

@Test
fun `invoice generation completes within 2 seconds`()

@Test
fun `app starts within 3 seconds on cold launch`()
```

---

## 📊 Test Data Generators

```kotlin
object TestDataFactory {
    
    fun createTestClass(
        date: LocalDate = LocalDate.now(),
        status: ClassStatus = ClassStatus.SCHEDULED,
        studioId: Long = 1L
    ): YogaClass {
        return YogaClass(
            id = Random.nextLong(),
            studioId = studioId,
            className = "Hatha Yoga",
            date = date,
            startTime = LocalTime.of(17, 30),
            endTime = LocalTime.of(18, 45),
            status = status
        )
    }
    
    fun createTestStudio(
        name: String = "TSV München",
        hourlyRate: BigDecimal = BigDecimal("31.50")
    ): Studio {
        return Studio(
            id = Random.nextLong(),
            name = name,
            contactEmail = "test@example.com",
            hourlyRate = hourlyRate,
            isActive = true
        )
    }
    
    fun createWeekOfClasses(): List<YogaClass> {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        return listOf(
            createTestClass(date = monday),
            createTestClass(date = monday.plusDays(1)),
            createTestClass(date = monday.plusDays(2)),
            createTestClass(date = monday.plusDays(3))
        )
    }
}
```

---

## 🔄 Continuous Integration Tests

### Pre-commit Checks
```bash
#!/bin/bash
# Run before each commit

./gradlew ktlintCheck
./gradlew detekt
./gradlew testDebugUnitTest
```

### CI Pipeline Tests
```yaml
# .github/workflows/android.yml
name: Android CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Unit Tests
        run: ./gradlew testDebugUnitTest
      
      - name: Integration Tests
        run: ./gradlew connectedAndroidTest
      
      - name: Generate Coverage Report
        run: ./gradlew jacocoTestReport
      
      - name: Lint Check
        run: ./gradlew lintDebug
```

---

## 🎯 Acceptance Criteria Tests

### User Story: Quick Class Entry
```gherkin
Feature: Quick Class Entry
  As a yoga instructor
  I want to quickly log my classes
  So that I don't waste time on data entry

  Scenario: Add class using template
    Given I am on the week view
    When I tap the add button
    And I select "Monday Evening Yoga" template
    Then a new class should be created with pre-filled data
    And I should see it in the week view
    
  Scenario: Add class with custom time
    Given I am adding a new class
    When I change the time to 19:00
    Then the end time should auto-adjust to 20:15
```

### User Story: Invoice Generation
```gherkin
Feature: Invoice Generation
  As a yoga instructor
  I want to generate monthly invoices
  So that I can bill my clients

  Scenario: Generate invoice for single studio
    Given I have 8 completed classes for TSV München in October
    When I generate an invoice for October
    Then the invoice should show 10 hours total
    And the amount should be 315.00 EUR
    And a PDF should be created
```

---

## 📈 Test Metrics & Reporting

### Key Metrics to Track
- **Code Coverage**: Target 70% overall
- **Test Execution Time**: < 5 minutes for full suite
- **Flaky Test Rate**: < 2%
- **Bug Escape Rate**: < 5 bugs per release

### Test Report Format
```
===========================================
YogaKnete Test Report - 2024-11-04
===========================================

Test Summary:
✅ Passed: 142
❌ Failed: 2
⏭️ Skipped: 5
⏱️ Duration: 3m 42s

Coverage:
- Overall: 72.3%
- Business Logic: 91.2%
- UI Components: 54.6%
- Database: 83.7%

Failed Tests:
1. InvoiceCalculationTests.handles_decimal_rounding
   - Expected: 157.50, Actual: 157.49
   
2. WeekViewScreenTests.swipe_navigation_works
   - Flaky test, gesture not recognized

Action Items:
- Fix decimal rounding in invoice calculation
- Improve gesture test reliability
===========================================
```

---

## 🚦 Test Execution Strategy

### Development Phase
```bash
# Run during development
./gradlew testDebugUnitTest --continuous  # Auto-run on file changes
```

### Pre-Release
```bash
# Full test suite
./gradlew test
./gradlew connectedAndroidTest
./gradlew jacocoTestReport
```

### Production Monitoring
- Crash reporting via Firebase Crashlytics
- Performance monitoring
- User behavior analytics
- A/B testing for new features

---

## ✅ Test Checklist

### Before Each Release
- [ ] All unit tests passing
- [ ] Integration tests passing
- [ ] UI tests passing on multiple devices
- [ ] Manual testing of critical flows
- [ ] Performance benchmarks met
- [ ] No memory leaks detected
- [ ] Accessibility tests passing
- [ ] German translations verified
- [ ] PDF generation tested
- [ ] Backup/restore tested

---

## 🎯 Next Steps

1. **Set up test infrastructure**
   - Configure test dependencies
   - Create test fixtures
   - Set up CI pipeline

2. **Write initial test suite**
   - Start with critical business logic
   - Add database tests
   - Create UI test skeleton

3. **Establish testing culture**
   - TDD for new features
   - Required test coverage for PRs
   - Regular test review meetings

Ready to build a robust, well-tested app! 🧪✨
