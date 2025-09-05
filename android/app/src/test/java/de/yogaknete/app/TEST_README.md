# YogaKnete App - Test Documentation

## Test Overview
Basic test suite for a private yoga class management app. Tests are kept minimal since this is a personal app.

## Test Files

### 1. **FunctionalitySummaryTest.kt**
- Documents all implemented functionality
- Acts as a living specification
- Confirms feature presence

### 2. **EntityTests.kt** 
- Tests data model creation
- Verifies default values
- Documents entity structure:
  - `UserProfile` - User data with hourly rate
  - `Studio` - Yoga studios/clubs
  - `YogaClass` - Individual yoga sessions

### 3. **RepositoryTests.kt**
- Tests repository implementations
- Uses mockk for DAO mocking
- Verifies data flow

### 4. **OnboardingViewModelTest.kt**
- Tests onboarding business logic
- Verifies profile and studio saving
- Uses coroutines test utilities

## Running Tests

```bash
# Run all tests
gradle test

# Run with coverage (if needed)
gradle testDebugUnitTest
```

## Test Results
Results are generated in: `app/build/test-results/testDebugUnitTest/`

## Current Coverage

✅ **Tested:**
- Data models
- Repository layer
- ViewModel logic
- Core business rules

❌ **Not Tested (UI):**
- Composable screens (manual testing only)
- Navigation flow
- UI validation

This is intentional - UI is tested manually since this is a private app.
