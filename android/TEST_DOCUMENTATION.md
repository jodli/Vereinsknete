# YogaKnete Test Documentation

## Test Structure

The YogaKnete Android app has a basic test suite designed as a safety net for essential functionality. The tests are minimal but cover the critical areas of the application.

## Test Coverage

### 1. Domain Model Tests
- **YogaClassTest** (`app/src/test/java/de/yogaknete/app/domain/model/YogaClassTest.kt`)
  - Tests YogaClass data class creation with default and custom values
  - Verifies ClassStatus enum values
  
- **StudioTest** (`app/src/test/java/de/yogaknete/app/domain/model/StudioTest.kt`)
  - Tests Studio data class with required and optional fields
  - Verifies hourly rate precision

### 2. Utility Tests
- **DateUtilsTest** (`app/src/test/java/de/yogaknete/app/core/utils/DateUtilsTest.kt`)
  - Tests week start/end calculation (Monday-Sunday)
  - Tests date formatting in German
  - Tests duration calculations
  - Tests date comparison functions (isToday, isPast)

### 3. Repository Tests
- **YogaClassRepositoryImplTest** (`app/src/test/java/de/yogaknete/app/data/repository/YogaClassRepositoryImplTest.kt`)
  - Tests CRUD operations for YogaClass
  - Verifies date range queries
  - Tests invoice-related queries
  
- **StudioRepositoryImplTest** (`app/src/test/java/de/yogaknete/app/data/repository/StudioRepositoryImplTest.kt`)
  - Tests CRUD operations for Studio
  - Tests active/inactive studio filtering
  - Verifies batch operations

### 4. ViewModel Tests
- **WeekViewModelTest** (`app/src/test/java/de/yogaknete/app/presentation/screens/week/WeekViewModelTest.kt`)
  - Tests week navigation (previous/next/today)
  - Tests class statistics calculation (only completed classes count)
  - Tests earnings calculation with studio rates
  - Tests state management

## Running Tests

### Prerequisites
- Android Studio or Gradle command line
- JDK 11-17 (Note: Tests may not run with JDK 24+ due to compatibility issues)

### Command Line
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "*YogaClassTest"

# Run with more output
./gradlew test --info
```

### Android Studio
1. Right-click on the test file or test directory
2. Select "Run Tests"

## Test Philosophy

These tests follow a minimalist approach suitable for a personal-use app:
- **Focus on core functionality**: Tests cover the most critical parts that could break
- **Simple assertions**: Tests verify basic behavior without over-complication
- **Mocking external dependencies**: Uses MockK for repository tests to isolate business logic
- **Fast execution**: All tests are unit tests that run quickly

## Known Issues

### Java 24 Compatibility
There's currently a compatibility issue with Java 24 and the Android Gradle Plugin's test runner. If you encounter errors like "Type T not present", consider using JDK 11-17 for running tests.

### Workaround
Tests compile successfully even with Java 24, confirming they are syntactically correct. You can verify compilation with:
```bash
./gradlew :app:compileDebugUnitTestKotlin
```

## Adding New Tests

When adding new features, consider adding basic tests for:
1. Data classes - ensure fields are properly initialized
2. Critical utility functions - especially date/time calculations
3. Repository methods - verify DAO calls are made correctly
4. ViewModel state changes - ensure UI state updates as expected

Keep tests simple and focused on preventing regressions in existing functionality.
